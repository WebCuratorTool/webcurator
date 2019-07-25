package org.webcurator.core.store;

public class DigitalAssetStorePaths {
    public static final String ROOT_PATH = "/digital-asset-store";
    public static final String GET_RESOURCE = ROOT_PATH + "/{target-instance-name}";
    public static final String SAVE = ROOT_PATH + "/{target-instance-name}";
    public static final String GET_HEADERS = ROOT_PATH + "/{target-instance-name}";
    public static final String COPY_AND_PRUNE = ROOT_PATH + "/{target-instance-name}";
    public static final String PURGE = ROOT_PATH + "/purge";
    public static final String PURGE_ABORTED_TARGET_INSTANCES = ROOT_PATH + "/purge-aborted-target-instances";
    public static final String GET_SMALL_RESOURCE = ROOT_PATH + "/small/{target-instance-name}";
    public static final String SUBMIT_TO_ARCHIVE = ROOT_PATH + "/archive/{target-instance-oid}";
    public static final String INITIATE_INDEXING = ROOT_PATH + "/initiate-index";
    public static final String INITIATE_REMOVE_INDEXES = ROOT_PATH + "/initiate-index-remove";
    public static final String CHECK_INDEXING = ROOT_PATH + "/check-indexing";
    public static final String GET_CUSTOM_DEPOSIT_FORM_DETAILS = ROOT_PATH + "/custom-deposit-form-details";

    private DigitalAssetStorePaths() {
    }
}
