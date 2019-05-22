package jwblangley.difference;

@FunctionalInterface
public interface DifferenceFunction<T> {

  Long apply(T a, T b);

  default Long squareDifference(T a, T b) {
    return apply(a, b) * apply(a, b);
  }

  default Long absoluteDifference(T a, T b) {
    return apply(a, b) < 0 ? apply(a, b) * -1 : apply(a, b);
  }
}