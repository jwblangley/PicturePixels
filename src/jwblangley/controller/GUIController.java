package jwblangley.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.view.GUIView;

public class GUIController {

  public static final int DEFAULT_NUM_SUBTILES = 7;
  public static final int DEFAULT_SUBTILE_MATCH_SIZE = 3;
  public static final int DEFAULT_NUM_DUPLICATES_ALLOWED = 1;
  public static final int DEFAULT_TILE_RENDER_SIZE = 100;

  private final PicturePixelMatcher matcher;
  private final GUIView view;

  private BufferedImage targetImage;
  private File sourceDirectory;
  private int numDuplicatesAllowed= DEFAULT_NUM_DUPLICATES_ALLOWED;
  private int numSubtiles = DEFAULT_NUM_SUBTILES;
  private int subtileMatchSize =DEFAULT_SUBTILE_MATCH_SIZE;
  private int tileRenderSize = DEFAULT_TILE_RENDER_SIZE;

  public GUIController(PicturePixelMatcher matcher, GUIView view) {
    this.matcher = matcher;
    this.view = view;
    view.setController(this);
  }

  public Pane getLayout(Stage window) {
    return this.view.layout(window);
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
