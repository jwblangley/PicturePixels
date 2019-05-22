import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {

  public static void main(String[] args) {
    File testImages = new File("exampleImages/");
    assert testImages.isDirectory();

    List<Tile> tiles = Arrays.stream(testImages.listFiles()).parallel()
        .map(file -> {
          try {
            System.out.println(file.getPath());
            return Tile.ofImage(3, file);
          } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
          }
        })
        .collect(Collectors.toList());

    System.out.println("tiles.get(0) = " + tiles.get(0).getSubtiles()[0][0]);

  }
}
