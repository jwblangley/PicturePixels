package jwblangley.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import jwblangley.app.App;
import jwblangley.difference.LeastDifference;
import jwblangley.observer.Observer;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.pictureMatching.Tile;

public class PicturePixelView extends JFrame {

  private final PicturePixelMatcher matcher;

  private JLabel statusLabel;
  private JFileChooser targetImageChooser;

  public PicturePixelView(PicturePixelMatcher matcher) {
    super("Picture by pixels");
    this.matcher = matcher;
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void createDisplay() {
    JPanel backPanel = new JPanel(new BorderLayout());
    this.getContentPane().add(backPanel);

    statusLabel = new JLabel("Status Label");
    statusLabel.setPreferredSize(new Dimension(200, 100));
    backPanel.add(statusLabel, BorderLayout.PAGE_START);

    JPanel selectionPanel = new JPanel(new BorderLayout());
    backPanel.add(selectionPanel, BorderLayout.CENTER);

    // Target image selection
    targetImageChooser = new JFileChooser();
    FileFilter imageFilter = new FileNameExtensionFilter(
        "Image files", ImageIO.getReaderFileSuffixes());
    targetImageChooser.setFileFilter(imageFilter);

    JButton targetButton = new JButton("Choose target image");
    targetButton.addActionListener(targetImageButtonListener);
    selectionPanel.add(targetButton, BorderLayout.LINE_START);

    // Input directory selection
    JFileChooser inputDirectoryChooser = new JFileChooser();
    inputDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    JButton inputDirectoryButton = new JButton("Choose input directory");
    inputDirectoryButton.addActionListener(actionEvent -> {
      int chooseSuccess = inputDirectoryChooser.showOpenDialog(null);
      if (chooseSuccess == JFileChooser.APPROVE_OPTION) {
        matcher.setInputDirectory(inputDirectoryChooser.getSelectedFile());
      }
    });
    selectionPanel.add(inputDirectoryButton, BorderLayout.LINE_END);

    JPanel optionsPanel = new JPanel(new BorderLayout());

    // TODO: add option setters

    JButton runButton = new JButton("Run");
    runButton.addActionListener(actionEvent ->
        // Run in new thread to keep main thread free for user interactions
        new Thread(this::runPicturePixels).start());
    backPanel.add(runButton, BorderLayout.PAGE_END);

    this.pack();
  }

  // N.B: this is a variable that wraps behaviour
  private ActionListener targetImageButtonListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      int chooseSuccess = targetImageChooser.showOpenDialog(null);
      if (chooseSuccess == JFileChooser.APPROVE_OPTION) {
        try {
          BufferedImage targetImage = ImageIO.read(targetImageChooser.getSelectedFile());
          matcher.setTargetImage(targetImage);
        } catch (IOException e) {
          e.printStackTrace();
          // TODO: manage exception
        }
      }
    }
  };

  private void setStatus(String status, Color statusColor) {
    statusLabel.setForeground(statusColor);
    statusLabel.setText(status);
    this.repaint();
  }

  private void runPicturePixels() {
    // Set up observer for progress
    AtomicInteger progressCounter = new AtomicInteger(0);
    Observer progressObserver = () -> System.out.println(
        String.format("%d/%d: %s",
            progressCounter.incrementAndGet(),
            matcher.maxProgress(),
            progressCounter.get() < matcher.getInputDirectory().listFiles().length ? "Reading" : "Rereading"
        )
    );
    matcher.addObserver(progressObserver);

    // Generate targetTiles
    List<Tile> targetTiles = matcher.generateTilesFromImage();

    // Generate input tiles
    List<Tile> inputTiles = matcher.generateTilesFromDirectory();

    // Calculate match
    List<Tile> resultList = LeastDifference.nearestNeighbourMatch(
        inputTiles,
        targetTiles,
        matcher.getNumDuplicatesAllowed(),
        App.SEARCH_REPEATS,
        Tile.differenceFunction::absoluteDifference);


    // Generate resulting image
    BufferedImage resultImage
        = matcher.collateResultFromImages(resultList, App.TILE_RENDER_SIZE);

    // Write resulting image
    try {
      ImageIO.write(resultImage, "png", new File("output.png"));
      System.out.println("Written");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
