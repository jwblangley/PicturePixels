package jwblangley.difference;

@FunctionalInterface
public interface DifferenceFunction<T> {

  Integer apply(T a, T b);

  default Integer squareDifference(T a, T b) {
    return apply(a, b) * apply(a, b);
  }

  default Integer absoluteDifference(T a, T b) {
    return apply(a, b) < 0 ? apply(a, b) * -1 : apply(a, b);
  }
}