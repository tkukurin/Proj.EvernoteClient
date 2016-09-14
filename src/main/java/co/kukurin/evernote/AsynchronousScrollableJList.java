package co.kukurin.evernote;

import co.kukurin.async.DataSupplier;
import co.kukurin.async.DataSupplierInfo;
import co.kukurin.async.DataSupplierInfoFactory;
import co.kukurin.custom.Optional;
import co.kukurin.gui.ListenerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.*;

// TODO this should actually implement scroll pane as a delegate
// since e.g. AsynchronousScrollableJList should not allow add() method to be called on it.
public class AsynchronousScrollableJList<T> extends JPanel {

    private static final Executor notesFetchExecutor = newSingleThreadExecutor();

    private volatile boolean updateInProgress;
    private Rectangle boundsRectangle;
    private final DataSupplier<T> dataSupplier;
    private final JScrollPane pane;

    public AsynchronousScrollableJList(DefaultListModel<T> listModel,
                                       DataSupplier<T> dataSupplier) {
        this(new JList<>(listModel), dataSupplier);
    }

    public Optional<T> getSelectedValue() { return Optional.ofNullable(getView().getSelectedValue()); }

    public DefaultListModel<T> getModel() {
        return (DefaultListModel<T>) getView().getModel();
    }

    private AsynchronousScrollableJList(JList<T> list, DataSupplier<T> dataSupplier) {
        this.pane = new JScrollPane(list);

        this.getView().setFixedCellWidth(200);
        this.dataSupplier = dataSupplier;

        this.pane.getViewport().addChangeListener(this::updateModelIfNecessaryOnScroll);
        this.pane.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateModelIfNecessaryOnResize));

        setLayout(new BorderLayout());
        add(this.pane, BorderLayout.CENTER);
    }

    private void updateModelIfNecessaryOnScroll(Object ignoredEvent) {
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

    private void updateModelIfNecessaryOnResize(Object ignoredEvent) {
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

    private void runAsyncUpdate() {
        this.updateInProgress = true;
        runAsync(this::getNewBatchForCurrentlyActiveItems, notesFetchExecutor).thenRun(() -> this.updateInProgress = false);
    }

    private void getNewBatchForCurrentlyActiveItems() {
        DefaultListModel<T> model = getModel();
        DataSupplierInfo dataSupplierInfo = DataSupplierInfoFactory.getDataSupplier(model.getSize(), 5); // TODO change this.

        this.dataSupplier.getData(dataSupplierInfo).forEach(model::addElement);
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        this.getView().addListSelectionListener(listener);
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
