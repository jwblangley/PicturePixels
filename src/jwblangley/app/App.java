package jwblangley.app;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import jwblangley.difference.LeastDifference;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.pictureMatching.Tile;
import jwblangley.view.PicturePixelView;

public class App {

  public static final int DEFAULT_NUM_SUBTILES = 7;
  public static final int DEFAULT_SUBTILE_MATCH_SIZE = 3;

  public static final int DEFAULT_NUM_DUPLICATES_ALLOWED = 1;

  // For generating the resulting image
  public static final int DEFAULT_TILE_RENDER_SIZE = 100;

  private static final int SEARCH_REPEATS = 3;

  private static PicturePixelMatcher matcher;
  private static PicturePixelView view;


  public static void main(String[] args) {
    matcher = new PicturePixelMatcher();
    matcher.setNumSubtiles(DEFAULT_NUM_SUBTILES);
    matcher.setSubtileMatchSize(DEFAULT_SUBTILE_MATCH_SIZE);
    matcher.setNumDuplicatesAllowed(DEFAULT_NUM_DUPLICATES_ALLOWED);
    matcher.setTileRenderSize(DEFAULT_TILE_RENDER_SIZE);

    view = new PicturePixelView(matcher);
    view.createDisplay();
    view.setLocationRelativeTo(null);
    view.setVisible(true);
  }

  public static void runPicturePixels() {
    assert matcher.getTargetImage() != null;
    assert matcher.getInputDirectory() != null;

    view.disableInputs();

    if (matcher.numCurrentInputs() < matcher.inputsRequired()) {
      view.setStatus("Not enough inputs with current settings", Color.RED);
      view.enableInputs();
      return;
    }

    // Set up observer for progress
    AtomicInteger progressCounter = new AtomicInteger(0);
    Observer progressObserver = () -> {
      view.setProgress(progressCounter.incrementAndGet());
      view.setStatus(progressCounter.get() < matcher.getInputDirectory().listFiles().length
          ? "Reading inputs" : "Rereading selected inputs", Color.BLACK);
    };
    matcher.addObserver(progressObserver);

    // Generate targetTiles
    List<Tile> targetTiles = matcher.generateTilesFromImage();

    // Generate input tiles
    List<Tile> inputTiles = matcher.generateTilesFromDirectory();

    // We only check against number of files previously: check now that all tiles are successful
    if (inputTiles.size() * matcher.getNumDuplicatesAllowed() < matcher.inputsRequired()) {
      view.setStatus("Not enough input images: some files could not be read as images", Color.RED);
      view.enableInputs();
      return;
    }

    // Calculate match
    List<Tile> resultList = LeastDifference.nearestNeighbourMatch(
        inputTiles,
        targetTiles,
        matcher.getNumDuplicatesAllowed(),
        App.SEARCH_REPEATS,
        Tile.differenceFunction::absoluteDifference
    );

    // Generate resulting image
    BufferedImage resultImage
        = matcher.collateResultFromImages(resultList);

    view.setStatus("Generation complete, choose save location", Color.BLACK);
    view.setProgress("Complete");

    // Save image option
    JFileChooser saveChooser = new JFileChooser();
    FileFilter imageFilter = new FileNameExtensionFilter(
        "Image files", ImageIO.getWriterFileSuffixes());
    saveChooser.setFileFilter(imageFilter);

    int saveSuccess = saveChooser.showSaveDialog(null);
    if (saveSuccess == JFileChooser.APPROVE_OPTION) {
      // Validate save location
      File saveFile;
      if (imageFilter.accept(saveChooser.getSelectedFile())) {
        saveFile = saveChooser.getSelectedFile();
      } else {
        saveFile = new File("output.png");
        view.setStatus("Cannot save that file (location/type). Saved to "
            + saveFile.getAbsolutePath(), Color.RED);
      }

      // Write resulting image to save location
      String fileExt = saveFile.getAbsolutePath()
          .substring(saveFile.getAbsolutePath().lastIndexOf('.') + 1);
      try {
        ImageIO.write(resultImage, fileExt, saveFile);
        view.setStatus("Image written", Color.GREEN);
      } catch (IOException e) {
        // TODO: handle exception
        e.printStackTrace();
      }
    }

    view.enableInputs();
  }
}
