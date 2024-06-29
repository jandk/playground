package be.twofold.playground.tinyobj;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ObjCompacter {

    private Distinct<Vector3> distinctVertices;
    private Distinct<Vector2> distinctUVs;
    private Distinct<Vector3> distinctNormals;

    public void read(Path src) throws IOException {
        List<Vector3> vertices = new ArrayList<>();
        List<Vector2> uvs = new ArrayList<>();
        List<Vector3> normals = new ArrayList<>();

        vertices.add(Vector3.Zero);
        uvs.add(Vector2.Zero);
        normals.add(Vector3.Zero);

        try (BufferedReader reader = Files.newBufferedReader(src)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] split = line.strip().split(" +");
                switch (split[0]) {
                    case "v" -> vertices.add(readVector3(split));
                    case "vt" -> uvs.add(readVector2(split));
                    case "vn" -> normals.add(readVector3(split));
                }
            }
        }

        distinctVertices = Distinct.distinct(vertices);
        distinctUVs = Distinct.distinct(uvs);
        distinctNormals = Distinct.distinct(normals);
    }

    public void copy(Path src, Path dst) throws IOException {
        try (
            BufferedReader reader = Files.newBufferedReader(src);
            BufferedWriter writer = Files.newBufferedWriter(dst)
        ) {
            boolean firstV = true;
            boolean firstVT = true;
            boolean firstVN = true;

            int[] vLookup = distinctVertices.translation();
            int[] vtLookup = distinctUVs.translation();
            int[] vnLookup = distinctNormals.translation();

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                String[] split = line.strip().split(" +");
                switch (split[0]) {
                    case "v" -> {
                        if (firstV) {
                            writeVertices(writer);
                            firstV = false;
                        }
                    }
                    case "vt" -> {
                        if (firstVT) {
                            writeUVs(writer);
                            firstVT = false;
                        }
                    }
                    case "vn" -> {
                        if (firstVN) {
                            writeNormals(writer);
                            firstVN = false;
                        }
                    }
                    case "f" -> {
                        writer.write("f ");
                        for (int i = 1; i < split.length; i++) {
                            String[] splitFace = split[i].split("/");
                            int v = Integer.parseInt(splitFace[0]);
                            int vt = Integer.parseInt(splitFace[1]);
                            int vn = Integer.parseInt(splitFace[2]);

                            writer.write(Integer.toString(vLookup[v]));
                            writer.write('/');
                            writer.write(Integer.toString(vtLookup[vt]));
                            writer.write('/');
                            writer.write(Integer.toString(vnLookup[vn]));
                            if (i != split.length - 1) {
                                writer.write(' ');
                            }
                        }
                        writer.write(System.lineSeparator());
                    }
                    default -> {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                }
            }
        }
    }

    private void writeVertices(BufferedWriter writer) throws IOException {
        List<Vector3> uniqueValues = distinctVertices.uniqueValues();
        writeVector3s(uniqueValues, writer, "v ");
    }

    private void writeUVs(BufferedWriter writer) throws IOException {
        List<Vector2> uniqueValues = distinctUVs.uniqueValues();
        writeVector2s(uniqueValues, writer, "vt ");
    }

    private void writeNormals(BufferedWriter writer) throws IOException {
        List<Vector3> uniqueValues = distinctNormals.uniqueValues();
        writeVector3s(uniqueValues, writer, "vn ");
    }

    private void writeVector2s(List<Vector2> uniqueValues, BufferedWriter writer, String prefix) throws IOException {
        for (int i = 1; i < uniqueValues.size(); i++) {
            Vector2 value = uniqueValues.get(i);
            writer.write(prefix);
            writer.write(Float.toString(value.x()));
            writer.write(' ');
            writer.write(Float.toString(value.y()));
            writer.write(System.lineSeparator());
        }
    }

    private void writeVector3s(List<Vector3> uniqueValues, BufferedWriter writer, String prefix) throws IOException {
        for (int i = 1; i < uniqueValues.size(); i++) {
            Vector3 value = uniqueValues.get(i);
            writer.write(prefix);
            writer.write(Float.toString(value.x()));
            writer.write(' ');
            writer.write(Float.toString(value.y()));
            writer.write(' ');
            writer.write(Float.toString(value.z()));
            writer.write(System.lineSeparator());
        }
    }


    private Vector2 readVector2(String[] split) {
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        return new Vector2(x, y);
    }

    private Vector3 readVector3(String[] split) {
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float z = Float.parseFloat(split[3]);
        return new Vector3(x, y, z);
    }

    public static void main(String[] args) throws IOException {
        Path src = Paths.get("D:\\Software\\VegaReleasev1.30\\exported_files\\models\\heavy_cannon\\heavy_cannon_LOD0.obj");
        Path dst = Paths.get("D:\\Software\\VegaReleasev1.30\\exported_files\\models\\heavy_cannon\\heavy_cannon_LOD0-compact.obj");

        ObjCompacter compacter = new ObjCompacter();
        System.out.println("Reading file");
        compacter.read(src);

        System.out.println("Copying file");
        compacter.copy(src, dst);
    }

}
