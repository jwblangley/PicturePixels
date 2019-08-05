package jwblangley.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
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

  private JPanel selectionPanel, optionsPanel;
  private JLabel statusLabel;
  private JFileChooser targetImageChooser;
  private JProgressBar progressBar;

  public PicturePixelView(PicturePixelMatcher matcher) {
    super("Picture by pixels");
    this.matcher = matcher;
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public boolean checkValidInputs() {
    if (matcher.getTargetImage() == null || matcher.getInputDirectory() == null) {
      if (matcher.getTargetImage() == null) {
        setStatus("Please select a target image", Color.RED);
      } else {
        setStatus("Please select an input directory", Color.RED);
      }
      return false;
    }
    return true;
  }

  public void createDisplay() {
    JPanel backPanel = new JPanel(new BorderLayout());
    this.getContentPane().add(backPanel);

    JPanel statusPanel = new JPanel(new BorderLayout());
    backPanel.add(statusPanel, BorderLayout.PAGE_START);

    statusLabel = new JLabel("Select a target image to begin");
    statusLabel.setPreferredSize(new Dimension(750, 200));
    statusLabel.setHorizontalAlignment(JLabel.CENTER);
    statusPanel.add(statusLabel, BorderLayout.CENTER);

    progressBar = new JProgressBar();
    progressBar.setSize(new Dimension(500, 25));
    progressBar.setStringPainted(true);
    progressBar.setMinimum(0);
    statusPanel.add(progressBar, BorderLayout.PAGE_END);

    selectionPanel = new JPanel(new BorderLayout());
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
        matcher.setInputDirectoryAndRecurseSelect(inputDirectoryChooser.getSelectedFile());
        updateStatusWithNumbers();
      }
    });
    selectionPanel.add(inputDirectoryButton, BorderLayout.LINE_END);

    JButton runButton = new JButton("Run");
    runButton.addActionListener(actionEvent -> {
      if (checkValidInputs()) {
        progressBar.setMaximum(matcher.maxProgress());
        // Run in new thread to keep main thread free for user interactions
        new Thread(App::runPicturePixels).start();
      }
    });
    selectionPanel.add(runButton, BorderLayout.PAGE_END);

    // Option setters

    optionsPanel = new JPanel();
    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
    backPanel.add(optionsPanel, BorderLayout.PAGE_END);

    OptionSetter numDuplicatesSetter = new OptionSetter("Number of duplicates allowed",
        String.valueOf(App.DEFAULT_NUM_DUPLICATES_ALLOWED));
    numDuplicatesSetter.addObserver(() -> {
      try {
        int numDuplicates = Integer.parseInt(numDuplicatesSetter.getValue());
        if (numDuplicates < 1) {
          setStatus("Invalid parameter: # duplicates", Color.RED);
          return;
        }
        matcher.setNumDuplicatesAllowed(numDuplicates);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: # duplicates", Color.RED);
      }
    });
    optionsPanel.add(numDuplicatesSetter);

    OptionSetter numSubtilesSetter = new OptionSetter("Number of subtiles",
        String.valueOf(App.DEFAULT_NUM_SUBTILES));
    numSubtilesSetter.addObserver(() -> {
      try {
        int numSubtiles = Integer.parseInt(numSubtilesSetter.getValue());
        if (numSubtiles < 1) {
          setStatus("Invalid parameter: # subtiles", Color.RED);
          return;
        }
        matcher.setNumSubtiles(numSubtiles);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: # subtiles", Color.RED);
      }
    });
    optionsPanel.add(numSubtilesSetter);

    OptionSetter subtileSizeSetter = new OptionSetter("Subtile match size",
        String.valueOf(App.DEFAULT_SUBTILE_MATCH_SIZE));
    subtileSizeSetter.addObserver(() -> {
      try {
        int subtileSize = Integer.parseInt(subtileSizeSetter.getValue());
        if (subtileSize < 1) {
          setStatus("Invalid parameter: # subtile size", Color.RED);
          return;
        }
        matcher.setSubtileMatchSize(subtileSize);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: subtile size", Color.RED);
      }
    });
    optionsPanel.add(subtileSizeSetter);

    OptionSetter tileRenderSetter = new OptionSetter("Tile render size",
        String.valueOf(App.DEFAULT_TILE_RENDER_SIZE));
    tileRenderSetter.addObserver(() -> {
      try {
        int tileRenderSize = Integer.parseInt(tileRenderSetter.getValue());
        if (tileRenderSize < 1) {
          setStatus("Invalid parameter: # tile render size", Color.RED);
          return;
        }
        matcher.setTileRenderSize(tileRenderSize);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: tile render size", Color.RED);
      }
    });
    optionsPanel.add(tileRenderSetter);

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
          updateStatusWithNumbers();
        } catch (IOException e) {
          setStatus("Could not read target image", Color.RED);
        }
      }
    }
  };

  private void updateStatusWithNumbers() {
    if (checkValidInputs()) {
      Dimension resultDim = matcher.resultDimension();
      setStatus(
          String.format("Inputs: %d/%d, Number of tiles: %d, Result image: %dx%d",
              matcher.numCurrentInputs(), matcher.inputsRequired(), matcher.inputsRequired(),
              resultDim.width, resultDim.height),
          matcher.numCurrentInputs() > matcher.inputsRequired() ? Color.GREEN : Color.RED
      );
    }
  }

  public void setStatus(String status, Color statusColor) {
    statusLabel.setForeground(statusColor);
    statusLabel.setText(status);
    this.repaint();
  }

  public void setProgress(int progress) {
    progressBar.setValue(progress);
  }

  public void setProgress(String s) {
    progressBar.setString(s);
  }

  public void disableInputs() {
    setAllComponentsEnabled(selectionPanel, false);
    setAllComponentsEnabled(optionsPanel, false);
  }

  public void enableInputs() {
    setAllComponentsEnabled(selectionPanel, true);
    setAllComponentsEnabled(optionsPanel, true);
  }

  private void setAllComponentsEnabled(Container rootComponent, boolean enabled) {
    rootComponent.setEnabled(enabled);
    for (Component component : rootComponent.getComponents()) {
      if (component instanceof Container) {
        setAllComponentsEnabled((Container) component, enabled);
      }
      component.setEnabled(enabled);
    }
  }
}
