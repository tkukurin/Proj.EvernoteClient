package co.kukurin.async;

import co.kukurin.custom.Optional;
import co.kukurin.utils.ListenerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static java.util.concurrent.CompletableFuture.runAsync;

@Slf4j
public class AsynchronousJList<ListItemType, UpdaterReturnType> extends JPanel {

    private Rectangle boundsRectangle;
    private final AsynchronousUpdater<UpdaterReturnType> updater;
    private final JScrollPane pane;

    public AsynchronousJList(AsynchronousUpdater<UpdaterReturnType> updater,
                             co.kukurin.DefaultListModel<ListItemType> listModel) {
        this.pane = new JScrollPane(new JList<>(listModel));
        this.updater = updater;

        this.getView().setFixedCellWidth(200); // TODO handle differently

        this.pane.getViewport().addChangeListener(this::updateModelIfNecessaryOnScroll);
        this.pane.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateModelIfNecessaryOnResize));
        this.getView().addListSelectionListener(this::updateModelIfNecessaryOnSelectionChange);

        setLayout(new BorderLayout());
        add(this.pane, BorderLayout.CENTER);
    }

    public Optional<ListItemType> getSelectedValue() { return Optional.ofNullable(getView().getSelectedValue()); }

    public co.kukurin.DefaultListModel<ListItemType> getModel() {
        return (co.kukurin.DefaultListModel<ListItemType>) getView().getModel();
    }

    public void setModel(co.kukurin.DefaultListModel<ListItemType> model) {
        getView().setModel(model);
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        getView().addListSelectionListener(listener);
    }

    public void setSelectedIndex(int index) {
        getView().setSelectedIndex(index);
    }

    @Override
    public boolean requestFocusInWindow() {
        return getView().requestFocusInWindow();
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

    @SuppressWarnings("unchecked")
    private JList<ListItemType> getView() {
        return (JList<ListItemType>) this.pane.getViewport().getView();
    }
}
