package com.github.mrlawrenc;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
@EnableDiscoveryClient
@EnableFeignClients
public class MrLawrencApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(MrLawrencApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("doc url:http://localhost/swagger-ui.html");
    }

    /**
     * 当想作为服务调用方使用时，可以开启客户端的负载均衡
     */
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RestController
    @ApiOperation("测试nacos同时作为服务提供者和服务消费者，使用feign和restTemplate调用其余服务提供者")
    public static class TestNacos {

        @Autowired
        private RestTemplate restTemplate;
        @Autowired
        private AttachmentController4Service feignService;

        @ApiOperation("测试RestTemplate")
        @GetMapping("/haloConfig/{string}")
        public String test(@PathVariable String string) {
            System.out.println("参数忽略:" + string);
            return restTemplate.getForObject("http://halo-blog-local/testConfig", String.class);
        }

        @ApiOperation("测试Feign")
        @GetMapping("/testFeign")
        public String testFeign() {
            return feignService.testConfig();
        }


    }

    @FeignClient("halo-blog-local")
    @Component
    public interface AttachmentController4Service {
        @GetMapping("/testConfig")
        String testConfig();
    }

    /**
     * 在webflux中，是不会注入HttpMessageConverter的，因此feign调用其余接口会报错，没有找到自动注入的converter
     *
     * @see org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration 注入有条件,必须不是reactive的环境
     * <p>
     * 因此这里reactive环境 手动注入一个
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }


}
