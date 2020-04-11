package jwblangley.view;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import jwblangley.controller.GUIController;
import jwblangley.pictureMatching.PicturePixelMatcher;

public class GUIView {

  private Label statusLabel;
  private ProgressBar progressBar;

  private GUIController controller;

  public void setController(GUIController controller) {
    this.controller = controller;
  }

  public Pane layout(Stage window) {
    BorderPane backPane = new BorderPane();
    backPane.setPadding(new Insets(5));

    BorderPane statusPane = new BorderPane();
    backPane.setTop(statusPane);

    statusLabel = new Label("Select a target image to begin");
    statusLabel.setTextAlignment(TextAlignment.CENTER);
    statusPane.setCenter(statusLabel);

    progressBar = new ProgressBar();
    progressBar.prefWidthProperty().bind(statusPane.widthProperty());
    statusPane.setBottom(progressBar);

    BorderPane selectionPane = new BorderPane();
    backPane.setCenter(selectionPane);


    // Target image selection
    Button targetButton = new Button("Choose target image");
    targetButton.setOnAction(e -> {
      // Set up FileChooser
      FileChooser targetImageChooser = new FileChooser();
      targetImageChooser.setTitle("Choose target image");
      ExtensionFilter imageFilter = new ExtensionFilter("Image files", PicturePixelMatcher.IMAGE_READ_EXTENSIONS);
      targetImageChooser.getExtensionFilters().add(imageFilter);

      // Choose file
      File chosenFile = targetImageChooser.showOpenDialog(window);
      if (chosenFile != null) {
        try {
          BufferedImage targetImage = ImageIO.read(chosenFile);
          controller.setTargetImage(targetImage);
          updateStatusWithNumbers();
        } catch (IOException ex) {
          setStatus("Could not read target image", Color.RED);
        }
      }
    });
    BorderPane.setAlignment(targetButton, Pos.CENTER);
    selectionPane.setLeft(targetButton);


    // Input directory selection
    Button inputDirectoryButton = new Button("Choose input directory");
    inputDirectoryButton.setOnAction(e -> {
      DirectoryChooser inputDirectoryChooser = new DirectoryChooser();
      inputDirectoryChooser.setTitle("Choose input directory");
      File chosenDirectory = inputDirectoryChooser.showDialog(window);
      if (chosenDirectory != null) {
        controller.setSourceDirectory(chosenDirectory);
        updateStatusWithNumbers();
      }
    });
    BorderPane.setAlignment(inputDirectoryButton, Pos.CENTER);
    selectionPane.setRight(inputDirectoryButton);

    Button runButton = new Button("Run");
    runButton.setOnAction(e -> {
      // TODO: progressBar.setString(null);
      // TODO: progressBar.setMaximum(matcher.maxProgress());
      // Run in new thread to keep main thread free for user interactions
       new Thread(() -> controller.runPicturePixels(window)).start();
    });
    selectionPane.setCenter(runButton);

    // Option setters
    VBox optionsPane = new VBox(5);
    backPane.setBottom(optionsPane);

    GUIOptionSetter numDuplicatesSetter = new GUIOptionSetter("Number of duplicates allowed",
        String.valueOf(GUIController.DEFAULT_NUM_DUPLICATES_ALLOWED));
    numDuplicatesSetter.addObserver(() -> {
      try {
        int numDuplicates = Integer.parseInt(numDuplicatesSetter.getValue());
        if (numDuplicates < 1) {
          setStatus("Invalid parameter: # duplicates", Color.RED);
          return;
        }
        controller.setNumDuplicatesAllowed(numDuplicates);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: # duplicates", Color.RED);
      }
    });
    optionsPane.getChildren().add(numDuplicatesSetter.getLayout());

    GUIOptionSetter numSubtilesSetter = new GUIOptionSetter("Number of subtiles",
        String.valueOf(GUIController.DEFAULT_NUM_SUBTILES));
    numSubtilesSetter.addObserver(() -> {
      try {
        int numSubtiles = Integer.parseInt(numSubtilesSetter.getValue());
        if (numSubtiles < 1) {
          setStatus("Invalid parameter: # subtiles", Color.RED);
          return;
        }
        controller.setNumSubtiles(numSubtiles);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: # subtiles", Color.RED);
      }
    });
    optionsPane.getChildren().add(numSubtilesSetter.getLayout());

    GUIOptionSetter subtileSizeSetter = new GUIOptionSetter("Subtile match size",
        String.valueOf(GUIController.DEFAULT_SUBTILE_MATCH_SIZE));
    subtileSizeSetter.addObserver(() -> {
      try {
        int subtileSize = Integer.parseInt(subtileSizeSetter.getValue());
        if (subtileSize < 1) {
          setStatus("Invalid parameter: # subtile size", Color.RED);
          return;
        }
        controller.setSubtileMatchSize(subtileSize);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: subtile size", Color.RED);
      }
    });
    optionsPane.getChildren().add(subtileSizeSetter.getLayout());

    GUIOptionSetter tileRenderSetter = new GUIOptionSetter("Tile render size",
        String.valueOf(GUIController.DEFAULT_TILE_RENDER_SIZE));
    tileRenderSetter.addObserver(() -> {
      try {
        int tileRenderSize = Integer.parseInt(tileRenderSetter.getValue());
        if (tileRenderSize < 1) {
          setStatus("Invalid parameter: # tile render size", Color.RED);
          return;
        }
        controller.setTileRenderSize(tileRenderSize);
        updateStatusWithNumbers();
      } catch (NumberFormatException e) {
        setStatus("Invalid parameter: tile render size", Color.RED);
      }
    });
    optionsPane.getChildren().add(tileRenderSetter.getLayout());

    return backPane;
  }

  private void updateStatusWithNumbers() {
    if (controller.getTargetImage() == null) {
      setStatus("Please select a target image first", Color.RED);
      return;
    }

    if (controller.getSourceDirectory() == null) {
      setStatus("Please select an input directory", Color.RED);
      return;
    }

    Dimension resultDim = controller.resultDimension();
    setStatus(String.format("Inputs/Tiles: %d/%d, Result image: %dx%d",
        controller.numCurrentInputs(),
        controller.numInputsRequired(),
        resultDim.width,
        resultDim.height),
      controller.numCurrentInputs() > controller.numInputsRequired() ? Color.GREEN : Color.RED
    );
  }

  public void setStatus(String status, Color statusColor) {
    Platform.runLater(() -> {
      statusLabel.setTextFill(statusColor);
      statusLabel.setText(status);
    });
  }

  public void disableInputs() {
    // TODO
  }

  public void enableInputs() {
    // TODO
  }
}
