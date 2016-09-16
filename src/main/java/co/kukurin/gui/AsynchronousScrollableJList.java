package co.kukurin.gui;

import co.kukurin.async.DataSupplier;
import co.kukurin.async.DataSupplierInfo;
import co.kukurin.async.DataSupplierInfoFactory;
import co.kukurin.async.EvernoteExecutors;
import co.kukurin.custom.Optional;
import co.kukurin.gui.factories.ListenerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.runAsync;

@Slf4j
public class AsynchronousScrollableJList<T> extends JPanel {

    private volatile boolean updateInProgress;
    private Rectangle boundsRectangle;
    private final DataSupplier<T> dataSupplier;
    private final JScrollPane pane;

    public AsynchronousScrollableJList(DataSupplier<T> dataSupplier) {
        this(new JList<>(new DefaultListModel<>()), dataSupplier);
    }

    public Optional<T> getSelectedValue() { return Optional.ofNullable(getView().getSelectedValue()); }

    public DefaultListModel<T> getModel() {
        return (DefaultListModel<T>) getView().getModel();
    }

    private AsynchronousScrollableJList(JList<T> list, DataSupplier<T> dataSupplier) {
        this.pane = new JScrollPane(list);

        this.getView().setFixedCellWidth(200); // TODO handle differently
        this.dataSupplier = dataSupplier;

        this.pane.getViewport().addChangeListener(this::updateModelIfNecessaryOnScroll);
        this.pane.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateModelIfNecessaryOnResize));
        this.getView().addListSelectionListener(this::updateModelIfNecessaryOnSelectionChange);

        setLayout(new BorderLayout());
        add(this.pane, BorderLayout.CENTER);
    }

    private void updateModelIfNecessaryOnScroll(Object unused) {
        if(this.updateInProgress) {
            return;
        }

        int currentScrollBarPosition = this.pane.getVerticalScrollBar().getValue();
        int totalScrollBarHeight = this.pane.getVerticalScrollBar().getHeight();
        int maxScrollBarValue = this.pane.getVerticalScrollBar().getMaximum();

        if(isScrollbarNearUpdateRegion(currentScrollBarPosition, totalScrollBarHeight, maxScrollBarValue)) {
            runAsyncUpdate();
        }
    }

    // TODO doesn't take into account when scrollbar is near beginning
    // TODO also should take time since last update into account
    private boolean isScrollbarNearUpdateRegion(int currentScrollBarPosition, int totalScrollBarHeight, int maxScrollBarValue) {
        return (maxScrollBarValue - currentScrollBarPosition < totalScrollBarHeight || !this.pane.getVerticalScrollBar().isVisible());
    }

    private void updateModelIfNecessaryOnResize(Object unused) {
        if(this.updateInProgress) { // TODO implement some kind of updateQueue instead?
            return;
        }

        JList<?> view = getView();
        Optional.ofNullable(view.getCellBounds(view.getFirstVisibleIndex(), view.getLastVisibleIndex()))
                .filter(visibleCellBounds -> {
                    this.boundsRectangle = view.getBounds(this.boundsRectangle);
                    return this.boundsRectangle.getHeight() < visibleCellBounds.getHeight();
                })
                .ifPresent(resizedToLargerArea -> runAsyncUpdate());
    }

    private void updateModelIfNecessaryOnSelectionChange(ListSelectionEvent unused) {
        log.info("model size {}", this.getModel().getSize());

        boolean isSelectedLastItemInList = this.getModel().size() == this.getView().getMaxSelectionIndex() + 1;
        if(!this.updateInProgress && isSelectedLastItemInList) {
            runAsyncUpdate();
        }
    }

    private void runAsyncUpdate() {
        this.updateInProgress = true;
        log.info("running update.");
        runAsync(this::getNewBatchForCurrentlyActiveItems, EvernoteExecutors.defaultExecutor)
                .thenRun(() -> {
                    this.updateInProgress = false;
                    log.info("update done!");
                })
                .exceptionally(e -> {
                    log.info("exception occurred {}", e);
                    return null;
                });
    }

    private void getNewBatchForCurrentlyActiveItems() {
        DefaultListModel<T> model = getModel();
        DataSupplierInfo dataSupplierInfo = DataSupplierInfoFactory.getDataSupplier(model.getSize(), 5); // TODO change this.

        this.dataSupplier.getData(dataSupplierInfo).forEach(model::addElement);
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        this.getView().addListSelectionListener(listener);
    }

    public void setSelectedIndex(int index) {
        getView().setSelectedIndex(index);
    }

    @Override
    public boolean requestFocusInWindow() {
        return getView().requestFocusInWindow();
    }

    @SuppressWarnings("unchecked")
    private JList<T> getView() {
        return (JList<T>) this.pane.getViewport().getView();
    }
}
