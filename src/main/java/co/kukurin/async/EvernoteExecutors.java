package co.kukurin.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EvernoteExecutors {

    // TODO this is a fix for the "out of sequence" exception which Evernote client generates
    // investigate implementing possible multithread update.
    public static final Executor defaultExecutor = Executors.newSingleThreadExecutor();

    private EvernoteExecutors() {}
}
