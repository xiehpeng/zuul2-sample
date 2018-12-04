package com.gateway.filters.endpoint;

import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthFilter extends HttpSyncEndpoint {

    private static final Logger log = LoggerFactory.getLogger(HealthFilter.class);

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200);
        resp.setBodyAsText("healthy");

        // need to set this manually since we are not going through the ProxyEndpoint
        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS);

        log.info("xhp debug endpoint filter....");

        return resp;
    }
}
