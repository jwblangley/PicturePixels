package jwblangley.pictureMatching;

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
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import jwblangley.observer.Observable;
import jwblangley.observer.Observer;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;

public class PicturePixelMatcher implements Observable {

  List<Observer> observers = new LinkedList<>();

  private BufferedImage targetImage;
  private int numSubtiles;
  private int tileMatchSize;
  private int numDuplicatesAllowed;
  private int tileRenderSize;

  private File inputDirectory;

  public BufferedImage getTargetImage() {
    return targetImage;
  }

  public void setTargetImage(BufferedImage targetImage) {
    this.targetImage = targetImage;
  }

  public void setNumSubtiles(int numSubtiles) {
    this.numSubtiles = numSubtiles;
  }

  public void setTileMatchSize(int tileMatchSize) {
    this.tileMatchSize = tileMatchSize;
  }

  public int getNumDuplicatesAllowed() {
    return numDuplicatesAllowed;
  }

  public void setNumDuplicatesAllowed(int numDuplicatesAllowed) {
    this.numDuplicatesAllowed = numDuplicatesAllowed;
  }

  public void setTileRenderSize(int tileRenderSize) {
    this.tileRenderSize = tileRenderSize;
  }

  public File getInputDirectory() {
    return inputDirectory;
  }

  public void setInputDirectory(File inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  // Used for progress updates
  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers() {
    observers.forEach(Observer::onNotified);
  }


  public int numCurrentInputs() {
    return inputDirectory.listFiles().length * numDuplicatesAllowed;
  }

  public int inputsRequired() {
    int numTilesWidth = targetImage.getWidth() / tileMatchSize;
    int numTilesHeight = targetImage.getHeight() / tileMatchSize;
    return numTilesWidth * numTilesHeight;
  }

  public int maxProgress() {
    // N.B: inputs required is equal to the number of tiles used
    return inputDirectory.listFiles().length + inputsRequired();
  }


  public List<Tile> generateTilesFromImage() {
    assert targetImage.getWidth() > tileMatchSize
        : "Input image is not large enough for specified tile layout";
    assert targetImage.getHeight() > tileMatchSize
        : "Input image is not large enough for specified tile layout";

    int numTilesWidth = targetImage.getWidth() / tileMatchSize;
    int numTilesHeight = targetImage.getHeight() / tileMatchSize;

    List<Tile> targetTiles = new LinkedList<>();
    for (int y = 0; y < numTilesHeight; y++) {
      for (int x = 0; x < numTilesWidth; x++) {
        BufferedImage subImage
            = targetImage
            .getSubimage(x * tileMatchSize, y * tileMatchSize, tileMatchSize, tileMatchSize);
        Tile targetTile = Tile.ofBufferedImage(numSubtiles, subImage);
        targetTiles.add(targetTile);
      }
    }

    return targetTiles;
  }

  // Will notify observers once every directory
  public List<Tile> generateTilesFromDirectory() {
    assert inputDirectory.isDirectory() : "Must be given a directory";

    int numFiles = inputDirectory.listFiles().length;
    AtomicInteger progress = new AtomicInteger(0);

    HashMap<Tile, File> tileSource = new HashMap<>();

    List<Tile> tiles = Arrays.stream(inputDirectory.listFiles())
        .parallel()
        .map(file -> {
          // Progress update. N.B: before section as section can be interrupted.
          notifyObservers();
          try {
            return Tile.ofImageFile(numSubtiles, file);
          } catch (Exception e) {
            System.out.printf("Could not read%s as image\n", file.getAbsolutePath());
            return Tile.nullTile();
          }
        })
        .collect(Collectors.toList());

    // Remove any null tiles (from non image files)
    // N.B do the check this way round to avoid having to do repeat IO
    tiles = tiles.stream()
        .filter(Predicate.not(Tile::isNull))
        .collect(Collectors.toList());

    return tiles;
  }

  // Will notify observers after each tile is drawn.
  public BufferedImage collateResultFromImages(List<Tile> tiles) {
    int numTilesWidth = targetImage.getWidth() / tileMatchSize;
    int numTilesHeight = targetImage.getHeight() / tileMatchSize;

    assert tiles != null && tiles.size() == numTilesWidth * numTilesHeight
        : "Incorrect number of tiles for dimensions specified";

    BufferedImage resultImage
        = new BufferedImage(tileRenderSize * numTilesWidth,
        tileRenderSize * numTilesHeight,
        BufferedImage.TYPE_INT_RGB);

    Graphics g = resultImage.getGraphics();

    IntStream.range(0, tiles.size()).parallel().forEach(i -> {
      Tile tile = tiles.get(i);
      int y = i / numTilesWidth;
      int x = i % numTilesWidth;

      BufferedImage toDraw = null;
      try {
        toDraw = ImageIO.read(tile.getSource());
      } catch (IOException e) {
        //TODO: handle exception
      }

      toDraw = ImageUtils.cropSquare(toDraw, CropType.CENTER);

      g.drawImage(toDraw, x * tileRenderSize, y * tileRenderSize, tileRenderSize,
          tileRenderSize, null);

      // Progress update
      notifyObservers();
    });

    return resultImage;
  }

}
