package jwblangley.difference;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class LeastDifference {

  /*
   * Problem: Permute a set to match against an ordered list yielding the minimal total difference.
   * This problem can be mapped to the Travelling Sales Person and therefore an optimal approach
   * is unreasonable for large inputs.
   *
   * N.B: the set can be larger than the target list.
   */

  // Basic greedy approach heuristic.
  // Use a list for input as we want to allow duplicates
  // Needs all positive values -> uses absolute difference
  public static <T> List<T> nearestNeighbourMatch(List<T> unorderedInput, List<T> target,
      DifferenceFunction<T> diffFunc) {

    List<T> result = new LinkedList<>();

    // N.B: initialised to all false
    boolean[] used = new boolean[unorderedInput.size()];

    for (int i = 0; i < target.size(); i++) {
      int minDiff = Integer.MAX_VALUE;
      int minIndex = -1;
      for (int j = 0; j < unorderedInput.size(); j++) {
        if (!used[j]) {
          int diff = diffFunc.absoluteDifference(target.get(i), unorderedInput.get(j));
          if (diff < minDiff) {
            minDiff = diff;
            minIndex = j;
          }
        }
      }
      result.add(unorderedInput.get(minIndex));
      used[minIndex] = true;
    }
    return result;
  }

  // Shuffle input and repeat multiple times to yield better value
  public static <T> List<T> repeatNearestNeighbourMatch(int n, List<T> unorderedInput,
      List<T> target, DifferenceFunction<T> diffFunc) {
    assert n > 0 : "Cannot repeat < 0 times";

    int minTotal = Integer.MAX_VALUE;
    List<T> bestResult = null;

    for (int i = 0; i < n; i++) {
      List<T> result = nearestNeighbourMatch(unorderedInput, target, diffFunc);
      int totalDiff = totalDifference(result, target, diffFunc);
      if (totalDiff < minTotal) {
        minTotal = totalDiff;
        bestResult = result;
      }
    }
    return bestResult;
  }

  public static <T> int totalDifference(List<T> input, List<T> target,
      DifferenceFunction<T> diffFunc) {
    assert input.size() == target.size() : "List sizes must match when comparing apply";

    return IntStream.range(0, input.size())
        .map(i -> diffFunc.apply(input.get(i), target.get(i)))
        .sum();
  }

}
