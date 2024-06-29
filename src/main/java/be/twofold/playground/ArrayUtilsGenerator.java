package be.twofold.playground;

import java.util.*;
import java.util.function.*;

public final class ArrayUtilsGenerator {
    private record GenType(String primitive, String boxed) {
    }

    private static final List<GenType> PrimitiveTypes = List.of(
        new GenType("boolean", "Boolean"),
        new GenType("byte", "Byte"),
        new GenType("char", "Character"),
        new GenType("double", "Double"),
        new GenType("float", "Float"),
        new GenType("int", "Integer"),
        new GenType("long", "Long"),
        new GenType("short", "Short")
    );

    public static void main(String[] args) {
        System.out.println(new ArrayUtilsGenerator().generate());
    }

    private String generate() {
        return new Generator()
            .println("package be.twofold;")
            .println()
            .println("import java.util.*;")
            .println()
            .println("public final class ArrayUtils {")
            .println()
            .indent(this::printMethods)
            .println("}")
            .toString();
    }

    private void printMethods(Generator generator) {
        printConstructor(generator);
        print(generator, this::printContains);
        print(generator, this::printIndexOf);
        print(generator, this::printIndexOfFull);
    }

    private void print(Generator generator, BiConsumer<Generator, GenType> consumer) {
        for (GenType type : PrimitiveTypes) {
            consumer.accept(generator, type);
            generator.println();
        }
    }

    private void printConstructor(Generator generator) {
        generator
            .println("private ArrayUtils() {")
            .indent(g -> g.println("throw new UnsupportedOperationException();"))
            .println("}");
    }

    private void printContains(Generator generator, GenType type) {
        generator
            .println("/**")
            .println(" * Check if the value is present in the array.")
            .println(" *")
            .println(" * @param array An array of values")
            .println(" * @param value A value to find")
            .println(" * @return {@code true} if the value is present")
            .println(" */")
            .printf("public static boolean contains(%s[] array, %s value) {", type.primitive(), type.primitive())
            .indent(g -> g.println("return indexOf(array, value) != -1;"))
            .println("}");
    }

    private void printIndexOf(Generator generator, GenType type) {
        generator
            .println("/**")
            .println(" * Finds the index of the value in the array.")
            .println(" * <p>")
            .println(" * If the value does not appear, {@code -1} is returned.")
            .println(" *")
            .println(" * @param array The array to search through")
            .println(" * @param value The value to find")
            .println(" * @return The index, or {@code -1} if not found")
            .println(" */")
            .printf("public static int indexOf(%s[] array, %s value) {", type.primitive(), type.primitive())
            .indent(g -> g.println("return indexOf(array, value, 0, array.length);"))
            .println("}");
    }

    private void printIndexOfFull(Generator generator, GenType type) {
        generator
            .println("/**")
            .println(" * Finds the index of the value in the array, in the specified range.")
            .println(" * <p>")
            .println(" * If the value does not appear, {@code -1} is returned.")
            .println(" *")
            .println(" * @param array     The array to search through")
            .println(" * @param fromIndex The index to start searching at")
            .println(" * @param toIndex   The index to end searching at")
            .println(" * @param value     The value to find")
            .println(" * @return The index, or {@code -1} if not found")
            .println(" */")
            .printf("public static int indexOf(%s[] array, %s value, int fromIndex, int toIndex) {", type.primitive(), type.primitive())
            .indent(g -> g
                .println("Check.fromToIndex(fromIndex, toIndex, array.length);")
                .println()
                .println("for (int i = fromIndex; i < toIndex; i++) {")
                .indent(gg -> gg
                    .printf("if (%s) {", equals(type, "array[i]", "value"))
                    .indent(ggg -> ggg.println("return i;"))
                    .println("}"))
                .println("}")
                .println("return -1;"))
            .println("}");
    }

    private void printLastIndexOf(Generator generator, GenType type) {
        generator
            .println("/**")
            .println(" * Finds the last index of the value in the array.")
            .println(" * <p>")
            .println(" * If the value does not appear, {@code -1} is returned.")
            .println(" *")
            .println(" * @param array The array to search through, from the end")
            .println(" * @param value The value to find")
            .println(" * @return The index, or {@code -1} if not found")
            .println(" */")
            .printf("public static int lastIndexOf(%s[] array, %s value) {", type.primitive(), type.primitive())
            .indent(g -> g.println("return lastIndexOf(array, value, 0, array.length);"))
            .println("}");
    }

    private String equals(GenType type, String left, String right) {
        return switch (type.primitive()) {
            case "float" -> String.format("Float.compare(%s, %s) == 0", left, right);
            case "double" -> String.format("Double.compare(%s, %s) == 0", left, right);
            default -> String.format("%s == %s", left, right);
        };
    }

    private static final class Generator {
        private final StringBuilder builder;
        private final int indent;

        public Generator() {
            this(new StringBuilder(), 0);
        }

        private Generator(StringBuilder builder, int indent) {
            this.builder = builder;
            this.indent = indent;
        }

        public Generator printf(String format, Object... args) {
            printIndent();
            builder.append(String.format(format, args));
            printNewLine();
            return this;
        }

        public Generator println(String line) {
            printIndent();
            builder.append(line);
            printNewLine();
            return this;
        }

        public Generator println() {
            printNewLine();
            return this;
        }

        public Generator indent(Consumer<Generator> consumer) {
            consumer.accept(new Generator(builder, indent + 1));
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

        private void printIndent() {
            builder.append("    ".repeat(Math.max(0, indent)));
        }

        private void printNewLine() {
            builder.append(System.lineSeparator());
        }
    }
}
