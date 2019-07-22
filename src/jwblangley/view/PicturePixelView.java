package jwblangley.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import jwblangley.app.App;
import jwblangley.pictureMatching.PicturePixelMatcher;

public class PicturePixelView extends JFrame {

  private final PicturePixelMatcher matcher;

  private JLabel statusLabel;
  private JFileChooser targetImageChooser;
  private JProgressBar progressBar;

  public PicturePixelView(PicturePixelMatcher matcher) {
    super("Picture by pixels");
    this.matcher = matcher;
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void createDisplay() {
    JPanel backPanel = new JPanel(new BorderLayout());
    this.getContentPane().add(backPanel);

    JPanel statusPanel = new JPanel(new BorderLayout());
    backPanel.add(statusPanel, BorderLayout.PAGE_START);

    statusLabel = new JLabel("Status Label");
    statusLabel.setPreferredSize(new Dimension(500, 100));
    statusLabel.setHorizontalAlignment(JLabel.CENTER);
    statusPanel.add(statusLabel, BorderLayout.CENTER);

    progressBar = new JProgressBar();
    progressBar.setSize(new Dimension(500, 25));
    progressBar.setStringPainted(true);
    progressBar.setMinimum(0);
    statusPanel.add(progressBar, BorderLayout.PAGE_END);

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

    JButton runButton = new JButton("Run");
    runButton.addActionListener(actionEvent ->
        // Run in new thread to keep main thread free for user interactions
        new Thread(App::runPicturePixels).start());
    selectionPanel.add(runButton, BorderLayout.PAGE_END);

    JPanel optionsPanel = new JPanel(new BorderLayout());
    backPanel.add(optionsPanel, BorderLayout.PAGE_END);

    // TODO: add option setters

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

  public void setStatus(String status, Color statusColor) {
    statusLabel.setForeground(statusColor);
    statusLabel.setText(status);
    this.repaint();
  }

}
