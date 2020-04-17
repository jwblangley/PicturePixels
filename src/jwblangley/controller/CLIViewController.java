package jwblangley.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.imageio.ImageIO;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;

public class CLIViewController implements Controller, Observer {

  public static final int PROGRESS_BAR_LENGTH = 50;

  private final PicturePixelMatcher matcher;

  public CLIViewController(PicturePixelMatcher matcher) {
    this.matcher = matcher;

    matcher.setController(this);
    matcher.addObserver(this);
  }

  @Override
  public void reportStatus(String status) {
    System.out.println(status);
  }

  @Override
  public int numCurrentInputs(File sourceDirectory, int numDuplicates) {
    return matcher.numCurrentInputs(sourceDirectory, numDuplicates);
  }

  @Override
  public int numInputsRequired(BufferedImage targetImage, int subtileMatchSize, int numSubtiles) {
    return matcher.numInputsRequired(targetImage, subtileMatchSize, numSubtiles);
  }

  private void printProgress(double progress) {
    int numHashes = (int) Math.ceil(progress * ((double) PROGRESS_BAR_LENGTH));
    int numSpaces = PROGRESS_BAR_LENGTH - numHashes;
    System.out.printf("[%s%s] %2.0f%%\r",
        String.join("", Collections.nCopies(numHashes, "#")),
        String.join("", Collections.nCopies(numSpaces, " ")),
        progress * 100
    );
  }

  // Runs on progress update
  @Override
  public void onNotified() {
    printProgress(matcher.getProgress());
  }

  public boolean parseInputsAndRun(String[] args) {
    // Returns false when args structure is incorrect

    // N.B: Not validating model parameters here as that is done in checkValidInputs

    if (args.length != 9) {
      return false;
    }

    boolean info;

    BufferedImage targetImage;
    File sourceDirectory;
    int numDuplicates;
    int numSubtiles;
    int subtileMatchSize;
    int tileRenderSize;
    File saveFile;

    // nogui
    if (!args[0].equalsIgnoreCase("nogui")) {
      return false;
    }

    // run|info
    String runInfo = args[1];
    if (runInfo.equalsIgnoreCase("run")) {
      info = false;
    } else if (runInfo.equalsIgnoreCase("info")) {
      info = true;
    } else {
      // Invalid input
      return false;
    }

    // Target Image
    File targetImageFile = new File(args[2]).getAbsoluteFile();
    try {
      targetImage = ImageIO.read(targetImageFile);
    } catch (IOException ex) {
      reportStatus("Could not read target image");
      return false;
    }

    // Input directory
    sourceDirectory = new File(args[3]);

    // Number of duplicates
    try {
      numDuplicates = Integer.parseInt(args[4]);
    } catch (NumberFormatException e) {
      reportStatus("Invalid integer: number of duplicates");
      return false;
    }

    // Number of subtiles
    try {
      numSubtiles = Integer.parseInt(args[5]);
    } catch (NumberFormatException e) {
      reportStatus("Invalid integer: number of subtiles");
      return false;
    }

    // Subtile match size
    try {
      subtileMatchSize = Integer.parseInt(args[6]);
    } catch (NumberFormatException e) {
      reportStatus("Invalid integer: number of duplicates");
      return false;
    }

    // Tile render size
    try {
      tileRenderSize = Integer.parseInt(args[7]);
    } catch (NumberFormatException e) {
      reportStatus("Invalid integer: number of duplicates");
      return false;
    }

    // Save file
    saveFile = new File(args[8]).getAbsoluteFile();
    if (saveFile.exists()) {
      reportStatus("Save file already exists");
      return false;
    }
    if (!saveFile.getParentFile().isDirectory()) {
      // Checks that parent directory exists and is a directory
      reportStatus("Invalid save file location");
      return false;
    }

    if (info) {
      reportInfo(
          targetImage,
          sourceDirectory,
          numSubtiles,
          subtileMatchSize,
          numDuplicates,
          tileRenderSize
      );
    } else {
      runPicturePixels(
          targetImage,
          sourceDirectory,
          numSubtiles,
          subtileMatchSize,
          numDuplicates,
          tileRenderSize,
          saveFile
      );
    }
    return true;
  }

  public void reportInfo(
      BufferedImage targetImage,
      File sourceDirectory,
      int numSubtiles,
      int subtileMatchSize,
      int numDuplicates,
      int tileRenderSize
  ) {

    Dimension resultDim
        = matcher.resultDimension(targetImage, subtileMatchSize, numSubtiles, tileRenderSize);

    reportStatus(String.format("Inputs/Tiles: %d/%d, Result image: %dx%d",
        matcher.numCurrentInputs(sourceDirectory, numDuplicates),
        matcher.numInputsRequired(targetImage, subtileMatchSize, numSubtiles),
        resultDim.width,
        resultDim.height)
    );
  }

  public void runPicturePixels(
      BufferedImage targetImage,
      File sourceDirectory,
      int numSubtiles,
      int subtileMatchSize,
      int numDuplicatesAllowed,
      int tileRenderSize,
      File saveFile) {

    if (checkValidInputs(targetImage, sourceDirectory, numDuplicatesAllowed,
        numSubtiles, subtileMatchSize, tileRenderSize)) {

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
        return;
      }

      reportStatus("Generation complete");

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
  }
}
