package co.kukurin.custom.exception;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandler {

    public static void ignoreIfThrows(ThrowableRunnable runnable) {
        try { runnable.run(); }
        catch(Exception ignorable) {}
    }

    public static Optional<Exception> catchException(ThrowableRunnable runnable) {
        try { runnable.run(); }
        catch(Exception e) { return Optional.of(e); }

        return Optional.empty();
    }

    public static <T, U extends RuntimeException> ExceptionRemapper<T, U> usingMapping(
            Function<Exception, U> exceptionMapper) {
        return new ExceptionRemapper<>(exceptionMapper);
    }

    public static <T> T getOrRethrowAsUnchecked(ThrowableSupplier<T> supplier) {
        return getOrRemapAsUnchecked(supplier, RuntimeException::new);
    }

    public static <T, U extends RuntimeException> T getOrRethrowAsUnchecked(
            ThrowableSupplier<T> supplier,
            Supplier<U> exceptionSupplier) {
        try { return supplier.get(); }
        catch(Exception e) { throw exceptionSupplier.get(); }
    }

    public static <T, U extends RuntimeException> T getOrRemapAsUnchecked(
            ThrowableSupplier<T> supplier,
            Function<Exception, U> exceptionMapper) {
        try { return supplier.get(); }
        catch(Exception e) { throw exceptionMapper.apply(e); }
    }

    public static <T> Optional<T> tryGetValue(ThrowableSupplier<T> supplier) {
        try { return Optional.of(supplier.get()); }
        catch(Exception e) { return Optional.empty(); }
    }

    public static class ExceptionRemapper<T, U extends RuntimeException> {
        private Function<Exception, U> exceptionMapper;

        private ExceptionRemapper(Function<Exception, U> exceptionMapper) {
            this.exceptionMapper = exceptionMapper;
        }

        public T getOrRethrowAsUnchecked(ThrowableSupplier<T> supplier) {
            return getOrRemapAsUnchecked(supplier, exceptionMapper);
        }
    }

}
