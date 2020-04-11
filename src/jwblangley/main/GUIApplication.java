package jwblangley.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jwblangley.controller.GUIController;
import jwblangley.pictureMatching.PicturePixelMatcher;
import jwblangley.view.GUIView;

public class GUIApplication extends Application {

  private final PicturePixelMatcher picturePixelMatcher = new PicturePixelMatcher();
  private final GUIController guiController = new GUIController(picturePixelMatcher, new GUIView());

  @Override
  public void start(Stage window) throws Exception {
    Scene scene = new Scene(guiController.getLayout(window));

    window.setTitle("PicturePixels");
    window.setScene(scene);
    window.centerOnScreen();
    window.show();
  }
}
