package be.twofold.playground;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class Tables {

    public static void main(String[] args) {
        Map<String, List<Col>> cols = readCols();
        for (Map.Entry<String, List<Col>> entry : cols.entrySet()) {
            try (PrintStream out = new PrintStream("target/" + uc(entry.getKey()) + ".java")) {
                generatePojo(out, entry.getKey(), entry.getValue());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static void generatePojo(PrintStream out, String tableName, List<Col> cols) {
        out.println("@javax.persistence.Entity");
        out.println("@javax.persistence.Table(name = \"" + tableName + "\")");
        out.println("public class " + uc(tableName) + " {");
        out.println();
        for (Col col : cols) {
            if (col.column.equals("id")) {
                out.println("\t@javax.persistence.Id");
            }
            out.printf("\t@javax.persistence.Column(name = \"%s\"%s)%n", col.column, col.nullable ? "" : ", nullable = false");
            out.printf("\tprivate %s %s;%n", convertType(col.type, col.nullable), col.column);
            out.println();
        }
        for (Col col : cols) {
            out.println("\tpublic " + convertType(col.type, col.nullable) + " get" + uc(col.column) + "() {");
            out.println("\t\treturn " + col.column + ";");
            out.println("\t}");
            out.println();
            out.println("\tpublic void set" + uc(col.column) + "(" + convertType(col.type, col.nullable) + " " + col.column + ") {");
            out.println("\t\tthis." + col.column + " = " + col.column + ";");
            out.println("\t}");
            out.println();
        }
        out.println("}");
        out.println();
    }

    private static String convertType(String type, boolean nullable) {
        switch (type) {
            case "character":
            case "character varying":
                return "String";
            case "date":
                return "java.time.LocalDate";
            case "double precision":
                return nullable ? "Double" : "double";
            case "integer":
                return nullable ? "Integer" : "int";
            case "smallint":
                return nullable ? "Short" : "short";
            case "timestamp without time zone":
                return "java.time.Instant";
            default:
                throw new UnsupportedOperationException(type);
        }
    }

    private static String uc(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static Map<String, List<Col>> readCols() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Tables.class.getResourceAsStream("/tabledefs.txt")))) {
            Map<String, List<Col>> result = reader.lines()
                .map(Tables::parseColDef)
                .collect(Collectors.groupingBy(col -> col.table));
            return new TreeMap<>(result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Col parseColDef(String s) {
        String[] split = s.split(",");
        return new Col(
            split[0],
            split[1],
            split[2],
            "YES".equals(split[3]),
            split.length == 5 ? Integer.parseInt(split[4]) : null
        );
    }

    static final class Col {
        final String table;
        final String column;
        final String type;
        final boolean nullable;
        final Integer length;

        Col(String table, String column, String type, boolean nullable, Integer length) {
            this.table = table;
            this.column = column;
            this.type = type;
            this.nullable = nullable;
            this.length = length;
        }
    }

}
