package org.webcurator.core.store;

public class DigitalAssetStorePaths {
    public static final String ROOT_PATH = "/digital-asset-store";
    public static final String RESOURCE = ROOT_PATH + "/{target-instance-id}";
    public static final String SAVE = ROOT_PATH + "/save/{target-instance-name}";
    public static final String HEADERS = ROOT_PATH + "/headers/{target-instance-id}";
    public static final String COPY_AND_PRUNE = ROOT_PATH + "/copy-and-prune/{target-instance-name}";
    public static final String PURGE = ROOT_PATH + "/purge";
    public static final String PURGE_ABORTED_TARGET_INSTANCES = ROOT_PATH + "/purge-aborted-target-instances";
    public static final String SMALL_RESOURCE = ROOT_PATH + "/small/{target-instance-id}";
    public static final String ARCHIVE = ROOT_PATH + "/archive/{target-instance-oid}";
    public static final String INITIATE_INDEXING = ROOT_PATH + "/initiate-index";
    public static final String INITIATE_REMOVE_INDEXES = ROOT_PATH + "/initiate-index-remove";
    public static final String CHECK_INDEXING = ROOT_PATH + "/check-indexing";
    public static final String CUSTOM_DEPOSIT_FORM_DETAILS = ROOT_PATH + "/custom-deposit-form-details";
    public static final String OPERATE_HARVEST_RESULT_MODIFICATION = ROOT_PATH + "/harvest-result-modification-action";
    public static final String PROGRESS_QUERY = ROOT_PATH + "/query/progress";

    private DigitalAssetStorePaths() {
    }
}
