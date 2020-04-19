package jwblangley.difference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import jwblangley.observer.ObservableProgress;

public class LeastDifference extends ObservableProgress {

  /*
   * Problem: Permute a set to match against an ordered list yielding the minimal total difference.
   * This problem can be mapped to the Travelling Sales Person and therefore an optimal approach
   * is unreasonable for large inputs.
   *
   * N.B: the set can be larger than the target list.
   */

  // TODO: better matching algorithm(s)?

  // Basic greedy approach heuristic.
  // Needs all positive values -> uses absolute difference
  // Use set to remove duplicates. If duplicates are allowed, use numRepeatsAllowed
  private static <T> List<T> basicNearestNeighbourMatch(
      List<T> unorderedInput,
      List<T> target,
      int numRepeatsAllowed,
      DifferenceFunction<T> diffFunc,
      ObservableProgress progress) {

    assert unorderedInput.size() * numRepeatsAllowed >= target.size()
        : "Not enough input to match to target";

    List<T> result = new LinkedList<>();

    int[] used = new int[unorderedInput.size()];
    Arrays.fill(used, 0);

    for (int i = 0; i < target.size(); i++) {
      long minDiff = Integer.MAX_VALUE;
      int minIndex = -1;
      for (int j = 0; j < unorderedInput.size(); j++) {
        if (used[j] < numRepeatsAllowed) {
          long diff = diffFunc.absoluteDifference(target.get(i), unorderedInput.get(j));
          if (diff < minDiff) {
            minDiff = diff;
            minIndex = j;
          }
        }
      }
      result.add(unorderedInput.get(minIndex));
      used[minIndex]++;
      progress.incrementProgress();
    }
    return result;
  }

  // Since IntStreamRange is not ordered, this solves the blocky nature of the algorithm.
  private static <T> List<T> parallelNearestNeighbourMatch(
      List<T> unorderedInput,
      List<T> target,
      int numRepeatsAllowed,
      DifferenceFunction<T> diffFunc,
      ObservableProgress progress) {

    assert unorderedInput.size() * numRepeatsAllowed >= target.size()
        : "Not enough input to match to target";

    AtomicReferenceArray<T> atomicResult = new AtomicReferenceArray<>(target.size());

    AtomicIntegerArray used = new AtomicIntegerArray(unorderedInput.size());

    IntStream.range(0, target.size()).parallel()
        .forEach(i -> {
          long minDiff = Integer.MAX_VALUE;
          int minIndex = -1;
          for (int j = 0; j < unorderedInput.size(); j++) {
            if (used.getAndUpdate(j, IntUnaryOperator.identity()) < numRepeatsAllowed) {
              long diff = diffFunc.absoluteDifference(target.get(i), unorderedInput.get(j));
              if (diff < minDiff) {
                minDiff = diff;
                minIndex = j;
              }
            }
          }
          atomicResult.getAndSet(i, unorderedInput.get(minIndex));
          used.incrementAndGet(minIndex);
          progress.incrementProgress();
        });

    // Linked list as entirely insertion
    List<T> result = new LinkedList<>();

    // Sequentially add all elements
    // N.B: Non-atomic function get is okay here as this is now sequential
    for (int i = 0; i < target.size(); i++) {
      result.add(atomicResult.get(i));
    }
    return result;
  }

  // Shuffle input and repeat multiple times to yield better value
  public static <T> List<T> nearestNeighbourMatch(
      List<T> unorderedInput,
      List<T> target,
      int numRepeatsAllowed,
      int numShuffles,
      DifferenceFunction<T> diffFunc,
      boolean parallel,
      ObservableProgress observableProgress) {

    assert numShuffles > 0 : "Cannot repeat < 0 times";

    // Set up progress updates
    if (observableProgress != null) {
      observableProgress.resetProgress();
      observableProgress.setMaxProgress(target.size() * numShuffles);
    }

    long minTotal = Long.MAX_VALUE;
    List<T> bestResult = null;

    // Need to iterate through the set -> convert to list.
    List<T> input = new ArrayList<>(unorderedInput);

    for (int i = 0; i < numShuffles; i++) {
      Collections.shuffle(input);
      List<T> result;
      if (parallel) {
        result = parallelNearestNeighbourMatch(
            input,
            target,
            numRepeatsAllowed,
            diffFunc,
            observableProgress
        );
      } else {
        result = basicNearestNeighbourMatch(
            input,
            target,
            numRepeatsAllowed,
            diffFunc,
            observableProgress
        );
      }
      long totalDiff = totalDifference(result, target, diffFunc);
      if (totalDiff < minTotal) {
        minTotal = totalDiff;
        bestResult = result;
      }
    }
    return bestResult;
  }

  public static <T> List<T> nearestNeighbourMatch(
      List<T> unorderedInput,
      List<T> target,
      int numRepeatsAllowed,
      int numShuffles,
      DifferenceFunction<T> diffFunc,
      boolean parallel) {

    return nearestNeighbourMatch(
        unorderedInput,
        target,
        numRepeatsAllowed,
        numShuffles,
        diffFunc,
        parallel,
        null);
  }

  // Measures fitness
  public static <T> long totalDifference(List<T> input, List<T> target,
      DifferenceFunction<T> diffFunc) {
    assert input.size() == target.size() : "List sizes must match when comparing apply";

    return IntStream.range(0, input.size())
        .mapToLong(i -> diffFunc.apply(input.get(i), target.get(i)))
        .sum();
  }

}
