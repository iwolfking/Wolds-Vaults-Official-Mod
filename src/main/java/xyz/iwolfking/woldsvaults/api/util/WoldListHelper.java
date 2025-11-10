package xyz.iwolfking.woldsvaults.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WoldListHelper {

    public static <T> List<List<T>> getBatchedList(List<T> collection, int batchSize) {
        return IntStream.iterate(0, i -> i < collection.size(), i -> i + batchSize)
                .mapToObj(i -> collection.subList(i, Math.min(i + batchSize, collection.size())))
                .collect(Collectors.toList());
    }

    public static <T> List<List<T>> getEvenlySplitList(List<T> objs, final int N) {
        return new ArrayList<>(IntStream.range(0, objs.size()).boxed().collect(
                Collectors.groupingBy(e->e%N,Collectors.mapping(e->objs.get(e), Collectors.toList())
                )).values());
    }
}
