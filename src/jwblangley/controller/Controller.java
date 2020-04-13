package jwblangley.controller;

import java.awt.image.BufferedImage;
import java.io.File;

public interface Controller {

  void reportStatus(String status);

  int numCurrentInputs(File sourceDirectory, int numDuplicates);

  int numInputsRequired(BufferedImage targetImage, int subtileMatchSize, int numSubtiles);

  default boolean checkValidInputs(
      BufferedImage targetImage,
      File sourceDirectory,
      int numDuplicates,
      int numSubtiles,
      int subtileMatchSize,
      int tileRenderSize
      ) {

    // Validate inputs/settings
    if (targetImage == null) {
      reportStatus("Invalid target image");
      return false;
    }
    if (sourceDirectory == null) {
      reportStatus("Invalid input directory");
      return false;
    }
    if (numDuplicates < 1) {
      reportStatus("Invalid number of duplicates");
      return false;
    }
    if (numSubtiles < 1) {
      reportStatus("Invalid number of subtiles");
      return false;
    }
    if (subtileMatchSize < 1) {
      reportStatus("Invalid subtile match size");
    }
    if (tileRenderSize < 1) {
      reportStatus("Invalid tile render size");
      return false;
    }

    // Check there are enough inputs
    if (numCurrentInputs(sourceDirectory, numDuplicates)
        < numInputsRequired(targetImage, subtileMatchSize, numSubtiles)) {

      reportStatus("Not enough inputs with current settings");
      return false;
    }

    return true;
  }

}
