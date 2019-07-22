package jwblangley.app;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import jwblangley.difference.LeastDifference;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.pictureMatching.Tile;
import jwblangley.view.PicturePixelView;

public class App {

  private static final int NUM_SUBTILES = 7;
  private static final int SUBTIILE_MATCH_SIZE = 3;
  private static final int TILE_MATCH_SIZE = NUM_SUBTILES * SUBTIILE_MATCH_SIZE;

  public static final int NUM_DUPLICATES_ALLOWED = 1;
  public static final int SEARCH_REPEATS = 3;

  // For generating the resulting image
  public static final int TILE_RENDER_SIZE = 100;

  private static PicturePixelMatcher matcher;
  private static PicturePixelView view;


  public static void main(String[] args) {
    matcher = new PicturePixelMatcher();
    matcher.setNumSubtiles(NUM_SUBTILES);
    matcher.setTileMatchSize(TILE_MATCH_SIZE);
    matcher.setNumDuplicatesAllowed(NUM_DUPLICATES_ALLOWED);

    view = new PicturePixelView(matcher);
    view.createDisplay();
    view.setLocationRelativeTo(null);
    view.setVisible(true);
  }

  public static void runPicturePixels() {
    // Set up observer for progress
    AtomicInteger progressCounter = new AtomicInteger(0);
    Observer progressObserver = () -> view.setStatus(
        String.format("%d/%d: %s",
            progressCounter.incrementAndGet(),
            matcher.maxProgress(),
            progressCounter.get() < matcher.getInputDirectory().listFiles().length
                ? "Reading inputs" : "Rereading selected inputs"
        ),
        Color.BLACK
    );
    matcher.addObserver(progressObserver);

    // Generate targetTiles
    List<Tile> targetTiles = matcher.generateTilesFromImage();

    // Generate input tiles
    List<Tile> inputTiles = matcher.generateTilesFromDirectory();

    // TODO: check input tiles length is still okay - bad inputs will be removed

    // Calculate match
    List<Tile> resultList = LeastDifference.nearestNeighbourMatch(
        inputTiles,
        targetTiles,
        matcher.getNumDuplicatesAllowed(),
        App.SEARCH_REPEATS,
        Tile.differenceFunction::absoluteDifference);

    // Generate resulting image
    BufferedImage resultImage
        = matcher.collateResultFromImages(resultList, TILE_RENDER_SIZE);

    // Write resulting image
    try {
      ImageIO.write(resultImage, "png", new File("output.png"));
      System.out.println("Written");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
