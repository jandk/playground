package be.twofold.playground.stl;

import java.util.*;

public record Stl(
    byte[] header,
    List<StlFace> faces
) {
}
