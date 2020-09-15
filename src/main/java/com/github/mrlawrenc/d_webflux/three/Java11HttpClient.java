package com.github.mrlawrenc.d_webflux.three;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:52
 * @description :
 * <p>
 * {@link org.springframework.web.client.RestTemplate} 差不多
 */
public class Java11HttpClient {


    public static void main(String[] args) throws Exception {
        //HttpClient client = HttpClient.newBuilder().proxy(ProxySelector.of(InetSocketAddress.createUnresolved("host",9999))).build();
        HttpClient client = HttpClient.newBuilder().build();


        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(URI.create("http://localhost/date")).build()
                , HttpResponse.BodyHandlers.ofString());


        //阻塞任务
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(
                HttpRequest.newBuilder().uri(URI.create("http://localhost/sleep")).build()
                , HttpResponse.BodyHandlers.ofString());

        System.out.println(response + "\n" + response.body());


        //优雅一点
        future.whenComplete((r, t) -> System.out.format("listen result:%s\n", r.body()));
        //阻塞等待
        System.out.println("wait result:" + future.get().body());


    }
}