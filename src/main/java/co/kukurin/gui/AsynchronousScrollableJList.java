package co.kukurin.gui;

import co.kukurin.async.DataSupplier;
import co.kukurin.custom.Optional;
import co.kukurin.gui.factories.ListenerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static java.util.concurrent.CompletableFuture.runAsync;

@Slf4j
public class AsynchronousScrollableJList<T> extends JPanel {

    private Rectangle boundsRectangle;
    private final AsynchronousUpdater<T> updater;
    private final JScrollPane pane;

    public AsynchronousScrollableJList(DataSupplier<T> dataSupplier, int fetchSize) {
        this(new JList<>(new AsynchronousListModel<>()), dataSupplier, fetchSize);
    }

    public Optional<T> getSelectedValue() { return Optional.ofNullable(getView().getSelectedValue()); }

    public AsynchronousListModel<T> getModel() {
        return (AsynchronousListModel<T>) getView().getModel();
    }
    public void setModel(AsynchronousListModel<T> model) {
        getView().setModel(model);
    }

    private AsynchronousScrollableJList(JList<T> list, DataSupplier<T> dataSupplier, int fetchSize) {
        this.pane = new JScrollPane(list);

        this.getView().setFixedCellWidth(200); // TODO handle differently
        this.updater = new AsynchronousUpdater<>(dataSupplier, getModel()::addAll, fetchSize);

        this.pane.getViewport().addChangeListener(this::updateModelIfNecessaryOnScroll);
        this.pane.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateModelIfNecessaryOnResize));
        this.getView().addListSelectionListener(this::updateModelIfNecessaryOnSelectionChange);

        setLayout(new BorderLayout());
        add(this.pane, BorderLayout.CENTER);
    }

    private void updateModelIfNecessaryOnScroll(Object unused) {
        if(isUpdateInProgress()) {
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
        if(isUpdateInProgress()) { // TODO implement some kind of updateQueue instead?
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

        boolean isSelectedLastItemInList = this.getModel().getSize() == this.getView().getMaxSelectionIndex() + 1;
        if(!isUpdateInProgress() && isSelectedLastItemInList) {
            runAsyncUpdate();
        }
    }

    private boolean isUpdateInProgress() {
        return this.updater.isUpdateInProgress();
    }

    private void runAsyncUpdate() {
        this.updater.runAsyncUpdate(getModel().getSize());
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
