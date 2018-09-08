package service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.karakal.commons.http.HttpClientUtil;
import constant.ElasticsearchProp;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yangmingquan on 2018/9/7.
 */
@Service
public class ESHttpClient {
    protected Logger logger = Logger.getLogger(this.getClass());
    @Autowired
    private ElasticsearchProp elasticsearchProp;


    public JSONObject query(String index, String type, Map query, Map sort, String[] fields, Integer size, Integer from){
        JSONObject hits = null;
        String path = String.format(elasticsearchProp.getUrl(), index, type);
        Map param = new HashMap();
        param.put("query",query);
        if(sort != null){
            param.put("sort",sort);
        }
        if(fields != null){
            param.put("_source",fields);
        }
        if(from != null){
            if(from > Integer.valueOf(elasticsearchProp.getEsQueryNum())){
                from = Integer.valueOf(elasticsearchProp.getEsQueryNum());
            }
            param.put("from",from);
        }
        if(size != null){
            if(size > Integer.valueOf(elasticsearchProp.getEsQueryNum())){
                size = Integer.valueOf(elasticsearchProp.getEsQueryNum());
            }
            if(from != null){
                size = from + size > 10000 ? 10000-from : size;
            }
            param.put("size",size);
        }

        String str = JSON.toJSONString(param);
        logger.info("DSL语句为"+str);
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost(path);
            post.setHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            post.setHeader("Connection", "keep-alive");
            String sessionId = getSessionId();
            post.setHeader("SessionId", sessionId);
            post.setHeader("appid", "mzk");
            StringEntity entity = new StringEntity( str, Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/x-www-form-urlencoded");
            post.setEntity(entity);
            HttpClientUtil.Response res = (HttpClientUtil.Response)httpClient.execute(post, new ResponseHandler() {
                public HttpClientUtil.Response handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine statusLine = response.getStatusLine();
                    HttpEntity entity = response.getEntity();
                    if(statusLine.getStatusCode() != 200) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    } else {
                        HttpClientUtil.Response res = new HttpClientUtil.Response();
                        res.setHeaders(response.getAllHeaders());
                        if(entity == null) {
                            throw new ClientProtocolException("Response contains no content");
                        } else {
                            ContentType contentType = ContentType.get(entity);
                            res.setContentType(contentType);
                            InputStream is = entity.getContent();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();

                            try {
                                byte[] buf = new byte[2048];
                                boolean len = false;

                                int len1;
                                while((len1 = is.read(buf)) != -1) {
                                    bos.write(buf, 0, len1);
                                }

                                res.setContent(bos.toByteArray());
                                HttpClientUtil.Response var10 = res;
                                return var10;
                            } finally {
                                IOUtils.closeQuietly(is);
                                IOUtils.closeQuietly(bos);
                            }
                        }
                    }
                }
            });
            JSONObject resultJson = JSON.parseObject(res.toString());
            //logger.info("es返回数据为"+resultJson);
            hits = resultJson.getJSONObject("hits");
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("请求es数据失败");
        }

        return hits;
    }

    public JSONObject query(String index,String type,Map query,Map sort,Integer size,Integer from){
        return query(index,type,query,sort,null,size,from);
    }

    // 构建唯一会话Id
    public static String getSessionId(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
    }
}
