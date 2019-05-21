import java.util.Arrays;
import java.util.List;
import jwblangley.difference.DifferenceFunction;
import jwblangley.difference.LeastDifference;
import org.junit.Test;

import static org.junit.Assert.*;

public class LeastDifferenceTest {

  final DifferenceFunction<Integer> integerDifference = (a, b) -> a - b;

  @Test
  public void totalDifferenceOfSingleSameElementIsCorrect() {
    List<Integer> a = Arrays.asList(1);
    List<Integer> b = Arrays.asList(1);

    int diff = LeastDifference.totalDifference(a, b, integerDifference);

    assertEquals(0, diff);
  }

  @Test
  public void totalDifferenceOfSingleElementIsCorrect() {
    List<Integer> a = Arrays.asList(1);
    List<Integer> b = Arrays.asList(2);

    int diff = LeastDifference.totalDifference(a, b, integerDifference);

    assertEquals(-1, diff);
  }

  @Test
  public void totalAbsoluteDifferenceOfSingleElementIsCorrect() {
    List<Integer> a = Arrays.asList(1);
    List<Integer> b = Arrays.asList(2);

    int diff = LeastDifference.totalDifference(a, b, integerDifference::absoluteDifference);

    assertEquals(1, diff);
  }

  @Test
  public void totalSquareDifferenceOfSingleElementIsCorrect() {
    List<Integer> a = Arrays.asList(1);
    List<Integer> b = Arrays.asList(3);

    int diff = LeastDifference.totalDifference(a, b, integerDifference::squareDifference);

    assertEquals(4, diff);
  }

  @Test
  public void totalDifferenceOfElementsIsCorrect() {
    List<Integer> a = Arrays.asList(1, 2);
    List<Integer> b = Arrays.asList(2, 3);

    int diff = LeastDifference.totalDifference(a, b, integerDifference);

    assertEquals(-2, diff);
  }

  @Test
  public void totalAbsoluteDifferenceOfElementsIsCorrect() {
    List<Integer> a = Arrays.asList(1, 2);
    List<Integer> b = Arrays.asList(2, 3);

    int diff = LeastDifference.totalDifference(a, b, integerDifference::absoluteDifference);

    assertEquals(2, diff);
  }

  @Test
  public void totalSquareDifferenceOfElementsIsCorrect() {
    List<Integer> a = Arrays.asList(1, 2);
    List<Integer> b = Arrays.asList(3, 4);

    int diff = LeastDifference.totalDifference(a, b, integerDifference::squareDifference);

    assertEquals(8, diff);
  }

  @Test
  public void nearestNeighbourWithTargetAlreadyGivenIsCorrect() {
    List<Integer> a = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> b = Arrays.asList(1, 2, 3, 4, 5);

    List<Integer> result = LeastDifference.nearestNeighbourMatch(a,b,integerDifference::absoluteDifference);
    assertArrayEquals(new Integer[]{1,2,3,4,5}, result.toArray());
  }

}
