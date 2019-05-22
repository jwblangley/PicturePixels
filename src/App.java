import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class App {

  public static void main(String[] args) {
    File testImages = new File("exampleImages/");
    assert testImages.isDirectory();

    int numImages = testImages.listFiles().length;
    AtomicInteger progress = new AtomicInteger(0);

    List<Tile> tiles = Arrays.stream(testImages.listFiles())
        .parallel()
        .map(file -> {
          try {
            System.out.println(progress.incrementAndGet() + "/" + numImages);
            return Tile.ofImage(3, file);
          } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
          }
        })
        .collect(Collectors.toList());


  }
}
