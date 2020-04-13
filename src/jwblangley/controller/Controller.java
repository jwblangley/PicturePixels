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

    // TODO: Check remaining inputs

    // Check there are enough inputs
    if (numCurrentInputs(sourceDirectory, numDuplicates)
        < numInputsRequired(targetImage, subtileMatchSize, numSubtiles)) {

      reportStatus("Not enough inputs with current settings");
      return false;
    }

    return true;
  }

}
