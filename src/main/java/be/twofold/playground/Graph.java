package be.twofold.playground;

import lombok.*;

import java.util.*;
import java.util.stream.*;

final class Graph<T> {
    private final Map<T, Set<T>> graph = new HashMap<>();

    public void addEdge(T source, T target) {
        graph
            .computeIfAbsent(target, __ -> new HashSet<>());

        graph
            .computeIfAbsent(source, __ -> new HashSet<>())
            .add(target);
    }

    public void removeNode(T node) {
        graph.remove(node);
        removeIn(node);
    }

    public void removeIn(T node) {
        graph.values().forEach(s -> s.remove(node));
    }

    public void removeAll(T node) {
        for (T outNode : List.copyOf(adj(node))) {
            removeAll(outNode);
        }
        removeNode(node);
    }

    public Set<T> adj(T source) {
        return graph.getOrDefault(source, Set.of());
    }

    public Set<T> nodes() {
        return graph.keySet();
    }

    public Stream<Edge<T>> edges() {
        return nodes().stream()
            .flatMap(src -> adj(src).stream().map(tgt -> new Edge<>(src, tgt)));
    }

    public Graph<T> reverse() {
        Graph<T> result = new Graph<>();
        edges().forEach(e -> result.addEdge(e.target, e.source));
        return result;
    }

    public String toDot() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph Factorio {\n");
        builder.append("  rankdir = LR\n");

        for (T source : nodes()) {
            // builder.append("  ").append(source).append("\n");
            for (T target : adj(source)) {
                builder.append("  \"").append(source).append("\" -> \"").append(target).append("\"\n");
            }
        }
        builder.append("}\n");
        return builder.toString();
    }

    @Value
    public static final class Edge<T> {
        private final T source;
        private final T target;
    }
}
