package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;

import java.util.*;

public record Model(List<Vector3> vertices, List<Face> faces) {
}
