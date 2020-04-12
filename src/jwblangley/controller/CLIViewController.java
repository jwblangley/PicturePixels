package jwblangley.controller;

import jwblangley.pictureMatching.PicturePixelMatcher;

public class CLIViewController implements Controller {

  private final PicturePixelMatcher matcher;

  public CLIViewController(PicturePixelMatcher matcher) {
    this.matcher = matcher;
  }

  @Override
  public void reportStatus(String status) {
    System.out.println(status);
  }
}
