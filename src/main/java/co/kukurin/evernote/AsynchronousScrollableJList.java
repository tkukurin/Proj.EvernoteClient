package co.kukurin.evernote;

import co.kukurin.async.DataSupplier;
import co.kukurin.async.DataSupplierInfo;
import co.kukurin.async.DataSupplierInfoFactory;
import co.kukurin.custom.Optional;
import co.kukurin.gui.ListenerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class AsynchronousScrollableJList<T> extends JScrollPane {

    private volatile boolean updateInProgress;
    private Rectangle boundsRectangle;
    private final DataSupplier<T> dataSupplier;

    public AsynchronousScrollableJList(DefaultListModel<T> listModel,
                                       DataSupplier<T> dataSupplier) {
        this(new JList<>(listModel), dataSupplier);
    }

    private AsynchronousScrollableJList(JList<T> list, DataSupplier<T> dataSupplier) {
        super(list);
        this.getView().setFixedCellWidth(200);
        this.dataSupplier = dataSupplier;
        this.getViewport().addChangeListener(this::updateListModelIfNecessary);
        this.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateListModelIfNecessary));
    }

    private void updateListModelIfNecessary(Object ignoredEvent) {
        if(this.updateInProgress) {
            return;
        }

        JList<?> view = getView();
        Optional.ofNullable(view.getCellBounds(view.getFirstVisibleIndex(), view.getLastVisibleIndex()))
                .filter(visibleCellBounds -> {
                    this.boundsRectangle = view.getBounds(this.boundsRectangle);
                    return this.boundsRectangle.getHeight() < visibleCellBounds.getHeight();
                })
                .ifPresent(resizedToLargerArea -> runAsyncUpdate())
                .orElseDo(() -> {
                    int currentScrollBarPosition = this.getVerticalScrollBar().getValue();
                    int totalScrollBarHeight = this.getVerticalScrollBar().getHeight();
                    int maxScrollBarValue = this.getVerticalScrollBar().getMaximum();

                    if(shouldUpdate(currentScrollBarPosition, totalScrollBarHeight, maxScrollBarValue)) {
                        runAsyncUpdate();
                    }
                });

    }

    private void runAsyncUpdate() {
        this.updateInProgress = true;
        CompletableFuture
                .runAsync(this::getNewBatchForCurrentlyActiveItems)
                .thenRun(() -> this.updateInProgress = false);
    }

    private boolean shouldUpdate(int currentScrollBarPosition, int totalScrollBarHeight, int maxScrollBarValue) {
        return (maxScrollBarValue - currentScrollBarPosition < totalScrollBarHeight || !this.getVerticalScrollBar().isVisible());
    }

    private void getNewBatchForCurrentlyActiveItems() {
        DefaultListModel<T> model = getModel();
        DataSupplierInfo dataSupplierInfo = DataSupplierInfoFactory.getDataSupplier(model.getSize(), 5);

        this.dataSupplier.getData(dataSupplierInfo).forEach(model::addElement);
    }

    @SuppressWarnings("unchecked")
    private JList<T> getView() {
        return ((JList<T>) this.getViewport().getView());
    }
    private DefaultListModel<T> getModel() {
        return (DefaultListModel<T>) getView().getModel();
    }
}
