package jwblangley.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.view.GUIPicturePixelView;

public class GUIController extends Application {

  public static final int DEFAULT_NUM_SUBTILES = 7;
  public static final int DEFAULT_SUBTILE_MATCH_SIZE = 3;
  public static final int DEFAULT_NUM_DUPLICATES_ALLOWED = 1;
  public static final int DEFAULT_TILE_RENDER_SIZE = 100;

  private final PicturePixelMatcher matcher;
  private final GUIPicturePixelView view;

  private BufferedImage targetImage;
  private File sourceDirectory;
  private int numDuplicatesAllowed= DEFAULT_NUM_DUPLICATES_ALLOWED;
  private int numSubtiles = DEFAULT_NUM_SUBTILES;
  private int subtileMatchSize =DEFAULT_SUBTILE_MATCH_SIZE;
  private int tileRenderSize = DEFAULT_TILE_RENDER_SIZE;

  public GUIController(PicturePixelMatcher matcher, GUIPicturePixelView view) {
    this.matcher = matcher;
    this.view = view;
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

  public int numCurrentInputs() {
    return matcher.numCurrentInputs(sourceDirectory, numDuplicatesAllowed);
  }

  public int numInputsRequired() {
    return matcher.numInputsRequired(targetImage, subtileMatchSize, numSubtiles);
  }


  @Override
  public void start(Stage window) throws Exception {
    Scene scene = new Scene(new GUIPicturePixelView(this).layout(window));

    window.setTitle("PicturePixels");
    window.setScene(scene);
    window.centerOnScreen();
    window.show();
  }

  public boolean checkValidInputs() {
    if (targetImage == null) {
      view.setStatus("Please select a target image", Color.RED);
      return false;
    }
    if (sourceDirectory == null) {
      view.setStatus("Please select an input directory", Color.RED);
      return false;
    }
    if (numCurrentInputs() < numInputsRequired()) {
      view.setStatus("Not enough inputs with current settings", Color.RED);
      return false;
    }
    // TODO: check other inputs
    return true;
  }

  public void runPicturePixels() {
    if (checkValidInputs()) {
      matcher.createPicturePixels(
          targetImage,
          sourceDirectory,
          numSubtiles,
          subtileMatchSize,
          numDuplicatesAllowed,
          tileRenderSize
      );
    }
  }
}
