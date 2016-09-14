package co.kukurin.testing;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MockFactory {

    private static <T> List<T> createListOfSize(int size, IntFunction<T> elementCreatorFromIndex) {
        return IntStream
                .range(0, size)
                .mapToObj(elementCreatorFromIndex)
                .collect(Collectors.toList());
    }

}
