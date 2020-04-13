package jwblangley.controller;

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
    System.out.printf("[%s%s] %2.0f%%\n",
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
