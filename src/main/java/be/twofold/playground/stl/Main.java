package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;

import javax.xml.parsers.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException {
        Path stlPath = Path.of("D:\\Jan\\Downloads\\3dbenchy.stl");
        Stl stl = StlReader.read(stlPath);
        Model stlModel = centerAroundOrigin(stlToModel(stl));

        Path modelPath = Path.of("D:\\Jan\\Desktop\\3dbenchy.model");
        Model model = ModelReader.read(modelPath);

        Set<Vector3> stlVertices = Set.copyOf(stlModel.vertices());
        Set<Vector3> modelVertices = Set.copyOf(model.vertices());
        assert stlVertices.equals(modelVertices);

        // Index model vertices
        Map<Vector3, Integer> modelVertexIndices = new HashMap<>();
        for (Vector3 vertex : model.vertices()) {
            modelVertexIndices.put(vertex, modelVertexIndices.size());
        }

        // Convert stl model faces to model faces
        Set<Face> stlFaces = stlModel.faces().stream()
            .map(face -> new Face(
                modelVertexIndices.get(stlModel.vertices().get(face.v1())),
                modelVertexIndices.get(stlModel.vertices().get(face.v2())),
                modelVertexIndices.get(stlModel.vertices().get(face.v3()))
            ))
            .collect(Collectors.toUnmodifiableSet());

        Set<Face> modelFaces = Set.copyOf(model.faces());

        // Faces only in stl model
        Set<Face> stlOnlyFaces = stlFaces.stream()
            .filter(face -> !modelFaces.contains(face))
            .collect(Collectors.toUnmodifiableSet());

        // Faces only in model
        Set<Face> modelOnlyFaces = modelFaces.stream()
            .filter(face -> !stlFaces.contains(face))
            .collect(Collectors.toUnmodifiableSet());

        System.out.println("stlOnlyFaces = " + stlOnlyFaces);
        System.out.println("modelOnlyFaces = " + modelOnlyFaces);
    }

    private static Model stlToModel(Stl stl) {
        // Index unique vertices
        Map<Vector3, Integer> vertexIndices = new LinkedHashMap<>();
        for (StlFace face : stl.faces()) {
            vertexIndices.putIfAbsent(face.v1(), vertexIndices.size());
            vertexIndices.putIfAbsent(face.v2(), vertexIndices.size());
            vertexIndices.putIfAbsent(face.v3(), vertexIndices.size());
        }

        // Convert faces to model faces
        List<Face> faces = new ArrayList<>();
        for (StlFace face : stl.faces()) {
            int v1 = vertexIndices.get(face.v1());
            int v2 = vertexIndices.get(face.v2());
            int v3 = vertexIndices.get(face.v3());

            if (v1 != v2 && v1 != v3 && v2 != v3) {
                faces.add(new Face(v1, v2, v3));
            }
        }

        return new Model(List.copyOf(vertexIndices.keySet()), faces);
    }

    private static Model centerAroundOrigin(Model model) {
        Vector3 center = getBoundingBox(model.vertices()).center();
        List<Vector3> centeredVertices = model.vertices().stream()
            .map(vertex -> vertex.sub(center))
            .toList();
        return new Model(centeredVertices, model.faces());
    }

    private static AABB getBoundingBox(Collection<Vector3> vertices) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (Vector3 vertex : vertices) {
            minX = Math.min(minX, vertex.x());
            minY = Math.min(minY, vertex.y());
            minZ = Math.min(minZ, vertex.z());
            maxX = Math.max(maxX, vertex.x());
            maxY = Math.max(maxY, vertex.y());
            maxZ = Math.max(maxZ, vertex.z());
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

}
