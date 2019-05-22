import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class App {

  public static void main(String[] args) {
    File testImages = new File("exampleImages/");
    assert testImages.isDirectory();

    int numFiles = testImages.listFiles().length;
    AtomicInteger progress = new AtomicInteger(0);

    List<Tile> tiles = Arrays.stream(testImages.listFiles())
        .parallel()
        .map(file -> {
          try {
            System.out.println(progress.incrementAndGet() + "/" + numFiles);
            return Tile.ofImage(3, file);
          } catch (IOException e) {
            return Tile.nullTile();
          }
        })
        .collect(Collectors.toList());

    // Remove any null tiles (From non image files)
    // N.B do the check this way round to avoid having to do repeat IO.
    tiles = tiles.stream().filter(Predicate.not(Tile::isNull)).collect(Collectors.toList());

    System.out.println("Size: " + tiles.size());


  }
}
