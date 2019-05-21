import org.junit.Test;

import static org.junit.Assert.*;

public class DifferenceFunctionTest {

  final DifferenceFunction<Integer> integerDifference = (a, b) -> a - b;

  @Test
  public void differenceOfSelfIsZero() {
    assertEquals(0, (int) integerDifference.apply(1,1));
  }

  @Test
  public void differenceOfDifferingElementsIsCorrect(){
    assertEquals(-4, (int) integerDifference.apply(1, 5));
  }

  @Test
  public void absoluteDifferenceOfSelfIsZero() {
    assertEquals(0, (int) integerDifference.absoluteDifference(1,1));
  }

  @Test
  public void absoluteDifferenceOfDifferingElementsIsCorrect(){
    assertEquals(4, (int) integerDifference.absoluteDifference(1, 5));
  }

  @Test
  public void squareDifferenceOfSelfIsZero() {
    assertEquals(0, (int) integerDifference.squareDifference(1,1));
  }

  @Test
  public void squareDifferenceOfDifferingElementsIsCorrect(){
    assertEquals(16, (int) integerDifference.squareDifference(1, 5));
  }

}
