package jwblangley.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.view.GUIView;

public class GUIController implements Controller, Observer {

  public static final int DEFAULT_NUM_SUBTILES = 7;
  public static final int DEFAULT_SUBTILE_MATCH_SIZE = 3;
  public static final int DEFAULT_NUM_DUPLICATES_ALLOWED = 1;
  public static final int DEFAULT_TILE_RENDER_SIZE = 100;

  private final PicturePixelMatcher matcher;
  private final GUIView view;

  private BufferedImage targetImage;
  private File sourceDirectory;
  private int numDuplicatesAllowed = DEFAULT_NUM_DUPLICATES_ALLOWED;
  private int numSubtiles = DEFAULT_NUM_SUBTILES;
  private int subtileMatchSize = DEFAULT_SUBTILE_MATCH_SIZE;
  private int tileRenderSize = DEFAULT_TILE_RENDER_SIZE;

  public GUIController(PicturePixelMatcher matcher, GUIView view) {
    this.matcher = matcher;
    this.view = view;

    view.setController(this);
    matcher.setController(this);
    matcher.addObserver(this);
  }

  public Pane getLayout(Stage window) {
    return this.view.layout(window);
  }

  public BufferedImage getTargetImage() {
    return targetImage;
  }

  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setTargetImage(BufferedImage targetImage) {
    this.targetImage = targetImage;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public void setNumDuplicatesAllowed(int numDuplicatesAllowed) {
    this.numDuplicatesAllowed = numDuplicatesAllowed;
  }

  public void setNumSubtiles(int numSubtiles) {
    this.numSubtiles = numSubtiles;
  }

  public void setSubtileMatchSize(int subtileMatchSize) {
    this.subtileMatchSize = subtileMatchSize;
  }

  public void setTileRenderSize(int tileRenderSize) {
    this.tileRenderSize = tileRenderSize;
  }

  public Dimension resultDimension() {
    return matcher.resultDimension(targetImage, subtileMatchSize, numSubtiles, tileRenderSize);
  }

  @Override
  public int numCurrentInputs(File sourceDirectory, int numDuplicatesAllowed) {
    return matcher.numCurrentInputs(sourceDirectory, numDuplicatesAllowed);
  }

  public int numCurrentInputs() {
    return numCurrentInputs(sourceDirectory, numDuplicatesAllowed);
  }

  @Override
  public int numInputsRequired(BufferedImage targetImage, int subtileMatchSize, int numSubtiles) {
    return matcher.numInputsRequired(targetImage, subtileMatchSize, numSubtiles);
  }

  public int numInputsRequired() {
    return numInputsRequired(targetImage, subtileMatchSize, numSubtiles);
  }

  public boolean checkValidInputs() {
    return checkValidInputs(targetImage, sourceDirectory, numDuplicatesAllowed,
        numSubtiles, subtileMatchSize, tileRenderSize);
  }

  public void runPicturePixels(Stage window) {
    view.disableInputs();

    if (checkValidInputs()) {
      BufferedImage resultImage = null;
      try {
        resultImage = matcher.createPicturePixels(
            targetImage,
            sourceDirectory,
            numDuplicatesAllowed,
            numSubtiles,
            subtileMatchSize,
            tileRenderSize
        );
      } catch (IllegalStateException e) {
        e.printStackTrace();
        view.enableInputs();
        return;
      }

      reportStatus("Generation complete, choose save location");

      FileChooser fc = new FileChooser();

      // Get save file location using JavaFX thread FileChooser
      AtomicReference<File> saveFileRef = new AtomicReference<>();

      // Order JavaFX thread to run FileChooser
      Semaphore semaphore = new Semaphore(0);
      Platform.runLater(() -> {
        saveFileRef.set(fc.showSaveDialog(window));
        // Release semaphore once complete
        semaphore.release();
      });

      // Wait for semaphore release
      try {
        semaphore.acquire();
      } catch (InterruptedException e) {
        e.printStackTrace();
        reportStatus("An error occurred");
        view.enableInputs();
        return;
      }

      File saveFile = saveFileRef.get();
      if (saveFile == null) {
        // Operation was cancelled
        return;
      }

      // Write resulting image to save location
      String fileExt = saveFile.getAbsolutePath()
          .substring(saveFile.getAbsolutePath().lastIndexOf('.') + 1);
      try {
        ImageIO.write(resultImage, fileExt, saveFile);
        reportStatus("Image written");
      } catch (IOException e) {
        e.printStackTrace();
        reportStatus("Could not write image");
      }
    }

    view.enableInputs();
  }

  // Runs on progress update
  @Override
  public void onNotified() {
    view.setProgress(matcher.getProgress());
  }

  public void reportStatus(String status) {
    view.setStatus(status);
  }
}

