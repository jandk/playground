package be.twofold.playground.tinyobj;

public record Vector3(float x, float y, float z) {
    public static final Vector3 Zero = new Vector3(0, 0, 0);

    public Vector3(float x, float y, float z) {
        this.x = x == -0.0f ? 0.0f : x;
        this.y = y == -0.0f ? 0.0f : y;
        this.z = z == -0.0f ? 0.0f : z;
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 sub(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }
}
