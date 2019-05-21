public class LeastDifference {

  public static <T extends Difference<T>> int squareDifference(T a, T b) {
    return a.deltaFrom(b) * a.deltaFrom(b);
  }

}
