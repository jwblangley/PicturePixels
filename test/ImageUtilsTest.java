import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class ImageUtilsTest {

  @Test
  public void cropSquareTopLeftFromSquareIsSameSize() {
    BufferedImage testSquare = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.TOP_LEFT);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSquareCenterFromSquareIsSameSize() {
    BufferedImage testSquare = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.CENTER);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSquareWideTopLeftIsSquare() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.TOP_LEFT);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSquareWideCenterIsSquare() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.CENTER);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSquareTallTopLeftIsSquare() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.TOP_LEFT);

    assertTrue(result.getWidth() == result.getHeight());
  }

  @Test
  public void cropSquareTallCenterIsSquare() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.CENTER);

    assertTrue(result.getWidth() == result.getHeight());
  }

  @Test
  public void cropSquareTallTopLeftIsFullCrop() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.TOP_LEFT);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSquareTallCenterIsFullCrop() {
    BufferedImage testSquare = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage result = ImageUtils.cropSquare(testSquare, CropType.CENTER);

    assertEquals(100, result.getWidth());
    assertEquals(100, result.getHeight());
  }

  @Test
  public void cropSqaureTopLeftIsRightArea() throws IOException {
    BufferedImage rgbRect = ImageIO.read(new File("test/rgbRect.png"));
    BufferedImage result = ImageUtils.cropSquare(rgbRect, CropType.TOP_LEFT);
    int[] pixels = new int[result.getWidth() * result.getHeight()];
    result.getRGB(0,0, result.getWidth(), result.getHeight(), pixels, 0, result.getWidth());

    int[] expecteds = new int[result.getWidth() * result.getHeight()];
    Arrays.fill(expecteds, Color.RED.getRGB());

    assertArrayEquals(expecteds, pixels);
  }

  @Test
  public void cropSqaureCenterIsRightArea() throws IOException {
    BufferedImage rgbRect = ImageIO.read(new File("test/rgbRect.png"));
    BufferedImage result = ImageUtils.cropSquare(rgbRect, CropType.CENTER);
    int[] pixels = new int[result.getWidth() * result.getHeight()];
    result.getRGB(0,0, result.getWidth(), result.getHeight(), pixels, 0, result.getWidth());

    int[] expecteds = new int[result.getWidth() * result.getHeight()];
    Arrays.fill(expecteds, Color.GREEN.getRGB());

    assertArrayEquals(expecteds, pixels);
  }

}
