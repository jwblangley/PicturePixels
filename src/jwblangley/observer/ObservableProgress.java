package jwblangley.observer;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ObservableProgress extends Observable {

  private AtomicInteger progress;
  private int maxProgress;

  public ObservableProgress(int currentProgress, int maxProgress) {
    progress = new AtomicInteger(currentProgress);
    this.maxProgress = maxProgress;
  }

  public ObservableProgress(int maxProgress) {
    this(0, maxProgress);
  }

  public ObservableProgress() {
    // To be used in conjunction with setMaxProgress
    this(0, 0);
  }

  public void setMaxProgress(int maxProgress) {
    this.maxProgress = maxProgress;
  }

  public void resetProgress() {
    progress = new AtomicInteger(0);
  }

  public double getProgress() {
    return ((double) progress.get()) / ((double) maxProgress);
  }

  private void incrementProgress() {
    progress.incrementAndGet();
    notifyObservers();
  }

}
