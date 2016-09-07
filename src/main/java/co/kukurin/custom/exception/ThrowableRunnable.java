package co.kukurin.custom.exception;

@FunctionalInterface
public interface ThrowableRunnable {
    void run() throws Exception;
}
