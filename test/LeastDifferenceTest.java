import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jwblangley.difference.DifferenceFunction;
import jwblangley.difference.LeastDifference;
import org.junit.Test;

public class LeastDifferenceTest {

  private final DifferenceFunction<Long> longDifference = (a, b) -> a - b;

  @Test
  public void totalDifferenceOfSingleSameElementIsCorrect() {
    List<Long> a = Collections.singletonList(1L);
    List<Long> b = Collections.singletonList(1L);

    long diff = LeastDifference.totalDifference(a, b, longDifference);

    assertEquals(0, diff);
  }

  @Test
  public void totalDifferenceOfSingleElementIsCorrect() {
    List<Long> a = Collections.singletonList(1L);
    List<Long> b = Collections.singletonList(2L);

    long diff = LeastDifference.totalDifference(a, b, longDifference);

    assertEquals(-1, diff);
  }

  @Test
  public void totalAbsoluteDifferenceOfSingleElementIsCorrect() {
    List<Long> a = Collections.singletonList(1L);
    List<Long> b = Collections.singletonList(2L);

    long diff = LeastDifference.totalDifference(a, b, longDifference::absoluteDifference);

    assertEquals(1, diff);
  }

  @Test
  public void totalSquareDifferenceOfSingleElementIsCorrect() {
    List<Long> a = Collections.singletonList(1L);
    List<Long> b = Collections.singletonList(3L);

    long diff = LeastDifference.totalDifference(a, b, longDifference::squareDifference);

    assertEquals(4, diff);
  }

  @Test
  public void totalDifferenceOfElementsIsCorrect() {
    List<Long> a = Arrays.asList(1L, 2L);
    List<Long> b = Arrays.asList(2L, 3L);

    long diff = LeastDifference.totalDifference(a, b, longDifference);

    assertEquals(-2, diff);
  }

  @Test
  public void totalAbsoluteDifferenceOfElementsIsCorrect() {
    List<Long> a = Arrays.asList(1L, 2L);
    List<Long> b = Arrays.asList(2L, 3L);

    long diff = LeastDifference.totalDifference(a, b, longDifference::absoluteDifference);

    assertEquals(2, diff);
  }

  @Test
  public void totalSquareDifferenceOfElementsIsCorrect() {
    List<Long> a = Arrays.asList(1L, 2L);
    List<Long> b = Arrays.asList(3L, 4L);

    long diff = LeastDifference.totalDifference(a, b, longDifference::squareDifference);

    assertEquals(8, diff);
  }

  @Test
  public void nearestNeighbourWithTargetAlreadyGivenIsCorrect() {
    List<Long> a = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    List<Long> b = Arrays.asList(1L, 2L, 3L, 4L, 5L);

    List<Long> result = LeastDifference
        .nearestNeighbourMatch(a, b, 1, 1, longDifference::absoluteDifference);
    assertArrayEquals(new Long[]{1L, 2L, 3L, 4L, 5L}, result.toArray());
  }

}
