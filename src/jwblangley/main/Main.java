package jwblangley.main;

import javafx.application.Application;

public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      Application.launch(GUIApplication.class);
    }
  }

}
