package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public final class ModelReader {

    private final Document document;

    private ModelReader(Document document) {
        this.document = document;
    }

    public static Model read(Path path) {
        try {
            Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(path.toFile());

            return new ModelReader(document).read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Model read() {
        Element root = document.getDocumentElement();
        Element resources = getSingleElement(root, "resources");
        Element object = getSingleElement(resources, "object");
        Element mesh = getSingleElement(object, "mesh");

        // Read vertices
        List<Vector3> vertices = getElements(getSingleElement(mesh, "vertices"), "vertex")
            .map(ModelReader::nodeToVector3)
            .toList();

        // Read faces
        List<Face> faces = getElements(getSingleElement(mesh, "triangles"), "triangle")
            .map(ModelReader::nodeToFace)
            .toList();

        return new Model(vertices, faces);
    }

    private static Vector3 nodeToVector3(Element element) {
        float x = Float.parseFloat(element.getAttribute("x"));
        float y = Float.parseFloat(element.getAttribute("y"));
        float z = Float.parseFloat(element.getAttribute("z"));
        return new Vector3(x, y, z);
    }

    private static Face nodeToFace(Element element) {
        int v1 = Integer.parseInt(element.getAttribute("v1"));
        int v2 = Integer.parseInt(element.getAttribute("v2"));
        int v3 = Integer.parseInt(element.getAttribute("v3"));
        return new Face(v1, v2, v3);
    }

    private static Stream<Element> getElements(Element element, String name) {
        NodeList list = element.getElementsByTagName(name);
        return IntStream.range(0, list.getLength())
            .mapToObj(list::item)
            .map(Element.class::cast);
    }

    private static Element getSingleElement(Element element, String name) {
        NodeList list = element.getElementsByTagName(name);
        if (list.getLength() != 1) {
            throw new RuntimeException("Expected 1 element with name '" + name + "', but found " + list.getLength());
        }
        return (Element) list.item(0);
    }

}
