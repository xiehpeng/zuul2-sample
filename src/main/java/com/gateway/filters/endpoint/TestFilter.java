package com.gateway.filters.endpoint;

import com.gateway.utils.HttpClientUtils;
import com.netflix.zuul.filters.http.HttpAsyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * @Description: TODO
 * @Author: xie.huanpeng
 * @Date: 2018/11/28 13:46
 */
public class TestFilter extends HttpAsyncEndpoint {

    private static final Logger log = LoggerFactory.getLogger(TestFilter.class);

    public HttpResponseMessage apply(HttpRequestMessage input) {
        String data = "";
        try {
            StringBuilder url = new StringBuilder("http://10.112.53.90:9988/hello");
            Request request = Request.Get(url.toString());
            data = request.execute().returnContent().asString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpResponseMessage resp = new HttpResponseMessageImpl(input.getContext(), input, 200);
        resp.setBodyAsText(data);
        StatusCategoryUtils.setStatusCategory(input.getContext(), ZuulStatusCategory.SUCCESS);
        return resp;
    }

    @Override
    public Observable<HttpResponseMessage> applyAsync(HttpRequestMessage input) {
        //被观察者
        Observable respOb = Observable.create((OnSubscribe<HttpResponseMessage>) subscriber -> {
            HttpResponseMessage resp = new HttpResponseMessageImpl(input.getContext(), input, 200);
            try {
                resp.setBodyAsText(HttpClientUtils.get("https://www.baidu.com"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            StatusCategoryUtils.setStatusCategory(input.getContext(), ZuulStatusCategory.SUCCESS);
            subscriber.onNext(resp);
            subscriber.onCompleted();
        });
        return respOb;
    }
}
