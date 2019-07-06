package com.sec;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestStream {


    @Test
    public void test() {
        Map<String, Integer> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("d", 10);
        map.put("b", 20);
        map.put("c", 5);

        System.out.println(map);

        List<Map.Entry<String, Integer>> list = map.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toList());

        System.out.println(list);
    }
}
