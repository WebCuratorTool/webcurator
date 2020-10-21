package org.webcurator.core.visualization.networkmap.metadata;

public class NetworkDbVersionDTO {
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_DB_NOT_EXIT = 1;
    public static final int RESULT_VERSION_NOT_EXIST = 2;
    public static final int RESULT_NEED_REINDEX = 9;

    private int retrieveResult;
    private String globalVersion;
    private String currentVersion;

    public int getRetrieveResult() {
        return retrieveResult;
    }

    public void setRetrieveResult(int retrieveResult) {
        this.retrieveResult = retrieveResult;
    }

    public String getGlobalVersion() {
        return globalVersion;
    }

    public void setGlobalVersion(String globalVersion) {
        this.globalVersion = globalVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }
}
