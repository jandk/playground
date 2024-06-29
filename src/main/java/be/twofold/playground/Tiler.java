package be.twofold.playground;

import lombok.*;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Tiler {
    public static void main(String[] args) throws IOException {
        Path constantRoot = Paths.get("D:\\Games\\SteamLibrary\\steamapps\\common\\Factorio\\data\\core\\graphics\\icons\\technology\\constants");
//        Map<String, Image> constants = Files.list(constantRoot)
//            .filter(path -> path.getFileName().toString().endsWith(".png"))
//            .collect(Collectors.toMap(path -> path.getFileName().toString(), Tiler::readMipmappedImage));

        Path technologyRoot = Paths.get("D:\\Games\\SteamLibrary\\steamapps\\common\\Factorio\\data\\base\\graphics\\technology");
        List<Path> technologies = Files.list(technologyRoot)
            .filter(path -> path.getFileName().toString().endsWith(".png"))
            .sorted().toList();

        BufferedImage sheet = new BufferedImage(13 * 128, 10 * 128, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = sheet.getGraphics();
        for (int i = 0; i < technologies.size(); i++) {
            Path technology = technologies.get(i);
            Image image = readMipmappedImage(technology);
            g.drawImage(image, (i % 13) * 128, (i / 13) * 128, null);
        }

        ImageIO.write(sheet, "png", new File("C:\\temp\\sheet.png"));
    }

    @SneakyThrows
    private static Image readMipmappedImage(Path path) {
        BufferedImage image = ImageIO.read(path.toFile());
        return image.getSubimage(256, 0, 128, 128);
    }
}
