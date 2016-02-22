package sp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {}

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        return paths
                .stream()
                .map(Paths::get)
                .flatMap(path -> {
                    try {
                        return Files.lines(path);
                    } catch (IOException _) {
                        return Stream.empty();
                    }
                })
                .filter(str -> str.contains(sequence))
                        .collect(Collectors.toList());
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.

    private static final Random RANDOM = new Random();
    private static final int ITERATIONS = 1000000;
    private static final double RADIUS = 0.5;

    public static double piDividedBy4() {
        return Stream
                .generate(() -> Math.pow(RANDOM.nextDouble() - RADIUS, 2) + Math.pow(RANDOM.nextDouble() - RADIUS, 2))
                                                                              // x, y \in [-0.5; 0.5]; l = x^2 + y^2
                .limit(ITERATIONS)
                .filter(l -> l <= Math.pow(RADIUS, 2))                        // (x, y) \in circle <=> x^2 + y^2 <= r^2
                .count() * 1.0 /* cast to double */ / ITERATIONS;
    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions
                .entrySet()
                .stream()
                .collect(Collectors.maxBy(Comparator.comparing(
                        entry -> entry.getValue()
                                .stream()
                                .mapToInt(String::length)
                                .sum())))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders
                .stream()
                .flatMap(orderList -> orderList.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)));
    }
}
