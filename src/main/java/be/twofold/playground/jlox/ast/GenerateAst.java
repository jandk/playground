package be.twofold.playground.jlox.ast;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", List.of(
            "Binary   : Expr left, Token operator, Expr right",
            "Call     : Expr callee, Token paren, List<Expr> arguments",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Logical  : Expr left, Token operator, Expr right",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        Files.createDirectories(Paths.get(outputDir));
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {

            writer.println("package be.twofold.jlox.ast;");
            writer.println();
            writer.println("import be.twofold.jlox.parser.*;");
            writer.println();
            writer.println("import java.util.*;");
            writer.println();
            writer.println("public abstract class " + baseName + " {");

            defineVisitor(writer, baseName, types);

            // The AST classes.
            for (String type : types) {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            // The base accept() method.
            writer.println();
            writer.println("    public abstract <R> R accept(Visitor<R> visitor);");

            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    public static class " + className + " extends " + baseName + " {");

        // Constructor.
        writer.println("        public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Visitor pattern.
        writer.println();
        writer.println("        @Override");
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        public final " + field + ";");
        }

        writer.println("    }");
    }
}
