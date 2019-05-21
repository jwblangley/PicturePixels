import org.junit.Test;

import static org.junit.Assert.*;

public class LeastDifferenceTest {

  private class IntegerDifference implements Difference<IntegerDifference> {

    private final Integer val;

    private IntegerDifference(Integer val) {
      this.val = val;
    }

    @Override
    public int deltaFrom(IntegerDifference other) {
      return val - other.val;
    }
  }

  @Test
  public void squareDifferenceOfSelfIsZero() {
    IntegerDifference a = new IntegerDifference(1);
    assertEquals(LeastDifference.squareDifference(a, a), 0);
  }

}
