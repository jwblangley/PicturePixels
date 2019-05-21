import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Tile {

  private final int size;
  private Color[][] tiles;

  private final File imageFile;

  public Tile(int size, File imageFile) {
    this.size = size;
    this.imageFile = imageFile;

    try {
      tiles = calcualtePixels();
    } catch (IOException e) {
      // TODO: Handle exception
      e.printStackTrace();
    }
  }

  public Color[][] getTiles() {
    return tiles;
  }

  private Color[][] calcualtePixels() throws IOException {
    Color[][] tiles = new Color[size][size];

    BufferedImage image = ImageIO.read(imageFile);

    int splitWidth = image.getWidth() / size;
    int splitHeight = image.getHeight() / size;

    for (int j = 0; j < size; j++) {
      for (int i = 0; i < size; i++) {

        int[] tilePixels = new int[splitWidth * splitHeight];
        image.getRGB(i * splitWidth, j * splitHeight, splitWidth, splitHeight, tilePixels, 0, splitWidth);
        tiles[i][j] = averageColor(tilePixels);
      }
    }
    return tiles;
  }

  private final Color averageColor(int[] colors) {
    int totalRed=0;
    int totalGreen=0;
    int totalBlue=0;
    for (int rgb : colors) {
      Color c = new Color(rgb);

      totalRed += c.getRed();
      totalGreen += c.getGreen();
      totalBlue += c.getBlue();
    }

    totalRed /= colors.length;
    totalGreen /= colors.length;
    totalBlue /= colors.length;

    return new Color(totalRed, totalGreen, totalBlue);
  }
}
