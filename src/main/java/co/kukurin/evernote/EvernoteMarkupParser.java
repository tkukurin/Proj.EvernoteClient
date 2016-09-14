package co.kukurin.evernote;

// TODO probably not as static
public class EvernoteMarkupParser {

    public static String cleanup(String contents) {
        int startingIndexOfEnNoteTag = contents.indexOf("<en-note>");
        int endingIndexOfContent = contents.indexOf("</en-note>");

        return makeSureThatMarkupTagsExist(contents, startingIndexOfEnNoteTag, endingIndexOfContent);
    }

    private static String makeSureThatMarkupTagsExist(String contents, int startingIndexOfEnNoteTag, int endingIndexOfContent) {
        final int sizeOfEnNoteTag = 9;
        final int startingIndexOfContent = startingIndexOfEnNoteTag + sizeOfEnNoteTag;

        return startingIndexOfEnNoteTag > -1 && endingIndexOfContent > startingIndexOfContent
                ? contents.substring(startingIndexOfContent, endingIndexOfContent)
                : contents;
    }

}
