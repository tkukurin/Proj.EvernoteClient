package co.kukurin;

import co.kukurin.DefaultListModel;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Arrays;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultListModelTest {

    @Test
    public void shouldFireListenersOnSingleAdd() throws Exception {
        // given
        DefaultListModel<Object> givenModel = new DefaultListModel<>();
        ListDataListener givenMockedListener = Mockito.mock(ListDataListener.class);

        // when
        givenModel.addListDataListener(givenMockedListener);
        givenModel.add(new Object());

        // then
        ArgumentCaptor<ListDataEvent> eventCaptor = ArgumentCaptor.forClass(ListDataEvent.class);
        verify(givenMockedListener, times(1)).intervalAdded(eventCaptor.capture());

        ListDataEvent expected = new ListDataEvent(givenModel, ListDataEvent.INTERVAL_ADDED, 0, 1);
        then(eventCaptor.getValue()).isEqualToComparingOnlyGivenFields(expected, "source", "type", "index0", "index1");
    }

    @Test
    public void shouldFireListenersOnBatchAdd() throws Exception {
        // given
        DefaultListModel<Object> givenModel = new DefaultListModel<>();
        ListDataListener givenMockedListener = Mockito.mock(ListDataListener.class);

        // when
        givenModel.addListDataListener(givenMockedListener);
        givenModel.addAll(0, Arrays.asList(new Object(), new Object()));

        // then
        ArgumentCaptor<ListDataEvent> eventCaptor = ArgumentCaptor.forClass(ListDataEvent.class);
        verify(givenMockedListener, times(1)).intervalAdded(eventCaptor.capture());

        ListDataEvent expected = new ListDataEvent(givenModel, ListDataEvent.INTERVAL_ADDED, 0, 2);
        then(eventCaptor.getValue()).isEqualToComparingOnlyGivenFields(expected, "source", "type", "index0", "index1");
    }
}
