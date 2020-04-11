package jwblangley.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable {

  List<Observer> observers = new ArrayList<>();

  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  public void notifyObservers() {
    observers.forEach(Observer::onNotified);
  }
}
