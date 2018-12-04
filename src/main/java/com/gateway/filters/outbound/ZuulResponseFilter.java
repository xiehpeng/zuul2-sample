package com.gateway.filters.outbound;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.zuul.context.Debug;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.filters.http.HttpOutboundSyncFilter;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.niws.RequestAttempts;
import com.netflix.zuul.stats.status.StatusCategory;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.netflix.zuul.constants.ZuulHeaders.*;

public class ZuulResponseFilter extends HttpOutboundSyncFilter {
    private static final Logger log = LoggerFactory.getLogger(ZuulResponseFilter.class);

    private static final DynamicBooleanProperty SEND_RESPONSE_HEADERS =
            new DynamicBooleanProperty("zuul.responseFilter.send.headers", true);

    @Override
    public HttpResponseMessage apply(HttpResponseMessage response) {
        SessionContext context = response.getContext();

        if (SEND_RESPONSE_HEADERS.get()) {
            Headers headers = response.getHeaders();

            StatusCategory statusCategory = StatusCategoryUtils.getStatusCategory(response);
            if (statusCategory != null) {
                headers.set(X_ZUUL_STATUS, statusCategory.name());
            }

            RequestAttempts attempts = RequestAttempts.getFromSessionContext(response.getContext());
            String headerStr = "";
            if (attempts != null) {
                headerStr = attempts.toString();
            }
            headers.set(X_ZUUL_PROXY_ATTEMPTS, headerStr);

            headers.set(X_ZUUL, "zuul");
            headers.set(X_ZUUL_INSTANCE, "unknown");
            headers.set(CONNECTION, KEEP_ALIVE);
            headers.set(X_ZUUL_FILTER_EXECUTION_STATUS, context.getFilterExecutionSummary().toString());
            headers.set(X_ORIGINATING_URL, response.getInboundRequest().reconstructURI());

            if (response.getStatus() >= 400 && context.getError() != null) {
                Throwable error = context.getError();
                headers.set(X_ZUUL_ERROR_CAUSE,
                        error instanceof ZuulException ? ((ZuulException) error).getErrorCause() : "UNKNOWN_CAUSE");
            }

        }

        if (context.debugRequest()) {
            Debug.getRequestDebug(context).forEach(s -> log.info("REQ_DEBUG: " + s));
            Debug.getRoutingDebug(context).forEach(s -> log.info("ZUUL_DEBUG: " + s));
        }

        log.info("xhp debug outbound filter....");

        return response;
    }

    @Override
    public int filterOrder() {
        return 999;
    }

    @Override
    public boolean shouldFilter(HttpResponseMessage msg) {
        return true;
    }
}
