package jwblangley.pictureMatching;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;
import jwblangley.difference.DifferenceFunction;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;

public class Tile {

  private final int numSubtiles;
  private Color[][] subtiles;

  private final boolean isNull;

  private File source;

  private Tile(int numSubtiles, BufferedImage image, File source, boolean isNull) {
    this.numSubtiles = numSubtiles;
    this.source = source;
    this.isNull = isNull;

    if (!isNull) {
      try {
        subtiles = calculateSubtiles(image);
      } catch (IOException e) {
        // TODO: Handle exception
        e.printStackTrace();
      }
    }
  }

  public static Tile ofImageFile(int numSubtiles, File imageFile) throws IOException {
    BufferedImage image = ImageIO.read(imageFile);
    if (image == null) {
      throw new UnsupportedEncodingException("Cannot read file: " + imageFile.getAbsolutePath());
    }
    return new Tile(numSubtiles, image, imageFile, false);
  }

  public static Tile ofBufferedImage(int numSubtiles, BufferedImage image) {
    return new Tile(numSubtiles, image, null, false);
  }

  public static Tile nullTile() {
    return new Tile(-1, null, null, true);
  }

  public Color[][] getSubtiles() {
    return subtiles;
  }

  public boolean isNull() {
    return isNull;
  }

  public File getSource() {
    return source;
  }

  private Color[][] calculateSubtiles(BufferedImage image) throws IOException {

    image = ImageUtils.cropSquare(image, CropType.CENTER);

    Color[][] subtiles = new Color[numSubtiles][numSubtiles];

    int splitWidth = image.getWidth() / numSubtiles;
    int splitHeight = image.getHeight() / numSubtiles;

    for (int j = 0; j < numSubtiles; j++) {
      for (int i = 0; i < numSubtiles; i++) {

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
    assert a.numSubtiles == b.numSubtiles
        : "Difference between two tiles is only valid for tiles of the same dimensions";
    assert !a.isNull && !b.isNull : "Cannot calculate the difference between null tiles";

    long totalDelta = 0;

    for (int i = 0; i < a.numSubtiles; i++) {
      for (int j = 0; j < a.numSubtiles; j++) {
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
