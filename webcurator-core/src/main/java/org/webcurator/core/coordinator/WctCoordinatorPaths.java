package org.webcurator.core.coordinator;

public class WctCoordinatorPaths {
    public static final String ROOT_PATH = "/curator-coordinator";
    public static final String HEARTBEAT = ROOT_PATH + "/heartbeat";
    public static final String RECOVERY = ROOT_PATH + "/recovery";
    public static final String HARVEST_COMPLETE = ROOT_PATH + "/harvest-complete";
    public static final String NOTIFICATION_BY_OID = ROOT_PATH + "/notification-by-oid";
    public static final String NOTIFICATION_BY_SUBJECT = ROOT_PATH + "/notification-by-subject";
    public static final String ADD_HARVEST_RESULT = ROOT_PATH + "/result/{harvest-result-oid}";
    public static final String CREATE_HARVEST_RESULT = ROOT_PATH + "/result";
    public static final String FINALISE_INDEX = ROOT_PATH + "/finalise-index";
    public static final String NOTIFY_AQA_COMPLETE = ROOT_PATH + "/notification-aqa-complete/{aqa-id}";
    public static final String ADD_HARVEST_RESOURCES = ROOT_PATH + "/add-harvest-resources/{harvest-result-oid}";
    public static final String COMPLETE_ARCHIVING = ROOT_PATH + "/complete-archiving/{target-instance-oid}";
    public static final String FAILED_ARCHIVING = ROOT_PATH + "/failed-archiving/{target-instance-oid}";
    public static final String TARGET_INSTANCE_HISTORY_SEED = ROOT_PATH + "/query-ti-history-seed";
    public static final String MODIFICATION_COMPLETE_PRUNE_IMPORT = ROOT_PATH + "/complete-modification";
    public static final String MODIFICATION_DOWNLOAD_IMPORTED_FILE = ROOT_PATH + "/modification-download-imported-file";
    public static final String DIGITAL_ASSET_STORE_HEARTBEAT = ROOT_PATH + "/das/heartbeat";
    public static final String DIGITAL_ASSET_STORE_UPDATE_HR_STATUS = ROOT_PATH + "/das/update-hr-status";
    public static final String DOWNLOAD = ROOT_PATH + "/download";
}
