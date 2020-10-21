package org.webcurator.core.visualization.networkmap.metadata;

public class NetworkMapTreeViewPath implements NetworkMapUnlStructure {
    private final static int UNL_FIELDS_COUNT_MAX = 3;

    private long id;
    private long parentPathId;
    private String title;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentPathId() {
        return parentPathId;
    }

    public void setParentPathId(long parentPathId) {
        this.parentPathId = parentPathId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toUnlString() {
        return String.format("%d\n%d\n%s", id, parentPathId, title);
    }

    @Override
    public void toObjectFromUnl(String unl) throws Exception {
        if (unl == null) {
            throw new Exception("Unl could not be null.");
        }
        String[] items = unl.split(UNL_FIELDS_SEPARATOR);
        if (items.length != UNL_FIELDS_COUNT_MAX) {
            throw new Exception("Item number=" + items.length + " does not equal to UNL_FIELDS_COUNT_MAX=" + UNL_FIELDS_COUNT_MAX);
        }

        this.id = Long.parseLong(items[0]);
        this.parentPathId = Long.parseLong(items[1]);
        this.title = items[2];
    }
}
