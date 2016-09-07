package co.kukurin.custom.exception;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    T get() throws Exception;
}
