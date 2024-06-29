package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;

public record StlFace(
    Vector3 normal,
    Vector3 v1,
    Vector3 v2,
    Vector3 v3
) {
}
