import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import jwblangley.pictureMatching.Tile;
import org.junit.Test;

public class TileTest {

  @Test
  public void tileSeparatesRegionsCorrectly() throws IOException {
    Tile testTile = Tile.ofImageFile(3, new File("test/rgbGrid.png"));

    Color[][] tiles = testTile.getSubtiles();
    assertEquals(Color.RED, tiles[0][0]);
    assertEquals(Color.RED, tiles[1][1]);
    assertEquals(Color.RED, tiles[2][2]);

    assertEquals(Color.GREEN, tiles[1][0]);
    assertEquals(Color.GREEN, tiles[2][1]);
    assertEquals(Color.GREEN, tiles[0][2]);

    assertEquals(Color.BLUE, tiles[2][0]);
    assertEquals(Color.BLUE, tiles[0][1]);
    assertEquals(Color.BLUE, tiles[1][2]);
  }

  @Test
  public void differenceBetweenTwoIdentialTilesIsZero() throws IOException {
    Tile a = Tile.ofImageFile(3, new File("test/rgbGrid.png"));
    Tile b = Tile.ofImageFile(3, new File("test/rgbGrid.png"));

    assertEquals(0, (long) Tile.differenceFunction.apply(a, b));

  }

}
