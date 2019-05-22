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

public class App {

  private static final int NUM_SUBTILES = 3;
  private static final int TILE_SIZE = 30;

  private static final String INPUT_DIRECTORY = "exampleImages/";
  private static final String TARGET_IMAGE = "target.png";


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

    assert targetImage.getWidth() > TILE_SIZE * NUM_SUBTILES : "Input image is not large enough for specified tile layout";
    assert targetImage.getHeight() > TILE_SIZE * NUM_SUBTILES : "Input image is not large enough for specified tile layout";

    int tileWidth = targetImage.getWidth() / TILE_SIZE;
    int tileHeight = targetImage.getHeight() / TILE_SIZE;



    List<Tile> targetTiles = new LinkedList<>();
    for (int y = 0; y < targetImage.getHeight() / TILE_SIZE; y++) {
      for (int x = 0; x < targetImage.getWidth() / TILE_SIZE; x++) {
        BufferedImage subImage = targetImage.getSubimage(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // TODO: create tile
        // TODO: add tile to targetTiles
      }
    }

    // Generate input tiles

    File testImages = new File(INPUT_DIRECTORY);
    assert testImages.isDirectory(): "Must be given a directory";

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
          } catch (IOException e) {
            return Tile.nullTile();
          }
        })
        .collect(Collectors.toList());

    // Remove any null tiles (from non image files)
    // N.B do the check this way round to avoid having to do repeat IO
    inputTiles = inputTiles.stream().filter(Predicate.not(Tile::isNull)).collect(Collectors.toList());


    // Test
    System.out.println("Size: " + inputTiles.size());

  }
}
