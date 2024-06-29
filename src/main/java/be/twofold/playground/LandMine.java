package be.twofold.playground;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import lombok.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class LandMine {
    private static final AtomicInteger Counter = new AtomicInteger();

    public static void main(String[] args) throws JsonProcessingException {
        long version = 281479273906176L;
        System.out.println(parseVersion(version));

        int gridSize = 7;
        int snap = gridSize & ~1;

        List<Entity> entities = new ArrayList<>();
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                if (((x + y) & 1) == 0) {
                    entities.add(createMine(x, y));
                }
            }
        }

        SnapToGrid snapToGrid = new SnapToGrid(snap, snap);
        Icon icon = new Icon(1, new Signal("land-mine", "item"));
        Blueprint blueprint = new Blueprint(snapToGrid, List.of(icon), entities);
        Book book = new Book("blueprint", 281479273906176L, blueprint);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper
            .writeValueAsString(book);

        System.out.println(json);
    }

    private static Entity createMine(int x, int y) {
        Position position = new Position(x + 0.5, y + 0.5);
        return new Entity(
            Counter.incrementAndGet(),
            "land-mine",
            position
        );
    }

    private static String parseVersion(long version) {
        int major = (int) ((version & 0xffff000000000000L) >>> 48);
        int minor = (int) ((version & 0x0000ffff00000000L) >>> 32);
        int patch = (int) ((version & 0x00000000ffff0000L) >>> 16);
        int build = (int) ((version & 0x000000000000ffffL));

        return major + "." + minor + "." + patch + (build != 0 ? "." + build : "");
    }

    @Value
    private static final class Book {
        private final String item;
        private final long version;
        private final Blueprint blueprint;
    }

    @Value
    private static final class Blueprint {
        @JsonProperty("snap-to-grid")
        SnapToGrid snapToGrid;
        List<Icon> icons;
        List<Entity> entities;
    }

    @Value
    private static final class SnapToGrid {
        private final int x;
        private final int y;
    }

    @Value
    private static final class Icon {
        private final int index;
        private final Signal signal;
    }

    @Value
    private static final class Signal {
        private final String name;
        private final String type;
    }

    @Value
    private static final class Entity {
        @JsonProperty("entity_number")
        private final int entityNumber;
        private final String name;
        private final Position position;
    }

    @Value
    private static final class Position {
        private final double x;
        private final double y;
    }

}
