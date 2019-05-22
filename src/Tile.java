import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import jwblangley.difference.DifferenceFunction;

public class Tile {

  private final int size;
  private Color[][] subtiles;

  private final File imageFile;

  private Tile(int size, File imageFile) {
    this.size = size;
    this.imageFile = imageFile;

    try {
      subtiles = calcualteSubtiles();
    } catch (IOException e) {
      // TODO: Handle exception
      e.printStackTrace();
    }
  }

  public static Tile ofImage(int size, File imageFile) {
    return new Tile(size, imageFile);
  }

  public Color[][] getSubtiles() {
    return subtiles;
  }

  private Color[][] calcualteSubtiles() throws IOException {
    Color[][] subtiles = new Color[size][size];

    BufferedImage image = ImageIO.read(imageFile);

    int splitWidth = image.getWidth() / size;
    int splitHeight = image.getHeight() / size;

    for (int j = 0; j < size; j++) {
      for (int i = 0; i < size; i++) {

        int[] tilePixels = new int[splitWidth * splitHeight];
        image.getRGB(i * splitWidth, j * splitHeight,
            splitWidth, splitHeight,
            tilePixels, 0, splitWidth);
        subtiles[i][j] = averageColor(tilePixels);
      }
    }
    return subtiles;
  }

  private Color averageColor(int[] colors) {
    int totalRed = 0;
    int totalGreen = 0;
    int totalBlue = 0;
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

  private static Long delta(Tile a, Tile b) {
    assert a.size == b.size
        : "Difference between two tiles is only valid for tiles of the same dimensions";

    long totalDelta = 0;

    for (int i = 0; i < a.size; i++) {
      for (int j = 0; j < a.size; j++) {
        int redDelta = a.subtiles[i][j].getRed() < b.subtiles[i][j].getRed()
            ? b.subtiles[i][j].getRed() - a.subtiles[i][j].getRed()
            : a.subtiles[i][j].getRed() - b.subtiles[i][j].getRed();
        int greenDelta = a.subtiles[i][j].getGreen() < b.subtiles[i][j].getGreen()
            ? b.subtiles[i][j].getGreen() - a.subtiles[i][j].getGreen()
            : a.subtiles[i][j].getGreen() - b.subtiles[i][j].getGreen();
        int blueDelta = a.subtiles[i][j].getBlue() < b.subtiles[i][j].getBlue()
            ? b.subtiles[i][j].getBlue() - a.subtiles[i][j].getBlue()
            : a.subtiles[i][j].getBlue() - b.subtiles[i][j].getBlue();
        totalDelta += redDelta + greenDelta + blueDelta;
      }
    }

    return totalDelta;
  }

  public static DifferenceFunction<Tile> differenceFunction = Tile::delta;
}
