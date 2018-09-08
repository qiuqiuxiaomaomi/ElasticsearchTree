package constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yangmingquan on 2018/9/7.
 */
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProp {
    private String clusterNodes;
    private String index;
    private String name;
    private String url;
    private String esQueryNum;

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEsQueryNum() {
        return esQueryNum;
    }

    public void setEsQueryNum(String esQueryNum) {
        this.esQueryNum = esQueryNum;
    }
}
