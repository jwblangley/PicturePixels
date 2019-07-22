package jwblangley.utils;

import java.awt.image.BufferedImage;

public class ImageUtils {

  public static BufferedImage cropSquare(BufferedImage image, CropType crop) {

    BufferedImage result = null;

    int minDim = Integer.min(image.getWidth(), image.getHeight());
    switch (crop) {
      case TOP_LEFT:
        result = image.getSubimage(0, 0, minDim, minDim);
        break;

      case CENTER:
        int maxDim = Integer.max(image.getWidth(), image.getHeight());
        int split = (maxDim - minDim) / 2;
        if (image.getWidth() > image.getHeight()) {
          result = image.getSubimage(split, 0, minDim, minDim);
        } else {
          result = image.getSubimage(0, split, minDim, minDim);
        }
        break;
    }

    return result;
  }

}
