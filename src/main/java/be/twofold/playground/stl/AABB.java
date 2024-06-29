package be.twofold.playground.stl;

import be.twofold.playground.tinyobj.*;

public record AABB(
    float minX,
    float minY,
    float minZ,
    float maxX,
    float maxY,
    float maxZ
) {
    public Vector3 center() {
        return new Vector3(
            (minX + maxX) / 2,
            (minY + maxY) / 2,
            (minZ + maxZ) / 2
        );
    }
}
