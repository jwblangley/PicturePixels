import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import jwblangley.difference.LeastDifference;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.tile.Tile;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;

public class App {

  private static final int NUM_SUBTILES = 7;
  private static final int SUBTIILE_MATCH_SIZE = 3;
  private static final int TILE_MATCH_SIZE = NUM_SUBTILES * SUBTIILE_MATCH_SIZE;

  // For generating the resulting image
  private static final int TILE_RENDER_SIZE = 100;

  private static final String INPUT_DIRECTORY = "exampleImages/";
  private static final String TARGET_IMAGE = "target.png";

  private static final int SEARCH_REPEATS = 3;

  public static void main(String[] args) {

    PicturePixelMatcher picturePixelMatcher = new PicturePixelMatcher();

    // Read in target image
    BufferedImage targetImage = null;
    try {
      targetImage = ImageIO.read(new File(TARGET_IMAGE));
    } catch (IOException e) {
      // TODO: handle exception
      e.printStackTrace();
    }

    int numTilesWidth = targetImage.getWidth() / TILE_MATCH_SIZE;
    int numTilesHeight = targetImage.getHeight() / TILE_MATCH_SIZE;

    // Generate targetTiles
    List<Tile> targetTiles
        = picturePixelMatcher.generateTilesFromImage(targetImage, NUM_SUBTILES, TILE_MATCH_SIZE);

    // Set up observer for progress
    File inputDirectory = new File(INPUT_DIRECTORY);
    AtomicInteger inputProgressCounter = new AtomicInteger(0);
    Observer inputProgressObserver = () -> {
      // Acts as CLI view
      System.out.println(String.format("%d/%d", inputProgressCounter.incrementAndGet(), inputDirectory.listFiles().length));
    };
    picturePixelMatcher.addObserver(inputProgressObserver);

    // Generate input tiles
    List<Tile> inputTiles = picturePixelMatcher.generateTilesFromDirectory(inputDirectory, NUM_SUBTILES);

    // Remove observer to allow reread progress
    picturePixelMatcher.removeObserver(inputProgressObserver);

    // Calculate match
    List<Tile> resultList = LeastDifference.repeatNearestNeighbourMatch(
        SEARCH_REPEATS,
        inputTiles,
        targetTiles,
        Tile.differenceFunction::absoluteDifference);

    // Set up observer for progress
    AtomicInteger resultProgressCounter = new AtomicInteger(0);
    Observer resultProgressObserver = () -> {
      // Acts as CLI view
      System.out.println(String.format("Rereading: %d/%d", resultProgressCounter.incrementAndGet(), resultList.size()));
    };
    picturePixelMatcher.addObserver(resultProgressObserver);

    // Generate resulting image
    BufferedImage resultImage
        = picturePixelMatcher.generateResultingImage(resultList, numTilesWidth, numTilesHeight, TILE_RENDER_SIZE);

    // Write resulting image
    try {
      ImageIO.write(resultImage, "png", new File("output.png"));
      System.out.println("Written");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
