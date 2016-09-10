package co.kukurin.evernote;

import co.kukurin.gui.ListenerFactory;
import lombok.Setter;

import javax.swing.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AsynchronousScrollableJList<T> extends JScrollPane {

    private final ListWithAsyncMetadata<T> activeItems;
    private final BiFunction<Integer, Integer, List<T>> batchSupplier;
    private int cachedVerticalScrollbarPosition;
    private int fetchSize = 50;

    public AsynchronousScrollableJList(ListWithAsyncMetadata<T> activeItems,
                                       Function<T, String> converterToString,
                                       BiFunction<Integer, Integer, List<T>> batchSupplier) {
        this(new JList<>(activeItems.asModel(converterToString)), activeItems, batchSupplier);
    }

    private AsynchronousScrollableJList(JList<String> list,
                                        ListWithAsyncMetadata<T> activeItems,
                                        BiFunction<Integer, Integer, List<T>> batchSupplier) {
        super(list);

        this.activeItems = activeItems;
        this.batchSupplier = batchSupplier;
        this.cachedVerticalScrollbarPosition = this.getVerticalScrollBar().getValue();

        this.getViewport().addChangeListener(this::updateListModel);
        this.getViewport().addComponentListener(ListenerFactory.createResizeListener(this::updateListModel));
    }

    // ne radi bas ono sto bi trebao, ali to je ideja.
    private void updateListModel(Object ignoredEvent) {
        int currentScrollBarPosition = this.getVerticalScrollBar().getValue();
        int totalScrollBarHeight = this.getVerticalScrollBar().getHeight();
        int maxScrollBarValue = this.getVerticalScrollBar().getMaximum();

        if(this.cachedVerticalScrollbarPosition != currentScrollBarPosition
                || !this.getVerticalScrollBar().isVisible()) {
            this.activeItems.addAll(batchSupplier.apply(this.activeItems.totalSize(), fetchSize));
            this.getView().setModel(this.activeItems.asModel());

            this.cachedVerticalScrollbarPosition = currentScrollBarPosition;
        }
    }

    @SuppressWarnings("unchecked")
    private JList<T> getView() {
        return ((JList<T>) this.getViewport().getView());
    }

}
