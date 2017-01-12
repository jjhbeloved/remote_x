package cd.blog.humbird.vertx.vx.beans;

import io.vertx.core.http.HttpServerRequest;

import java.io.Serializable;

public class VxQueryParams implements Serializable {

    private static final long serialVersionUID = 1L;
    private int offset = -1;
    private int limit = -1;
    private String sort;
    private String order;
    private String search;


    public String toString() {
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("offset:").append(offset);
        sbInfo.append(",limit:").append(limit);
        sbInfo.append(",sort:").append(sort);
        sbInfo.append(",order:").append(order);
        sbInfo.append(",search:").append(search);
        return sbInfo.toString();
    }

    public void parse(HttpServerRequest req) {
        String sOffset = req.getParam("offset");
        if (sOffset != null) {
            offset = Integer.parseInt(req.getParam("offset"));
            limit = Integer.parseInt(req.getParam("limit"));
        } else {
            offset = -1;
            limit = -1;
        }
        sort = req.getParam("sort");
        order = req.getParam("order");
        search = req.getParam("search");
    }

    /**
     * @return limit属性
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit 设置limit属性
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return offset属性
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset 设置offset属性
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return sort属性
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort 设置sort属性
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return order属性
     */
    public String getOrder() {
        return order;
    }

    /**
     * @param order 设置order属性
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * @return search属性
     */
    public String getSearch() {
        return search;
    }

    /**
     * @param search 设置search属性
     */
    public void setSearch(String search) {
        this.search = search;
    }
}
