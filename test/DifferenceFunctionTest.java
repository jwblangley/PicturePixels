import jwblangley.difference.DifferenceFunction;
import org.junit.Test;

import static org.junit.Assert.*;

public class DifferenceFunctionTest {

  final DifferenceFunction<Long> longDifference = (a, b) -> a - b;

  @Test
  public void differenceOfSelfIsZero() {
    assertEquals(0, (long) longDifference.apply(1L, 1L));
  }

  @Test
  public void differenceOfDifferingElementsIsCorrect() {
    assertEquals(-4, (long) longDifference.apply(1L, 5L));
  }

  @Test
  public void absoluteDifferenceOfSelfIsZero() {
    assertEquals(0, (long) longDifference.absoluteDifference(1L, 1L));
  }

  @Test
  public void absoluteDifferenceOfDifferingElementsIsCorrect() {
    assertEquals(4, (long) longDifference.absoluteDifference(1L, 5L));
  }

  @Test
  public void squareDifferenceOfSelfIsZero() {
    assertEquals(0, (long) longDifference.squareDifference(1L, 1L));
  }

  @Test
  public void squareDifferenceOfDifferingElementsIsCorrect() {
    assertEquals(16, (long) longDifference.squareDifference(1L, 5L));
  }

}
