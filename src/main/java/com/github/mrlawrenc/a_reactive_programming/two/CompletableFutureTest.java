package com.github.mrlawrenc.a_reactive_programming.two;

import java.util.concurrent.CompletableFuture;

/**
 * @author : MrLawrenc
 * @date : 2020/5/30 19:17
 * @description : TODO
 */
public class CompletableFutureTest {

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> "我是逍遥")
                .thenApply(r -> r + "即将开始做饭")
                .thenApplyAsync(r -> "吃饭")
                .thenRun(() -> System.out.println());
    }
}