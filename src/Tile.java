import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.imageio.ImageIO;
import jwblangley.difference.DifferenceFunction;

public class Tile {

  private final int size;
  private Color[][] subtiles;

  private final boolean isNull;

  private Tile(int size, BufferedImage image, boolean isNull) {
    this.size = size;
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

  public static Tile ofImageFile(int size, File imageFile) throws IOException {
    BufferedImage image = ImageIO.read(imageFile);
    if (image == null) {
      throw new UnsupportedEncodingException("Cannot read file: " + imageFile.getAbsolutePath());
    }
    return new Tile(size, image, false);
  }

  public static Tile nullTile() {
    return new Tile(-1, null, true);
  }

  public Color[][] getSubtiles() {
    return subtiles;
  }

  public boolean isNull() {
    return isNull;
  }

  private Color[][] calculateSubtiles(BufferedImage image) throws IOException {

    // Crop to square (top left corner)
    int minDimension = image.getWidth() < image.getHeight() ? image.getWidth() : image.getHeight();
    image = image.getSubimage(0, 0, minDimension, minDimension);

    Color[][] subtiles = new Color[size][size];

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
    assert !a.isNull && !b.isNull : "Cannot calculate the difference between null tiles";

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
