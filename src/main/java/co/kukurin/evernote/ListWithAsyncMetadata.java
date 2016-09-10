package co.kukurin.evernote;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ListWithAsyncMetadata<T> {

    private static final int itemsBeforeCopy = 200;

    private final ArrayList<T> currentlyActive;
    private List<T> futures;

    public ListWithAsyncMetadata(List<T> currentlyActive) {
        this.currentlyActive = new ArrayList<>(currentlyActive);
        this.futures = new LinkedList<>();
    }

    public void add(T element) {
        this.futures.add(element);
        appendFuturesToArrayListIfRequired();
    }

    public void addAll(Collection<T> elements) {
        this.futures.addAll(elements);
        appendFuturesToArrayListIfRequired();
    }

    private void appendFuturesToArrayListIfRequired() {
        if(this.futures.size() > itemsBeforeCopy) {
            this.currentlyActive.addAll(this.futures);
            this.futures = new LinkedList<T>();
        }
    }

    public T get(int index) {
        return currentlyActive.get(index);
    }

    public int activeSize() {
        return currentlyActive.size();
    }
    public int totalSize() { return currentlyActive.size() + futures.size(); }

    public ListModel<T> asModel() {
        return asModel(Function.identity());
    }

    public <R> ListModel<R> asModel(Function<T, R> converter) {
        return new AbstractListModel<R>() {
            @Override
            public int getSize() {
                return currentlyActive.size() + futures.size();
            }

            @Override
            public R getElementAt(int index) {
                int activeSize = currentlyActive.size();
                T element = index >= activeSize
                        ? futures.get(index - activeSize)
                        : currentlyActive.get(index);

                return converter.apply(element);
            }
        };
    }
}
