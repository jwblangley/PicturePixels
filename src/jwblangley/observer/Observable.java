package jwblangley.observer;

public interface Observable<P> {

  void addObserver(Observer<P> observer);

  void notifyObservers(P param);
}
