package bean;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by yangmingquan on 2018/9/7.
 */
public class BaseSearchBean {
    @ApiModelProperty(value = "页数")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "每页数量")
    private Integer pageSize = 20;
    @ApiModelProperty(value = "从第几条导出")
    private Integer startNo;
    @ApiModelProperty(value = "导出到第几条")
    private Integer endNo;
    @ApiModelProperty(value = "排序")
    private Integer orderBy;
    @ApiModelProperty(value = "名字")
    private String name;
    @ApiModelProperty(value = "excel")
    private Boolean isQueryExcel = false;
    @ApiModelProperty(value = "用户状态")
    private Integer mstatus;
    @ApiModelProperty(value = "城市")
    private String city;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setStartNo(Integer startNo) {
        this.startNo = startNo;
    }

    public final Integer getEndNo() {
        if (endNo != null && startNo != null) {
            return endNo - startNo + 1;
        }
        return endNo;
    }

    public void setEndNo(Integer endNo) {
        this.endNo = endNo;
    }

    public Integer getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(Integer orderBy) {
        this.orderBy = orderBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getQueryExcel() {
        return isQueryExcel;
    }

    public void setQueryExcel(Boolean queryExcel) {
        isQueryExcel = queryExcel;
    }

    public Integer getStartRow() {
        if(pageNo == null || pageSize == null){
            return 0;
        }
        return (pageNo-1)*pageSize;
    }

    public final Integer getStartNo() {
        if(startNo == null){
            return null;
        }
        return startNo - 1;
    }

    public Integer getMstatus() {
        return mstatus;
    }

    public void setMstatus(Integer mstatus) {
        this.mstatus = mstatus;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
