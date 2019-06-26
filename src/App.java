import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import jwblangley.difference.LeastDifference;

public class App {

  private static final int NUM_SUBTILES = 7;
  private static final int SUBTIILE_MATCH_SIZE = 3;
  private static final int TILE_MATCH_SIZE = NUM_SUBTILES * SUBTIILE_MATCH_SIZE;

  // Generate resulting image
  private static final int TILE_RENDER_SIZE = 100;

  private static final String INPUT_DIRECTORY = "exampleImages/";
  private static final String TARGET_IMAGE = "target.png";

  private static final int SEARCH_REPEATS = 3;

  // TODO: refactor into cleaner code

  public static void main(String[] args) {
    // Generate target tiles

    BufferedImage targetImage = null;
    try {
      targetImage = ImageIO.read(new File(TARGET_IMAGE));
    } catch (IOException e) {
      // TODO: handle exception
      e.printStackTrace();
    }

    assert targetImage.getWidth() > TILE_MATCH_SIZE
        : "Input image is not large enough for specified tile layout";
    assert targetImage.getHeight() > TILE_MATCH_SIZE
        : "Input image is not large enough for specified tile layout";

    int numTilesWidth = targetImage.getWidth() / TILE_MATCH_SIZE;
    int numTilesHeight = targetImage.getHeight() / TILE_MATCH_SIZE;

    List<Tile> targetTiles = new LinkedList<>();
    for (int y = 0; y < numTilesHeight; y++) {
      for (int x = 0; x < numTilesWidth; x++) {
        BufferedImage subImage = targetImage.getSubimage(x * TILE_MATCH_SIZE, y * TILE_MATCH_SIZE,
            TILE_MATCH_SIZE, TILE_MATCH_SIZE);
        Tile targetTile = Tile.ofBufferedImage(NUM_SUBTILES, subImage);
        targetTiles.add(targetTile);
      }
    }

    // Generate input tiles
    File testImages = new File(INPUT_DIRECTORY);
    assert testImages.isDirectory() : "Must be given a directory";

    int numFiles = testImages.listFiles().length;
    AtomicInteger progress = new AtomicInteger(0);

    HashMap<Tile, File> tileSource = new HashMap<>();

    List<Tile> inputTiles = Arrays.stream(testImages.listFiles())
        .parallel()
        .map(file -> {
          try {
            System.out.println(progress.incrementAndGet() + "/" + numFiles);
            Tile tile = Tile.ofImageFile(NUM_SUBTILES, file);
            tileSource.put(tile, file);
            return tile;
          } catch (Exception e) {
            return Tile.nullTile();
          }
        })
        .collect(Collectors.toList());

    // Remove any null tiles (from non image files)
    // N.B do the check this way round to avoid having to do repeat IO
    inputTiles = inputTiles.stream()
        .filter(Predicate.not(Tile::isNull))
        .collect(Collectors.toList());

    List<Tile> resultList = LeastDifference.repeatNearestNeighbourMatch(
        SEARCH_REPEATS,
        inputTiles,
        targetTiles,
        Tile.differenceFunction::absoluteDifference);

    BufferedImage resultImage
        = new BufferedImage(TILE_RENDER_SIZE * numTilesWidth,
        TILE_RENDER_SIZE * numTilesHeight,
        BufferedImage.TYPE_INT_RGB);

    Graphics g = resultImage.getGraphics();

    for (int i = 0; i < resultList.size(); i++) {
      Tile tile = resultList.get(i);
      int y = i / numTilesWidth;
      int x = i % numTilesWidth;

      BufferedImage toDraw = null;
      try {
        toDraw = ImageIO.read(tileSource.get(tile));
      } catch (IOException e) {
        //TODO: handle exception
      }

      // Crop to square (top left corner)
      int minDimension = toDraw.getWidth() < toDraw.getHeight()
          ? toDraw.getWidth() : toDraw.getHeight();
      toDraw = toDraw.getSubimage(0, 0, minDimension, minDimension);

      g.drawImage(toDraw, x * TILE_RENDER_SIZE, y * TILE_RENDER_SIZE, TILE_RENDER_SIZE,
          TILE_RENDER_SIZE, null);

      System.out.println("Rereading " + i + "/" + resultList.size());
    }

    try {
      ImageIO.write(resultImage, "png", new File("output.png"));
      System.out.println("Written");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
