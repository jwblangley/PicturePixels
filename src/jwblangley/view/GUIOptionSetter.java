package jwblangley.view;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jwblangley.observer.Observable;

public class GUIOptionSetter extends Observable {

  private final TextField inputField;
  private final Pane layout;

  public GUIOptionSetter(String title, String defaultValue) {
    layout = new HBox(5);

    Label titleLabel = new Label(title);
    inputField = new TextField(defaultValue);
    inputField.setOnKeyReleased(e -> notifyObservers());

    layout.getChildren().addAll(titleLabel, inputField);
  }

  public GUIOptionSetter(String title) {
    this(title, "");
  }

  public Pane getLayout() {
    return layout;
  }

  public String getValue() {
    return inputField.getText();
  }

}
