@FunctionalInterface
public interface DifferenceFunction<T> {
  Integer difference(T a, T b);

  default Integer squareDifference(T a, T b) {
    return difference(a, b) * difference(a, b);
  }

  default Integer absoluteDifference(T a, T b) {
    return difference(a, b) < 0 ? difference(a,b) * -1 : difference(a, b);
  }
}