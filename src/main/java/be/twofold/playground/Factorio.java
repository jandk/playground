package be.twofold.playground;

import com.fasterxml.jackson.databind.*;
import lombok.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class Factorio {

    private static final ObjectMapper ObjectMapper;

    static {
        ObjectMapper = new ObjectMapper();
        ObjectMapper.findAndRegisterModules();
        ObjectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static void main(String[] args) {
        FactorioData data = loadRecipes();
        Graph<String> graph = toGraph(data.getRecipes());
        Map<String, Map<String, Set<String>>> grouping = toGrouping(data.getItems());

        // printGrouping(grouping);

        // Remove the inputs to the circuits
        Stream.of("advanced-circuit", "electronic-circuit")
            .forEach(graph::removeIn);

        // Remove inputs we dont want to use
        Stream.of("battery", "explosives", "light-oil", "plastic-bar", "raw-fish", "solid-fuel", "sulfur", "sulfuric-acid", "uranium-235", "uranium-238", "wood")
            .forEach(graph::removeAll);

        // Remove non hub stuff
        Stream.of(
            "automation-science-pack", "logistic-science-pack", "military-science-pack", "production-science-pack",
            "loader", "fast-loader", "express-loader",
            "electric-energy-interface"
        ).forEach(graph::removeNode);

        // Remove combat items
        grouping.get("combat").entrySet().stream()
            .filter(e -> !"defensive-structure".equals(e.getKey()))
            .flatMap(e -> e.getValue().stream())
            .forEach(graph::removeNode);

        // Now we filter to what we have in our hub part
        Set<String> removals = new HashSet<>(Arrays.asList(
            "advanced-circuit",
            "coal",
            "copper-plate",
            "electronic-circuit",
            "iron-ore",
            "iron-plate",
            "lubricant",
            "pipe",
            "steel-plate",
            "stone",
            "stone-brick",
            "water"
        ));

        removals.remove("advanced-circuit");
//        removals.remove("coal");
        removals.remove("copper-plate");
        removals.remove("electronic-circuit");
//        removals.remove("iron-ore");
        removals.remove("iron-plate");
//        removals.remove("lubricant");
//        removals.remove("pipe");
        removals.remove("steel-plate");
//        removals.remove("stone");
//        removals.remove("stone-brick");
//        removals.remove("water");

        removals.forEach(graph::removeAll);

        // Remove hub 1 products
        Stream.of(
            "burner-inserter",
            "transport-belt",
            "inserter",
            "repair-pack",
            "electric-mining-drill",
            "radar"
        ).forEach(graph::removeAll);

        // Remove hub 2 products
//        Stream.of(
//            "copper-cable",
//            "iron-stick",
//            "steel-chest"
//        ).forEach(graph::removeAll);

        System.out.println(graph.toDot());

        Set<String> targets = graph.edges()
            .map(Graph.Edge::getTarget)
            .collect(Collectors.toSet());

        Set<String> sources = new HashSet<>(graph.nodes());
        sources.removeAll(targets);

        System.out.println("sources = " + sources);
    }

    private static void printGrouping(Map<String, Map<String, Set<String>>> grouping) {
        for (Map.Entry<String, Map<String, Set<String>>> groupEntry : grouping.entrySet()) {
            System.out.println(groupEntry.getKey());
            for (Map.Entry<String, Set<String>> subgroupEntry : groupEntry.getValue().entrySet()) {
                System.out.println("  " + subgroupEntry.getKey());
                for (String item : subgroupEntry.getValue()) {
                    System.out.println("    " + item);
                }
            }
        }
    }

    private static Map<String, Map<String, Set<String>>> toGrouping(Map<String, Item> items) {
        Map<String, Map<String, Set<String>>> result = new TreeMap<>();
        items.forEach((name, item) -> {
            result
                .computeIfAbsent(item.getGroup(), __ -> new TreeMap<>())
                .computeIfAbsent(item.getSubgroup(), __ -> new TreeSet<>())
                .add(name);
        });
        return result;
    }

    private static Graph<String> toGraph(Map<String, Recipe> recipes) {
        Graph<String> graph = new Graph<>();
        for (Map.Entry<String, Recipe> entry : recipes.entrySet()) {
            Recipe recipe = entry.getValue();
            if (recipe.getCategory().contains("crafting")) {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    graph.addEdge(ingredient.getName(), entry.getKey());
                }
            }
        }
        return graph;
    }

    private static FactorioData loadRecipes() {
        try (InputStream inputStream = Factorio.class.getResourceAsStream("/vanilla-1.1.19.json")) {
            return ObjectMapper.readValue(inputStream, FactorioData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Value
    private static final class FactorioData {
        Map<String, Item> items;
        Map<String, Recipe> recipes;
    }

    @Value
    private static final class Item {
        private final String group;
        private final String subgroup;
        private final String type;
    }

    @Value
    private static final class Recipe {
        private final String category;
        private final List<Ingredient> ingredients;
    }

    @Value
    private static final class Ingredient {
        private final String name;
        private final int amount;
    }

}
