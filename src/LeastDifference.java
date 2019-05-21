import java.util.List;
import java.util.stream.IntStream;

public class LeastDifference {

  /*
  * Problem: Permute an unordered list to match against an ordered list yielding the minimal
  * total difference.
  * This problem can be mapped to the Travelling Sales Person and therefore an optimal approach
  * is unreasonable for large inputs.
  */


  public static <T> int totalDifference(List<T> input, List<T> toMatch, DifferenceFunction<T> diffFunc) {
    assert input.size() == toMatch.size() : "List sizes must match when comparing difference";

    return IntStream.range(0, input.size())
        .map(i -> diffFunc.difference(input.get(i), toMatch.get(i)))
        .sum();
  }

}
