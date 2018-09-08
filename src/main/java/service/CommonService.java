package service;

import bean.BaseSearchBean;
import com.alibaba.fastjson.JSONObject;
import com.karakal.commons.bean.QueryResult;
import com.karakal.commons.util.MapUtil;
import util.BeanUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by yangmingquan on 2018/9/7.
 */
public class CommonService {
    private Set<String> matchKeys = new HashSet<>();
    {
        matchKeys.add("company");
        matchKeys.add("tuttiband");
    }

    private static Set<String> matchPhraseKeys = new HashSet<>();
    static {
        matchPhraseKeys.add("prize");
        matchPhraseKeys.add("honors");
        matchPhraseKeys.add("ethnic");
    }

    /**
     * {
     "query": {
     "bool" : {
     "must" : {
     "term" : { "user" : "kimchy" }
     },
     "filter": {
     "term" : { "tag" : "tech" }
     },
     "must_not" : {
     "range" : {
     "age" : { "gte" : 10, "lte" : 20 }
     }
     },
     "should" : [
     { "term" : { "tag" : "wow" } },
     { "term" : { "tag" : "elasticsearch" } }
     ],
     "minimum_should_match" : 1,
     "boost" : 1.0
     }
     }
     }
     * @return
     */
    public Map getBoolQuery(Object must, Object should, Object filter, Object must_not, Integer minimum_should_match, Float boost){
        Map bool = new HashMap();
        if(must != null){
            bool.put("must",must);
        }
        if(should != null){
            bool.put("should",should);
        }
        if(filter != null){
            bool.put("filter",filter);
        }
        if(must_not != null){
            bool.put("must_not",must_not);
        }
        if(minimum_should_match != null){
            bool.put("minimum_should_match",minimum_should_match);
        }
        if(boost != null){
            bool.put("boost",boost);
        }
        return MapUtil.createHashMap("bool",bool);
    }

    /**
     * 拼接multi_match对象
     * @param query
     * @param fields
     * @return
     */
    public Map getMultiMatch(String query,String[] fields,String type,String... analyzer){
        Map map = new HashMap();
        map.put("query",query);
        map.put("fields",fields);
        if(type != null){
            map.put("type",type);
        }
        if(analyzer.length>0){
            map.put("analyzer",analyzer[0]);
        }
        return MapUtil.createHashMap("multi_match",map);
    }

    public Map getMatch_phrase(String path,String key,Object value,Integer slop){
        if(path != null){
            key = path+"."+key;
        }
        Map map = new HashMap();
        map.put("query",value);
        map.put("slop",slop);
        Map query = MapUtil.createHashMap(key,map);
        return MapUtil.createHashMap("match_phrase",query);
    }

    public static QueryResult<JSONObject> queryCount(BaseSearchBean bean, long countBySearchLike) {
        QueryResult<JSONObject> queryResult = new QueryResult<>();
        queryResult.setTotalCount(countBySearchLike);
        queryResult.setPageSize(bean.getPageSize());
        queryResult.setPageTotal((int) Math.ceil(queryResult.getTotalCount() * 1.00 / queryResult.getPageSize()));
        // 设置页码数，如果大于最大的页码数，那么就取最大值
        queryResult.setPageNo(bean.getPageNo() >= queryResult.getPageTotal() ? queryResult.getPageTotal() : bean.getPageNo());
        // 设置开始行，如果大于最大页码数，就取页码最大值的开始行
        if (queryResult.getPageTotal() > 0) {
            queryResult
                    .setStartRow(bean.getPageNo() >= queryResult.getPageTotal() ? ((queryResult.getPageTotal() - 1) * queryResult.getPageSize() + 1)
                            : (bean.getStartRow() + 1));
        } else {
            queryResult.setStartRow(0);
        }
        return queryResult;
    }

    public Map getTerm(String path,String key,Object value){
        if(path != null){
            key = path+"."+key;
        }
        Map term = MapUtil.createHashMap(key,value);
        return MapUtil.createHashMap("term",term);
    }

    public Map getTerms(String path,String key,List values){
        if(path != null){
            key = path+"."+key;
        }
        Map term = MapUtil.createHashMap(key,values);
        return MapUtil.createHashMap("terms",term);
    }

