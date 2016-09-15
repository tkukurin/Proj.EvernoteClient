package co.kukurin.evernote;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.List;

public class EvernoteEntry {

    private Note delegate;

    // TODO currently unused.
    private @Getter @Setter boolean wasModified;

    public EvernoteEntry(Note delegate) {
        this.delegate = delegate;
    }

    public Note toNote() {
        return this.delegate;
    }

    public String getGuid() {
        return delegate.getGuid();
    }

    public String getTitle() {
        return delegate.getTitle();
    }

    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    public void setContent(String content) {
        delegate.setContent(content);
    }

    // TODO getCleanContent ?
    public String getContent() {
        return EvernoteMarkupParser.cleanup(delegate.getContent());
    }

    public byte[] getContentHash() {
        return delegate.getContentHash();
    }

    public long getCreated() {
        return delegate.getCreated();
    }

    public long getUpdated() {
        return delegate.getUpdated();
    }

    public long getDeleted() {
        return delegate.getDeleted();
    }

    public List<String> getTagGuids() {
        return delegate.getTagGuids();
    }

    public String getNotebookGuid() {
        return delegate.getNotebookGuid();
    }

    public int getUpdateSequenceNum() {
        return delegate.getUpdateSequenceNum();
    }

    public boolean isActive() {
        return delegate.isActive();
    }

    public void setActive(boolean active) {
        delegate.setActive(active);
    }

    public void setTagGuids(List<String> tagGuids) {
        delegate.setTagGuids(tagGuids);
    }

    public void addToTagGuids(String elem) {
        delegate.addToTagGuids(elem);
    }

    public void addToResources(Resource elem) {
        delegate.addToResources(elem);
    }

    public List<Resource> getResources() {
        return delegate.getResources();
    }

    public void setResources(List<Resource> resources) {
        delegate.setResources(resources);
    }

    public NoteAttributes getAttributes() {
        return delegate.getAttributes();
    }

    public void setAttributes(NoteAttributes attributes) {
        delegate.setAttributes(attributes);
    }

    public List<String> getTagNames() {
        return delegate.getTagNames();
    }

    public void addToTagNames(String elem) {
        delegate.addToTagNames(elem);
    }

    public void setTagNames(List<String> tagNames) {
        delegate.setTagNames(tagNames);
    }

    @Override
    public String toString() {
        return this.delegate.getTitle();
    }

    @Override
    public boolean equals(Object o) { // TODO guid should probably be enough to compare the entries?
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvernoteEntry that = (EvernoteEntry) o;
        return delegate.getGuid().equals(that.delegate.getGuid());

    }

    @Override
    public int hashCode() {
        return delegate.getGuid().hashCode();
    }
}
