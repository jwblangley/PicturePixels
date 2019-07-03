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
import javax.imageio.ImageIO;
import jwblangley.observer.Observable;
import jwblangley.observer.Observer;
import jwblangley.tile.Tile;
import jwblangley.utils.CropType;
import jwblangley.utils.ImageUtils;

public class PicturePixelMatcher implements Observable<Tile> {

  List<Observer<Tile>> observers = new LinkedList<>();

  @Override
  public void addObserver(Observer<Tile> observer) {
    observers.add(observer);
  }

  @Override
  public void notifyObservers(Tile param) {
    for (Observer<Tile> observer : observers) {
      observer.onNotified(param);
    }
  }

  public List<Tile> generateTilesFromImage(BufferedImage targetImage, int numSubtiles, int tileMatchSize) {
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
            = targetImage.getSubimage(x * tileMatchSize, y * tileMatchSize, tileMatchSize, tileMatchSize);
        Tile targetTile = Tile.ofBufferedImage(numSubtiles, subImage);
        targetTiles.add(targetTile);
      }
    }

    return targetTiles;
  }

  public List<Tile> generateTilesFromDirectory(File inputDirectory, int numSubtiles) {
    assert inputDirectory.isDirectory() : "Must be given a directory";

    int numFiles = inputDirectory.listFiles().length;
    AtomicInteger progress = new AtomicInteger(0);

    HashMap<Tile, File> tileSource = new HashMap<>();

    List<Tile> tiles = Arrays.stream(inputDirectory.listFiles())
        .parallel()
        .map(file -> {
          try {
            Tile tile = Tile.ofImageFile(numSubtiles, file);
            notifyObservers(tile);
            return tile;
          } catch (Exception e) {
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

  public BufferedImage generateResultingImage(List<Tile> chosenTiles, int numTilesWidth, int numTilesHeight, int tileRenderSize) {
    BufferedImage resultImage
        = new BufferedImage(tileRenderSize * numTilesWidth,
        tileRenderSize * numTilesHeight,
        BufferedImage.TYPE_INT_RGB);

    Graphics g = resultImage.getGraphics();

    for (int i = 0; i < chosenTiles.size(); i++) {
      Tile tile = chosenTiles.get(i);
      int y = i / numTilesWidth;
      int x = i % numTilesWidth;

      BufferedImage toDraw = null;
      try {
        System.out.println(tile.getSource());
        toDraw = ImageIO.read(tile.getSource());
      } catch (IOException e) {
        //TODO: handle exception
      }

      toDraw = ImageUtils.cropSquare(toDraw, CropType.CENTER);

      g.drawImage(toDraw, x * tileRenderSize, y * tileRenderSize, tileRenderSize,
          tileRenderSize, null);

      System.out.println("Rereading " + i + "/" + chosenTiles.size());
    }

    return resultImage;
  }

}
