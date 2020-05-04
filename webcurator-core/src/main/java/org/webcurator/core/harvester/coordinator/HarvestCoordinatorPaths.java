package org.webcurator.core.harvester.coordinator;

public class HarvestCoordinatorPaths {
    public static final String ROOT_PATH = "/harvest-coordinator";
    public static final String HEARTBEAT = ROOT_PATH + "/heartbeat";
    public static final String RECOVERY = ROOT_PATH + "/recovery";
    public static final String HARVEST_COMPLETE = ROOT_PATH + "/harvest-complete";
    public static final String NOTIFICATION_BY_OID = ROOT_PATH + "/notification-by-oid";
    public static final String NOTIFICATION_BY_SUBJECT = ROOT_PATH + "/notification-by-subject";
    public static final String ADD_HARVEST_RESULT = ROOT_PATH + "/result/{harvest-result-oid}";
    public static final String CREATE_HARVEST_RESULT = ROOT_PATH + "/result";
    public static final String FINALISE_INDEX = ROOT_PATH + "/finalise-index/{harvest-result-oid}";
    public static final String NOTIFY_AQA_COMPLETE = ROOT_PATH + "/notification-aqa-complete/{aqa-id}";
    public static final String ADD_HARVEST_RESOURCES = ROOT_PATH + "/add-harvest-resources/{harvest-result-oid}";
    public static final String COMPLETE_ARCHIVING = ROOT_PATH + "/complete-archiving/{target-instance-oid}";
    public static final String FAILED_ARCHIVING = ROOT_PATH + "/failed-archiving/{target-instance-oid}";
}
