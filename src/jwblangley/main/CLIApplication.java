package jwblangley.main;

import jwblangley.controller.CLIViewController;
import jwblangley.pictureMatching.PicturePixelMatcher;

public class CLIApplication {

  private final PicturePixelMatcher picturePixelMatcher = new PicturePixelMatcher();
  private final CLIViewController cliController = new CLIViewController(picturePixelMatcher);

  public static void launch(String[] args) {
  }
}
