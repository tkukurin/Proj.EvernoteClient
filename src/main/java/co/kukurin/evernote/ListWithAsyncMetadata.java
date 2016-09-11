package co.kukurin.evernote;

import lombok.experimental.Delegate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO rename this
// TODO synchronizer (i.e., cache total size locally and then update only when synchronization is requested.)
// TODO evernote always returns notes newer -> older? => older will never be added.
public class ListWithAsyncMetadata<E> {

    private @Delegate ArrayList<E> currentlyActive;
    private BiConsumer<Integer, ListWithAsyncMetadata<E>> batchSupplier;
    private Supplier<Integer> totalNumberOfItemsFetchableSupplier;

    public ListWithAsyncMetadata(BiConsumer<Integer, ListWithAsyncMetadata<E>> batchSupplier,
                                 Supplier<Integer> totalNumberOfItemsFetchableSupplier) {
        this.batchSupplier = batchSupplier;
        this.totalNumberOfItemsFetchableSupplier = totalNumberOfItemsFetchableSupplier;
        this.currentlyActive = new ArrayList<>();
    }

    public boolean hasMoreItems() {
        return currentlyActive.size() < totalNumberOfItemsFetchableSupplier.get();
    }

    public void loadNewBatch() {
        this.batchSupplier.accept(this.size(), this);
    }

    public ListModel<E> asModel() {
        return asModel(Function.identity());
    }

    public <R> ListModel<R> asModel(Function<E, R> converter) {
        return new AbstractListModel<R>() {
            @Override
            public int getSize() {
                return currentlyActive.size();
            }

            @Override
            public R getElementAt(int index) {
                E element = currentlyActive.get(index);
                return converter.apply(element);
            }
        };
    }
}
