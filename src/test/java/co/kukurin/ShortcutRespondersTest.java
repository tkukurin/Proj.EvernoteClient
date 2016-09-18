package co.kukurin;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static org.mockito.Mockito.*;

public class ShortcutRespondersTest {

    @Test
    public void shouldCallRunnableOnlyOnKeyReleaseEvent() throws Exception {
        // given
        KeyEvent givenKeyPressEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 1L, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        KeyEvent givenKeyReleaseEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_RELEASED, 1L, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        Runnable givenMockedRunnable = mock(Runnable.class);
        ShortcutResponders givenResponders = new ShortcutResponders();

        // when
        givenResponders.addKeyReleaseEvent(e -> e.isShiftDown() && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F, givenMockedRunnable);
        givenResponders.eventInvoked(givenKeyPressEvent);
        givenResponders.eventInvoked(givenKeyReleaseEvent);

        // then
        verify(givenMockedRunnable, times(1)).run();
    }

    @Test
    public void shouldCorrectlyAddEventFromString() throws Exception {
        // given
        KeyEvent givenKeyPressEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 1L, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        KeyEvent givenKeyReleaseEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_RELEASED, 1L, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        Runnable givenMockedRunnable = mock(Runnable.class);
        ShortcutResponders givenResponders = new ShortcutResponders();

        // when
        givenResponders.addKeyReleaseEvent("ctrl shift F", givenMockedRunnable);
        givenResponders.eventInvoked(givenKeyPressEvent);
        givenResponders.eventInvoked(givenKeyReleaseEvent);

        // then
        verify(givenMockedRunnable, times(1)).run();
    }

    @Test
    public void shouldNotRespondToEventsNotContainingAllKeys() throws Exception {
        // given
        KeyEvent givenKeyPressEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_PRESSED, 1L, KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        KeyEvent givenKeyReleaseEvent = new KeyEvent(new JFrame(), KeyEvent.KEY_RELEASED, 1L,KeyEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, 'F');
        Runnable givenMockedRunnable = mock(Runnable.class);
        ShortcutResponders givenResponders = new ShortcutResponders();

        // when
        givenResponders.addKeyReleaseEvent("ctrl shift F", givenMockedRunnable);
        givenResponders.eventInvoked(givenKeyPressEvent);
        givenResponders.eventInvoked(givenKeyReleaseEvent);

        // then
        verify(givenMockedRunnable, times(0)).run();
    }
}
