package be.twofold.playground.json;

import java.util.*;

public sealed interface JsonValue {
    final class JsonNull implements JsonValue {
        static final JsonNull INSTANCE = new JsonNull();

        private JsonNull() {
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof JsonNull;
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    final class JsonBoolean implements JsonValue {
        static final JsonBoolean TRUE = new JsonBoolean(true);
        static final JsonBoolean FALSE = new JsonBoolean(false);

        private final boolean value;

        JsonBoolean(boolean value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JsonBoolean other
                && value == other.value;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(value);
        }
    }

    final class JsonNumber implements JsonValue {
        private final Number value;

        JsonNumber(Number value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JsonNumber other
                && value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    final class JsonString implements JsonValue {
        private final String value;

        JsonString(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JsonString other
                && value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    final class JsonArray implements JsonValue {
        private final List<JsonValue> values;

        public JsonArray(List<JsonValue> values) {
            this.values = List.copyOf(values);
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JsonArray other
                && values.equals(other.values);
        }

        @Override
        public int hashCode() {
            return values.hashCode();
        }

        public static final class Builder {
            private final List<JsonValue> values = new ArrayList<>();

            public Builder add(JsonValue value) {
                values.add(Objects.requireNonNull(value, "value must not be null"));
                return this;
            }

            public JsonArray build() {
                return new JsonArray(values);
            }
        }
    }

    final class JsonObject implements JsonValue {
        private final Map<String, JsonValue> values;

        private JsonObject(Map<String, JsonValue> values) {
            this.values = Map.copyOf(values);
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof JsonObject other
                && values.equals(other.values);
        }

        @Override
        public int hashCode() {
            return values.hashCode();
        }

        public static final class Builder {
            private final Map<String, JsonValue> values = new HashMap<>();

            public Builder add(String key, JsonValue value) {
                Objects.requireNonNull(key, "key must not be null");
                Objects.requireNonNull(value, "value must not be null");
                if (values.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate key: " + key);
                }
                values.put(key, value);
                return this;
            }

            public JsonObject build() {
                return new JsonObject(values);
            }
        }
    }
}
