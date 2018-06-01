package marina_CKY;


/**
 * Filters are boolean functions which accept or reject items.
 */
public interface MyVal<T> {
  boolean accept(T t);
}