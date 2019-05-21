import java.awt.Color;
import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;

public class TileTest {

  @Test
  public void tileSeparatesRegionsCorrectly() {
    Tile testTile = new Tile(3, new File("test/rgbGrid.png"));

    Color[][] tiles = testTile.getTiles();
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

}