    public Map getMatch(String path,String key,Object value,String matchType){
        if(matchType == null){
            matchType = "match";
        }
        if(path != null){
            key = path+"."+key;
        }
        Map term = MapUtil.createHashMap(key,value);
        return MapUtil.createHashMap(matchType,term);
    }

    /**
     * 将查询条件转化为dsl列表
     * @param object
     * @param keys
     * @param path
     * @return
     */
    public List<Map> getTermsOrMatch(Object object ,String[] keys, String path){
        //循环处理各个key,将传入的参数转化为查询语句,比如 查询艺人的标签,转换为dsl为{ "term" : { "artists.tags" : 1002598757 } }
        List<Map> list = new ArrayList<>();
        for(String key : keys){
            Object value = BeanUtils.getProVlaue(object,key);
            if(value == null){
                continue;
            }
            //部分字段映射为大于小于
            if("publishTimeStart".equals(key)){
                list.add(getRange(path, "publishTime", value, "gte"));
                continue;
            }
            if("publishTimeEnd".equals(key)){
                list.add(getRange(path, "publishTime", value, "lte"));
                continue;
            }
            if("songPublishTimeEnd".equals(key)){
                list.add(getRange(null, "publishTime", value, "lte"));
                continue;
            }
            if(value != null){
                if(value instanceof List){
                    //如果传入参数是list,比如tags:[111,222],需要转化为多个term.match表达式
                    List values = (List)value;
                    for(Object temp : values){
                        list.add(getTermOrMatch(path,key,temp));
                    }
                }else{
                    list.add(getTermOrMatch(path,key,value));
                }
            }
        }
        return list;
    }

    public Map getTermOrMatch(String path,String key,Object value){
        Map map = new HashMap();
        //某些特殊字段用match匹配
        if(matchKeys.contains(key)){
            map = getMatch(path,key,value,null);
        }else if(matchPhraseKeys.contains(key)){
            map = getMatch_phrase(path,key,value,1);
        }else{
            map = getTerm(path,key,value);
        }
        return map;
    }



    public Map getRange(String path,String key,Object value,String operator){
        if(path != null){
            key = path+"."+key;
        }
        Map oper = MapUtil.createHashMap(operator,value);
        Map range = MapUtil.createHashMap(key,oper);
        return MapUtil.createHashMap("range",range);
    }

    public Map getExists(String path,String key){
        if(path != null){
            key = path+"."+key;
        }
        Map exists = MapUtil.createHashMap("field",key);
        return MapUtil.createHashMap("exists",exists);
    }

    /**
     * 拼接es的查询DSL(nested部分)
     *
     */
    public Map getQueryDSL(Object param,String content,String path){
        //先获取查询参数,目前可能有 artist,album,performers,cantors等等
        Object body = BeanUtils.getProVlaue(param,content);
        if(body == null){
            return null;
        }
        //获取传入参数的key列表
        Set<String> keys =getKeySet(body);
        //Set<String> keys = body.keySet();
        String[] keyStrs = keys.toArray(new String[keys.size()]);
        //拼接查询dsl,[{ "term" : { "artists.tags" : 1002598757 } },{ "term" : { "artists.honors" : "最佳歌手" } }]
        List<Map> list = getTermsOrMatch(body, keyStrs, path);
        if(list.size()>0){
            //生成filter语句
            Map query = getBoolQuery(null,null,list,null,null,null);
            Map nested = getNested(path,query);
            return nested;
        }
        return null;
    }

    public Set<String> getKeySet(Object entity){
        Set<String> keys = new HashSet<>();
        if(entity instanceof Map){
            keys = ((Map) entity).keySet();
        }else{
            Field[] fields = entity.getClass().getDeclaredFields();
            for(Field field : fields){
                keys.add(field.getName());
            }
        }
        return keys;
    }

    public Map getNested(String path,Map query){
        Map map = new HashMap();
        map.put("path",path);
        map.put("query",query);
        return MapUtil.createHashMap("nested",map);
    }

    public void save(List<Long> ids,String content){

    }
}
