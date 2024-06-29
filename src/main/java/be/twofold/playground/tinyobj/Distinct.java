package be.twofold.playground.tinyobj;

import java.util.*;

record Distinct<T>(List<T> uniqueValues, int[] translation) {
    public static <T> Distinct<T> distinct(List<T> list) {
        List<Integer> translation = new ArrayList<>();
        Map<T, Integer> distinctValues = new LinkedHashMap<>();
        for (T value : list) {
            int dst;
            if (distinctValues.putIfAbsent(value, distinctValues.size()) != null) {
                dst = distinctValues.get(value);
            } else {
                dst = distinctValues.size() - 1;
            }
            translation.add(dst);
        }

        return new Distinct<>(List.copyOf(distinctValues.keySet()), toArray(translation));
    }

    private static int[] toArray(List<? extends Number> list) {
        int length = list.size();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = list.get(i).intValue();
        }
        return array;
    }

    @Override
    public String toString() {
        return "Distinct(" +
            uniqueValues.size() + " distinct values, " +
            translation.length + " translations" +
            ")";
    }
}
