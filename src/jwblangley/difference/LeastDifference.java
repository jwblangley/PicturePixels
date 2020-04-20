package jwblangley.difference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    List<T> result = new ArrayList<>(Collections.nCopies(target.size(), null));

    int[] used = new int[unorderedInput.size()];
    Arrays.fill(used, 0);

    List<Integer> randOrder = IntStream.range(0, target.size())
        .boxed()
        .collect(Collectors.toList());
    Collections.shuffle(randOrder);

    for (int i : randOrder) {
      T targetItem = target.get(i);

      long minDiff = Integer.MAX_VALUE;
      int minIndex = -1;
      for (int j = 0; j < unorderedInput.size(); j++) {
        if (used[j] < numRepeatsAllowed) {
          long diff = diffFunc.absoluteDifference(targetItem, unorderedInput.get(j));
          if (diff < minDiff) {
            minDiff = diff;
            minIndex = j;
          }
        }
      }
      result.set(i, unorderedInput.get(minIndex));
      used[minIndex]++;

      // Progress update
      if (progress != null) {
        progress.incrementProgress();
      }
    }

    assert result.stream().noneMatch(Objects::isNull);

    return result;
  }

  // Shuffle input and repeat multiple times to yield better value
  public static <T> List<T> nearestNeighbourMatch(
      List<T> unorderedInput,
      List<T> target,
      int numRepeatsAllowed,
      int numShuffles,
      DifferenceFunction<T> diffFunc,
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
      List<T> result;

      result = basicNearestNeighbourMatch(
          input,
          target,
          numRepeatsAllowed,
          diffFunc,
          observableProgress
      );

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
      DifferenceFunction<T> diffFunc) {

    return nearestNeighbourMatch(
        unorderedInput,
        target,
        numRepeatsAllowed,
        numShuffles,
        diffFunc,
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
