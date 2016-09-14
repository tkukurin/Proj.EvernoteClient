package co.kukurin.evernote;

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class EvernoteMarkupParserTest {

    @Test
    public void shouldGiveCorrectContent() throws Exception {
        // given
        String givenActualContent = "this is the note <b>content</b>.";
        String givenContentWithEnNote = "<en-note>" + givenActualContent + "</en-note>";

        // when
        String whenContent = EvernoteMarkupParser.cleanup(givenContentWithEnNote);

        // then
        then(whenContent).isEqualTo(givenActualContent);
    }

    @Test
    public void shouldNotThrowExceptionOnInvalidMarkup() throws Exception {
        // given
        String givenContentWithoutEnNote = "no en note here.";

        // when
        String whenContent = EvernoteMarkupParser.cleanup(givenContentWithoutEnNote);

        // then
        then(whenContent).isEqualTo(givenContentWithoutEnNote);
    }
}
