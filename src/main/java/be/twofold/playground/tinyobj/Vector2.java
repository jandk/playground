package be.twofold.playground.tinyobj;

public record Vector2(float x, float y) {
    public static final Vector2 Zero = new Vector2(0, 0);
}
