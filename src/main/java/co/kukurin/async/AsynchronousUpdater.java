package co.kukurin.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Slf4j
public class AsynchronousUpdater<T> {

    private volatile boolean updateInProgress;
    private final Executor executor;
    private final DataSupplier<T> dataSupplier;
    private final Consumer<T> uponUpdateAcceptor;
    private final int fetchSize;

    public AsynchronousUpdater(DataSupplier<T> dataSupplier,
                               Consumer<T> uponUpdateAcceptor,
                               Executor executor,
                               int fetchSize) {
        this.dataSupplier = dataSupplier;
        this.uponUpdateAcceptor = uponUpdateAcceptor;
        this.fetchSize = fetchSize;
        this.executor = executor;
    }

    public boolean isUpdateInProgress() {
        return updateInProgress;
    }

    public void runAsyncUpdate(int startIndex) {
        this.runAsyncUpdate(DataSupplierInfoFactory.getDataSupplier(startIndex, this.fetchSize));
    }

    public void runAsyncUpdate(DataSupplierInfo dataSupplierInfo) {
        this.updateInProgress = true;
        log.info("running update.");

        completedFuture(dataSupplierInfo)
                .thenApplyAsync(this.dataSupplier::getData, this.executor)
                .thenAccept(fetchedItems -> {
                    this.updateInProgress = false;
                    this.uponUpdateAcceptor.accept(fetchedItems);
                })
                .exceptionally(e -> {
                    log.error("exception occurred fetching data", e);
                    return null;
                });
    }

}
