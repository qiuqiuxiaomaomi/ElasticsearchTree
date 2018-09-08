package service;

import bean.BaseSearchBean;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.karakal.commons.bean.QueryResult;
import constant.ElasticsearchProp;
import dao.mapper.UserInfoMapper;
import entity.UserInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.BeanUtils;
import util.CharUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangmingquan on 2018/9/7.
 */
@Service
public class MaterialSynchonizeService {
    public Logger logger = LoggerFactory.getLogger(MaterialSynchonizeService.this.getClass());
    @Autowired
    protected TransportClient elasticsearchClient;
    @Autowired
    private ElasticsearchProp elasticsearchProp;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ESHttpClient esHttpClient;
    @Resource
    protected SqlSessionTemplate sqlSession;
    @Autowired
    UserInfoMapper userInfoMapper;

    //有中文的不走拼音分词
    private static String[] cfields = new String[]{
            "name^500",
            "city^100",
            "address^50"};

    public static final Integer PAGE_SIZE = 100;

    public Object query(BaseSearchBean bean) {
        String searchName = bean.getName();
        QueryResult<JSONObject> result = null;
        List<JSONObject> userList = new ArrayList<>();
        Map multi_match = new HashMap();
        boolean flag = CharUtil.isChinese(searchName);
        if (flag == true) {
            multi_match = commonService.getMultiMatch(searchName, cfields, null);
        }
        //拼接整体的查询逻辑 query-->bool-->must-->[multi_match,nested]
        Map query = null;
        //增加filter
        List<Map> filters = new ArrayList<>();
        List statusList = new ArrayList();
        statusList.add(0);
        statusList.add(1);
        Map statusDSL = commonService.getTerms(null,"status",statusList);
        List cityList = new ArrayList();
        String city = bean.getCity();
        if(StringUtils.isNotEmpty(city) ){
            cityList.add(city);
            Map categoryDSL = commonService.getTerms(null,"city",cityList);
            filters.add(categoryDSL);
        }
        filters.add(statusDSL);
        List<Map> list = new ArrayList<>();
        list.add(multi_match);
        Map sort = new LinkedHashMap();
        sort.put("id","desc");
        query = commonService.getBoolQuery(null,list,filters,null,1,null);
        String[] sources = new String[]{"id","name", "city", "phone"};
        JSONObject hits = esHttpClient.query(elasticsearchProp.getIndex(), "userInfo", query, sort,sources, bean.getPageSize(),bean.getStartRow());
        //获取查询到的结果
        JSONArray hitsJSONArray = hits.getJSONArray("hits");
        for(int i = 0 ;i<hitsJSONArray.size();i++){
            JSONObject userTemp = hitsJSONArray.getJSONObject(i).getJSONObject("_source");
            userList.add(userTemp);
        }
        result = commonService.queryCount(bean, hits.getLong("total"));
        result.setList(userList);
        return result;
    }

    public void materialSync(Integer uid) {
        BulkRequestBuilder bulkRequest = elasticsearchClient.prepareBulk();
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(uid);
        try {
            Map<String, Object> userMap = BeanUtils.objectToMap(userInfo);
            bulkRequest.add(elasticsearchClient.prepareIndex(elasticsearchProp.getIndex(), "userInfo", uid.toString()).setSource(JSON.toJSONString(userMap)));
            BulkResponse responses = bulkRequest.execute().actionGet();
            String error = responses.buildFailureMessage();
            if (!error.equals("failure in bulk execution:")) {
                logger.info(error);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void importAll() {
        logger.info("开始进行全量用户信息导入!");
        Date first = new Date();
        int i = 0;
        while(true){
            BulkRequestBuilder bulkRequest = elasticsearchClient.prepareBulk();
            PageHelper.offsetPage(i, PAGE_SIZE);
            Date sta = new Date();
            logger.info("开始导入第"+i+"条数据");
            List<UserInfo> userInfos = userInfoMapper.selectAll();
            if(CollectionUtils.isEmpty(userInfos)){
                break;
            }
            try {
                //启动10个线程来收集数据
                ExecutorService executor = Executors.newFixedThreadPool(10);
                for (UserInfo userInfo : userInfos) {
                    executor.execute(new MyThread(userInfo, bulkRequest));
                }
                executor.shutdown();
                executor.awaitTermination(3600, TimeUnit.SECONDS);
                BulkResponse responses = bulkRequest.execute().actionGet();
                String error = responses.buildFailureMessage();
                if(!error.equals("failure in bulk execution:")){
                    logger.info(error);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            sqlSession.clearCache();
            Date end = new Date();
            logger.info("第"+i+"条数据导入完成,花费时间为"+(end.getTime()-sta.getTime())/1000);
            if(userInfos.size() < PAGE_SIZE){
                logger.info("所有数据导入完成,花费时间为"+(end.getTime()-first.getTime())/1000);
                break;
            }
            i = i + PAGE_SIZE;
        }
        return ;
    }

    /**
     *  实现专项库数据收集的内部类
     */
    public class MyThread extends Thread{
        private UserInfo userInfo;
        private BulkRequestBuilder bulkRequest;

        public MyThread(UserInfo userInfo,BulkRequestBuilder bulkRequest){
            this.userInfo = userInfo;
            this.bulkRequest = bulkRequest;
        }

        @Override
        public void run() {
            bulkRequest.add(elasticsearchClient.prepareIndex(elasticsearchProp.getIndex(), "speclib", userInfo.getId().toString()).setSource(JSON.toJSONString(userInfo)));
        }
    }


    public Object material(Object param, BaseSearchBean bean) {
        QueryResult<JSONObject> result = null;
        List<JSONObject> userInfoList = new ArrayList<>();
        String searchName = bean.getName();
        //增加filter
        Map multi_match = commonService.getMultiMatch(searchName,cfields,null);
        List<Map> musts = new ArrayList<>();
        List<Map> filters = new ArrayList<>();
        musts.add(multi_match);
        List statusList = new ArrayList();
        statusList.add(0);
        statusList.add(1);
        Map statusDSL = commonService.getTerms(null,"status",statusList);
        filters.add(statusDSL);
        Map query = commonService.getBoolQuery(null,musts,filters,null,1,null);
        Map sort = new LinkedHashMap();
        sort.put("_score","desc");
        Integer startNo = bean.getStartNo();
        Integer endNo = bean.getEndNo();
        if(startNo == null){
            startNo = bean.getStartRow();
        }
        if(endNo == null){
            endNo = bean.getPageSize();
        }
        String[] sources = new String[]{"id","name", "city", "phone"};
        JSONObject hits = esHttpClient.query(elasticsearchProp.getIndex(), "userInfo", query, sort,sources, endNo,startNo);
        JSONArray hitsJSONArray = hits.getJSONArray("hits");
        for(int i = 0 ;i<hitsJSONArray.size();i++){
            userInfoList.add(hitsJSONArray.getJSONObject(i).getJSONObject("_source"));
        }
        boolean isExcel = bean.getQueryExcel();
        //导出接口,只返回数据,部分在page对象
        if(isExcel){
            return userInfoList;
        }
        result = commonService.queryCount(bean, hits.getLong("total"));
        result.setList(userInfoList);
        return result;
    }
}
