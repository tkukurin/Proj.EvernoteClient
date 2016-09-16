package co.kukurin.gui;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class AsynchronousListModel<T> extends AbstractListModel<T> {

    private ArrayList<T> coreItems;

    public AsynchronousListModel() {
        this(new ArrayList<>());
    }

    public AsynchronousListModel(Collection<T> coreItems) {
        this.coreItems = new ArrayList<>(coreItems);
    }

    @Override
    public int getSize() {
        return this.coreItems.size();
    }

    @Override
    public T getElementAt(int index) {
        return this.coreItems.get(index);
    }

    public void add(T item) {
        this.coreItems.add(item);
        fireIntervalAdded(this.coreItems.size() - 1, this.coreItems.size());
    }
    public void add(int index, T item) {
        this.coreItems.add(index, item);
        fireIntervalAdded(index, index + 1);
    }

    public void addAll(Collection<T> items) {
        this.coreItems.addAll(items);
        fireIntervalAdded(this.coreItems.size() - items.size(), this.coreItems.size());
    }
    public void addAll(int index, Collection<T> items) {
        this.coreItems.addAll(index, items);
        fireIntervalAdded(index, index + items.size());
    }

    // TODO setItems ?
    // or just leave as-is ( list.setModel(...) ) ?
//    public void replaceModel(Collection<T> items) {
//        int oldSize = this.coreItems.size();
//        this.coreItems = new ArrayList<>(items);
//
//        fireIntervalRemoved(0, oldSize);
//        fireIntervalAdded(0, items.size());
//    }

    protected void fireContentsChanged(int index0, int index1) {
        ListDataListener[] listeners = listenerList.getListeners(ListDataListener.class);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index0, index1);
        Arrays.stream(listeners).forEach(listener -> listener.contentsChanged(e));
    }

    protected void fireIntervalAdded(int index0, int index1) {
        ListDataListener[] listeners = listenerList.getListeners(ListDataListener.class);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index0, index1);
        Arrays.stream(listeners).forEach(listener -> listener.intervalAdded(e));
    }

    protected void fireIntervalRemoved(int index0, int index1) {
        ListDataListener[] listeners = listenerList.getListeners(ListDataListener.class);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
        Arrays.stream(listeners).forEach(listener -> listener.intervalRemoved(e));
    }
}
