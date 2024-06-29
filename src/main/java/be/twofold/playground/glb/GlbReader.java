package be.twofold.playground.glb;

import be.twofold.playground.common.*;
import be.twofold.playground.glb.model.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

public final class GlbReader implements AutoCloseable {

    private static final ObjectMapper Mapper = new ObjectMapper()
        .findAndRegisterModules();

    private final SeekableByteChannel channel;

    public GlbReader(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public void read() throws IOException {
        GlbHeader header = readHeader();
        System.out.println("header = " + header);

        // Read chunks
        while (channel.position() < header.length()) {
            GlbChunk chunk = readChunk();
            System.out.println("chunk = " + chunk);
        }
    }

    private GlbHeader readHeader() throws IOException {
        ByteBuffer buffer = readBuffer(12);
        int magic = buffer.getInt();
        if (magic != 0x46546C67) {
            throw new IOException("Invalid magic");
        }
        int version = buffer.getInt();
        if (version != 2) {
            throw new IOException("Invalid version");
        }
        int length = buffer.getInt();
        if (length < 20) {
            throw new IOException("Invalid length");
        }
        return new GlbHeader(magic, version, length);
    }

    private List<Pair<Integer, Integer>> images;
    private List<Pair<Integer, Integer>> meshes;

    private GlbChunk readChunk() throws IOException {
        ByteBuffer buffer = readBuffer(8);
        int length = buffer.getInt();
        GlbChunkType type = GlbChunkType.fromCode(buffer.getInt());
        ByteBuffer data = readBuffer(length);
        if (type == GlbChunkType.JSON) {
            GlTFSchema schema = Mapper.readValue(data.array(), GlTFSchema.class);
            meshes = dumpMeshes(schema);
            // images = dumpImages(schema);
            System.out.println(schema.hashCode());
        }
        if (type == GlbChunkType.BIN) {
            byte[] array = data.array();
            for (Pair<Integer, Integer> mesh : meshes) {
                byte[] imageBytes = Arrays.copyOfRange(array, mesh.first(), mesh.first() + mesh.second());
                Files.write(Paths.get("D:\\Jan\\Desktop\\Imp-" + mesh.first() + ".bin"), imageBytes);
            }
        }
        return new GlbChunk(length, type, data);
    }

    private List<Pair<Integer, Integer>> dumpMeshes(GlTFSchema schema) {
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<>();
        for (MeshSchema mesh : schema.getMeshes()) {
            int joints = mesh.getPrimitives().get(0).getAttributes().getAdditionalProperties().get("JOINTS_0");
            System.out.println("joints = " + joints);
            AccessorSchema accessor = schema.getAccessors().get(joints);
            BufferViewSchema bufferView = schema.getBufferViews().get((Integer) accessor.getBufferView());
            result.add(new Pair<>(bufferView.getByteOffset(), bufferView.getByteLength()));
        }
        return result;
    }

    private List<Pair<Integer, Integer>> dumpImages(GlTFSchema schema) {
        List<Pair<Integer, Integer>> images = new ArrayList<>();
        for (ImageSchema image : schema.getImages()) {
            int bufferView = (Integer) image.getBufferView();
            System.out.println("image.getName() = " + image.getName());
            System.out.println("image.getMimeType() = " + image.getMimeType());
            BufferViewSchema bufferViewSchema = schema.getBufferViews().get(bufferView);
            int offset = bufferViewSchema.getByteOffset();
            int length = bufferViewSchema.getByteLength();
            images.add(new Pair<>(offset, length));

            System.out.println("Image at " + offset + " with length " + length);
        }
        return images;
    }

    private ByteBuffer readBuffer(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        channel.read(buffer);
        buffer.flip();
        return buffer;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

}
