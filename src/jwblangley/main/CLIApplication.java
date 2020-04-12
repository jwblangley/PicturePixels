package jwblangley.main;

import jwblangley.controller.CLIViewController;
import jwblangley.pictureMatching.PicturePixelMatcher;

public class CLIApplication {

  private static final PicturePixelMatcher picturePixelMatcher = new PicturePixelMatcher();
  private static final CLIViewController cliController = new CLIViewController(picturePixelMatcher);

  public static void launch(String[] args) {
    if (args.length != 9) {
      showUsage();
      System.exit(1);
    }
  }

  public static void showUsage() {
    System.out.println("Usage: java [-Xmx4g] -jar PicturePixels.jar "
        + "nogui run|info <target image> <input directory> <number of duplicates allowed> "
        + "<number of subtiles> <subtile matchsize> <tile render size> <output file>");
  }
}
