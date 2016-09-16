package co.kukurin.gui;

import co.kukurin.async.DataSupplier;
import co.kukurin.async.DataSupplierInfo;
import co.kukurin.async.DataSupplierInfoFactory;
import co.kukurin.async.EvernoteExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Slf4j
public class AsynchronousUpdater<T> {

    private volatile boolean updateInProgress;
    private final DataSupplier<T> dataSupplier;
    private final Consumer<Collection<T>> uponUpdateAcceptor;
    private final int fetchSize;

    public AsynchronousUpdater(DataSupplier<T> dataSupplier,
                               Consumer<Collection<T>> uponUpdateAcceptor, int fetchSize) {
        this.dataSupplier = dataSupplier;
        this.uponUpdateAcceptor = uponUpdateAcceptor;
        this.fetchSize = fetchSize;
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
                .thenApplyAsync(this.dataSupplier::getData, EvernoteExecutors.defaultExecutor)
                .thenAccept(notes -> {
                    this.updateInProgress = false;
                    this.uponUpdateAcceptor.accept(notes);
                })
                .exceptionally(e -> {
                    log.error("exception occurred fetching data", e);
                    return null;
                });
    }

}
