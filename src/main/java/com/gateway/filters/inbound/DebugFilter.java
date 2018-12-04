package com.gateway.filters.inbound;

import com.gateway.filters.endpoint.HealthFilter;
import com.gateway.filters.endpoint.TestFilter;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.netty.filter.ZuulEndPointRunner;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugFilter extends HttpInboundSyncFilter {

    private static final Logger log = LoggerFactory.getLogger(DebugFilter.class);

    private static final String HEALTH_PATH = "/healthcheck";

    @Override
    public HttpRequestMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        String path = request.getPath();
        String host = request.getOriginalHost();

        if (HEALTH_PATH.equalsIgnoreCase(path)) {
            context.setEndpoint(HealthFilter.class.getCanonicalName());
        }else if("/test".equalsIgnoreCase(path)){
            context.setEndpoint(TestFilter.class.getCanonicalName());
        }else {
            context.setEndpoint(ZuulEndPointRunner.PROXY_ENDPOINT_FILTER_NAME);
            context.setRouteVIP("api");
        }

        log.info("xhp debug inbound filter....");

        return request;
    }

    @Override
    public int filterOrder() {
        return 20;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return true;
    }
}
