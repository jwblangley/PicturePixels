package jwblangley.app;

import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.view.PicturePixelView;

public class App {

  private static final int NUM_SUBTILES = 7;
  private static final int SUBTIILE_MATCH_SIZE = 3;
  private static final int TILE_MATCH_SIZE = NUM_SUBTILES * SUBTIILE_MATCH_SIZE;

  public static final int NUM_DUPLICATES_ALLOWED = 1;
  public static final int SEARCH_REPEATS = 3;

  // For generating the resulting image
  public static final int TILE_RENDER_SIZE = 100;


  public static void main(String[] args) {
    PicturePixelMatcher picturePixelMatcher = new PicturePixelMatcher();
    picturePixelMatcher.setNumSubtiles(NUM_SUBTILES);
    picturePixelMatcher.setTileMatchSize(TILE_MATCH_SIZE);
    picturePixelMatcher.setNumDuplicatesAllowed(NUM_DUPLICATES_ALLOWED);

    PicturePixelView view = new PicturePixelView(picturePixelMatcher);
    view.createDisplay();
    view.setLocationRelativeTo(null);
    view.setVisible(true);
  }
}
