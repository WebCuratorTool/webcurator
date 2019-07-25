package org.webcurator.core.harvester.agent;

public class HarvestAgentPaths {
    public static final String ROOT_PATH = "/harvest-agent";
    public static final String INITIATE_HARVEST = ROOT_PATH + "/{job}/initiate-harvest";
    public static final String RECOVER_HARVESTS = ROOT_PATH + "/recover-harvests";
    public static final String RESTRICT_BANDWIDTH = ROOT_PATH + "/{job}/restrict-bandwidth";
    public static final String PAUSE = ROOT_PATH + "/{job}/pause";
    public static final String RESUME = ROOT_PATH + "/{job}/resume";
    public static final String ABORT = ROOT_PATH + "/{job}/abort";
    public static final String STOP = ROOT_PATH + "/{job}/stop";
    public static final String LOAD_SETTINGS = ROOT_PATH + "/{job}/load-settings";
    public static final String PAUSE_ALL = ROOT_PATH + "/pause-all";
    public static final String RESUME_ALL = ROOT_PATH + "/resume-all";
    public static final String STATUS = ROOT_PATH + "/status";
    public static final String UPDATE_PROFILE_OVERRIDES = ROOT_PATH + "/{job}/update-profile-overrides";
    public static final String PURGE_ABORTED_TARGET_INSTANCES = ROOT_PATH + "/purge-aborted-target-instances";
    public static final String IS_VALID_PROFILE = ROOT_PATH + "/{profile}/is-valid";
    public static final String EXECUT_SHELL_SCRIPT = ROOT_PATH + "/{job}/execute-shell-script";

    private HarvestAgentPaths() {
    }
}
