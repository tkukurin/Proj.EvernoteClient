package co.kukurin.evernote;

// TODO probably not as static
public class EvernoteMarkupParser {

    public static String cleanup(String contents) {
        final int sizeOfEnNoteTag = 9;
        int startingIndexOfContent = contents.indexOf("<en-note>") + sizeOfEnNoteTag;
        int endingIndexOfContent = contents.indexOf("</en-note>");

        return makeSureThatMarkupTagsExist(contents, startingIndexOfContent, endingIndexOfContent);
    }

    private static String makeSureThatMarkupTagsExist(String contents, int startingIndexOfContent, int endingIndexOfContent) {
        return startingIndexOfContent >= 0 && endingIndexOfContent >= 0
                ? contents.substring(startingIndexOfContent, endingIndexOfContent)
                : contents;
    }

}
