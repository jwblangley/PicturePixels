package jwblangley.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jwblangley.observer.Observable;
import jwblangley.observer.Observer;

public class OptionSetter extends Container implements Observable {

  List<Observer> observers = new LinkedList<>();

  JTextField inputField;

  public OptionSetter(String title, String defaultValue) {
    this.setLayout(new BorderLayout());

    JLabel titleLabel = new JLabel(title);
    add(titleLabel, BorderLayout.LINE_START);

    inputField = new JTextField(defaultValue);
    inputField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        notifyObservers();
      }
    });
    add(inputField, BorderLayout.CENTER);
  }

  public OptionSetter(String title) {
    this(title, "");
  }

  public String getValue() {
    return inputField.getText();
  }

  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers() {
    observers.forEach(Observer::onNotified);
  }
}
