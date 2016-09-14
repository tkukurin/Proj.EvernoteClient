package co.kukurin.custom.exception;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHelper {

    public static void ignoreIfThrows(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ignorable) {
        }
    }

    public static Optional<Exception> catchException(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            return Optional.of(e);
        }

        return Optional.empty();
    }

    // handlingExceptionAs ?
    public static ExceptionHandler usingExceptionHandler(Consumer<Exception> exceptionHandler) {
        return new ExceptionHandler(exceptionHandler);
    }

    public static ExceptionRemapper<RuntimeException> remappingOnException(Function<Exception, RuntimeException> exceptionMapper) {
        return new ExceptionRemapper<>(exceptionMapper);
    }

    public static <T> T getOrRethrowAsUnchecked(ThrowableSupplier<T> supplier) {
        return getOrRemapAsUnchecked(supplier, RuntimeException::new);
    }

    public static <T, U extends RuntimeException> T getOrRethrowAsUnchecked(ThrowableSupplier<T> valueSupplier,
                                                                            Supplier<U> exceptionIfValueSupplierFails) {
        try {
            return valueSupplier.get();
        } catch (Exception e) {
            throw exceptionIfValueSupplierFails.get();
        }
    }

    public static <T, U extends RuntimeException> T getOrRemapAsUnchecked(
            ThrowableSupplier<T> supplier,
            Function<Exception, U> exceptionMapper) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw exceptionMapper.apply(e);
        }
    }

    public static <T> Optional<T> tryGetValue(ThrowableSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class ExceptionRemapper<U extends RuntimeException> {
        private Function<Exception, U> exceptionMapper;

        private ExceptionRemapper(Function<Exception, U> exceptionMapper) {
            this.exceptionMapper = exceptionMapper;
        }

        public <T> T tryGetValue(ThrowableSupplier<T> supplier) {
            return getOrRemapAsUnchecked(supplier, exceptionMapper);
        }
    }

    public static class ExceptionHandler {
        private Consumer<Exception> exceptionHandler;

        public ExceptionHandler(Consumer<Exception> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        public void tryExecute(ThrowableRunnable runnable) {
            try {
                runnable.run();
            } catch (Exception e) {
                this.exceptionHandler.accept(e);
            }
        }
    }

}
