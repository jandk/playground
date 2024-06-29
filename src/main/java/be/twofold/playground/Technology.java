package be.twofold.playground;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Technology {
    public static void main(String[] args) throws IOException {
        JsonElement json;
        try (Reader reader = new InputStreamReader(Technology.class.getResourceAsStream("/technology.json"))) {
            json = JsonParser.parseReader(reader);
        }
        JsonObject object = json.getAsJsonObject();
        List<Tech> techs = object.entrySet().stream()
            .map(e -> e.getValue().getAsJsonObject())
            .map(Technology::readTech)
            .toList();

        Graph<String> graph = new Graph<>();
        for (Tech tech : techs) {
            for (String prerequisite : tech.prerequisites()) {
                graph.addEdge(prerequisite, tech.name());
            }
        }

        System.out.println(graph.toDot());
    }

    private static Tech readTech(JsonObject o) {
        List<String> prerequisites = Optional.ofNullable(o.get("prerequisites"))
            .map(e -> StreamSupport.stream(e.getAsJsonArray().spliterator(), false))
            .stream().flatMap(Function.identity())
            .map(JsonElement::getAsString)
            .toList();

        return new Tech(
            o.get("name").getAsString(),
            prerequisites
        );
    }

    private static JsonElement parseJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    private record Tech(String name, List<String> prerequisites) {
    }
}
