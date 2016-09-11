package co.kukurin.evernote;

import co.kukurin.gui.ListenerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AsynchronousScrollableJList<T> extends JScrollPane {

    private final ListWithAsyncMetadata<T> activeItems;
    private final Function<T, String> itemConverterToString;
    private final BiConsumer<Integer, ListWithAsyncMetadata<T>>  batchSupplier;
    private volatile boolean updateInProgress;
    private Rectangle boundsRectangle;

    public AsynchronousScrollableJList(ListWithAsyncMetadata<T> activeItems,
                                       Function<T, String> itemConverterToString,
                                       BiConsumer<Integer, ListWithAsyncMetadata<T>> batchSupplier) {
        this(new JList<>(activeItems.asModel(itemConverterToString)), activeItems, itemConverterToString, batchSupplier);
    }

    private AsynchronousScrollableJList(JList<String> list,
                                        ListWithAsyncMetadata<T> activeItems,
                                        Function<T, String> itemConverterToString,
                                        BiConsumer<Integer, ListWithAsyncMetadata<T>> batchSupplier) {
        super(list);

        this.itemConverterToString = itemConverterToString;
        this.activeItems = activeItems;
        this.batchSupplier = batchSupplier;

        this.getViewport().addChangeListener(this::updateListModelIfNecessary);
        this.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateListModelIfNecessary));
    }

    private void updateListModelIfNecessary(Object ignoredEvent) {
        JList<?> view = getView();

        Rectangle visibleCells = view.getCellBounds(view.getFirstVisibleIndex(), view.getLastVisibleIndex());
        this.boundsRectangle = view.getBounds(this.boundsRectangle);

        int currentScrollBarPosition = this.getVerticalScrollBar().getValue();
        int totalScrollBarHeight = this.getVerticalScrollBar().getHeight();
        int maxScrollBarValue = this.getVerticalScrollBar().getMaximum();

        if(!updateInProgress && shouldUpdate(currentScrollBarPosition, totalScrollBarHeight, maxScrollBarValue)) {
            this.updateInProgress = true;

            CompletableFuture
                    .runAsync(this::getNewBatchForCurrentlyActiveItems)
                    .thenRun(() -> {
                        this.updateInProgress = false;
                        SwingUtilities.invokeLater(this::updateListModelFromCurrentlyActive);
                    });
        }
    }

    private boolean shouldUpdate(int currentScrollBarPosition, int totalScrollBarHeight, int maxScrollBarValue) {
        return (maxScrollBarValue - currentScrollBarPosition < totalScrollBarHeight || !this.getVerticalScrollBar().isVisible());
    }

    private void getNewBatchForCurrentlyActiveItems() {
        // this.activeItems.hasMoreItems()
        this.activeItems.loadNewBatch();
        //this.batchSupplier.accept(this.activeItems.size(), this.activeItems);
    }

    private void updateListModelFromCurrentlyActive() {
        this.getView().setModel(this.activeItems.asModel(this.itemConverterToString));
    }

    @SuppressWarnings("unchecked")
    private JList<String> getView() {
        return ((JList<String>) this.getViewport().getView());
    }

}
