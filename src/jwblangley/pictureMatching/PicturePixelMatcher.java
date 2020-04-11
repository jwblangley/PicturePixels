package jwblangley.pictureMatching;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import jwblangley.difference.LeastDifference;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;

public class PicturePixelMatcher {

  private static final int SEARCH_REPEATS = 3;

  private File lastSetRootFile;
  private List<File> cachedInputFiles;

  private List<File> recursiveListFiles(File rootFile) {
    assert rootFile.isDirectory();

    // Return cached result if possible
    if (rootFile.equals(lastSetRootFile)) {
      return cachedInputFiles;
    }
    lastSetRootFile = rootFile;

    List<File> files = new ArrayList<>();
    for (File file : rootFile.listFiles()) {
      if (file.isDirectory()) {
        files.addAll(recursiveListFiles(file));
      } else {
        files.add(file);
      }
    }

    cachedInputFiles = files;
    return files;
  }

  public int numCurrentInputs(File sourceDirectory, int numDuplicatesAllowed) {
    return recursiveListFiles(sourceDirectory).size() * numDuplicatesAllowed;
  }

  private int tileMatchSize(int subtileMatchSize, int numSubtiles) {
    // Size that each image (from set of files) represents in the output image
    return subtileMatchSize * numSubtiles;
  }

  public int numInputsRequired(BufferedImage targetImage, int subtileMatchSize, int numSubtiles) {
    int numTilesWidth = targetImage.getWidth() / tileMatchSize(subtileMatchSize, numSubtiles);
    int numTilesHeight = targetImage.getHeight() / tileMatchSize(subtileMatchSize, numSubtiles);
    return numTilesWidth * numTilesHeight;
  }

  public Dimension resultDimension(BufferedImage targetImage, int subtileMatchSize, int numSubtiles, int tileRenderSize) {
    int numTilesWidth = targetImage.getWidth() / tileMatchSize(subtileMatchSize, numSubtiles);
    int numTilesHeight = targetImage.getHeight() / tileMatchSize(subtileMatchSize, numSubtiles);
    int w = numTilesWidth * tileRenderSize;
    int h = numTilesHeight * tileRenderSize;
    return new Dimension(w, h);
  }

  public List<Tile> generateTilesFromImage(BufferedImage targetImage, int subtileMatchSize, int numSubtiles) {

    final int tileMatchSize = tileMatchSize(subtileMatchSize, numSubtiles);

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
            = targetImage.getSubimage(
            x * tileMatchSize,
            y * tileMatchSize,
            tileMatchSize, tileMatchSize
        );
        Tile targetTile = Tile.ofBufferedImage(numSubtiles, subImage);
        targetTiles.add(targetTile);
      }
    }

    return targetTiles;
  }

  // Will notify observers once every directory
  public List<Tile> generateTilesFromDirectory(File inputDirectory, int numSubtiles) {
    assert inputDirectory.isDirectory() : "Must be given a directory";

    List<File> inputFiles = recursiveListFiles(inputDirectory);

    List<Tile> tiles = inputFiles.stream()
        .parallel()
        .map(file -> {
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
        .filter(t -> !t.isNull())
        .collect(Collectors.toList());

    return tiles;
  }

  public BufferedImage collateResultFromImages(List<Tile> tiles, BufferedImage targetImage, int subtileMatchSize, int numSubtiles, int tileRenderSize) {
    int numTilesWidth = targetImage.getWidth() / tileMatchSize(subtileMatchSize, numSubtiles);
    int numTilesHeight = targetImage.getHeight() / tileMatchSize(subtileMatchSize, numSubtiles);

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
        e.printStackTrace();
        return;
      }

      toDraw = ImageUtils.cropSquare(toDraw, CropType.CENTER);

      g.drawImage(toDraw, x * tileRenderSize, y * tileRenderSize, tileRenderSize,
          tileRenderSize, null);
    });

    return resultImage;
  }

  public BufferedImage createPicturePixels(
      BufferedImage targetImage,
      File sourceDirectory,
      int numSubtiles,
      int subtileMatchSize,
      int numDuplicatesAllowed,
      int tileRenderSize) {

    assert targetImage != null;
    assert sourceDirectory != null;
    assert subtileMatchSize > 0;
    assert numSubtiles > 0;
    assert tileRenderSize > 0;

    assert numDuplicatesAllowed > 0;

    // Generate targetTiles
    List<Tile> targetTiles = generateTilesFromImage(targetImage, subtileMatchSize, numSubtiles);

    // Generate input tiles
    List<Tile> inputTiles = generateTilesFromDirectory(sourceDirectory, numSubtiles);

    // We only check against number of files previously: check now that all tiles are successful
    if (inputTiles.size() * numDuplicatesAllowed < numInputsRequired(targetImage, subtileMatchSize, numSubtiles)) {
        // TODO
//      view.setStatus("Not enough input images: some files could not be read as images", Color.RED);
//      view.enableInputs();
//      return;
    }

    // Calculate match
    List<Tile> resultList = LeastDifference.nearestNeighbourMatch(
        inputTiles,
        targetTiles,
        numDuplicatesAllowed,
        SEARCH_REPEATS,
        Tile.differenceFunction::absoluteDifference,
        true
    );

    // Generate resulting image
    BufferedImage resultImage
        = collateResultFromImages(resultList, targetImage, subtileMatchSize, numSubtiles, tileRenderSize);

    return resultImage;
  }

}
