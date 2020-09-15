package com.github.mrlawrenc.a_reactive_programming.one;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author : MrLawrenc
 * @date : 2020/5/30 18:37
 * @description : TODO
 *
 *
 */
public class Java8Stream {

    public static void main(String[] args) {
        List<String> list = IntStream.range(0, 1000)
                .mapToObj(i -> "hello java8" + i)
                .filter(s -> !s.contains("8"))
                .collect(toList());
    }
}