package org.webcurator.core.visualization;

public class VisualizationConstants {
    public static final String ROOT_PATH = "/curator";
    public static final String PATH_UPLOAD_FILE = ROOT_PATH + "/modification/upload-file-stream";
    public static final String PATH_CHECK_FILES = ROOT_PATH + "/modification/check-files";
    public static final String PATH_APPLY_PRUNE_IMPORT = ROOT_PATH + "/modification/apply";

    public static final String PATH_GET_COMMON = ROOT_PATH + "/networkmap/get/common";
    public static final String PATH_GET_NODE = ROOT_PATH + "/networkmap/get/node";
    public static final String PATH_GET_OUTLINKS = ROOT_PATH + "/networkmap/get/outlinks";
    public static final String PATH_GET_CHILDREN = ROOT_PATH + "/networkmap/get/children";
    public static final String PATH_GET_ALL_DOMAINS = ROOT_PATH + "/networkmap/get/all/domains";
    public static final String PATH_GET_ROOT_URLS = ROOT_PATH + "/networkmap/get/root/urls";
    public static final String PATH_GET_MALFORMED_URLS = ROOT_PATH + "/networkmap/get/malformed/urls";
    public static final String PATH_SEARCH_URLS = ROOT_PATH + "/networkmap/search/urls";
    public static final String PATH_SEARCH_URL_NAMES = ROOT_PATH + "/networkmap/search/url-names";
    public static final String PATH_GET_HOP_PATH = ROOT_PATH + "/networkmap/get/hop/path";
    public static final String PATH_GET_HIERARCHY_URLS = ROOT_PATH + "/networkmap/get/hierarchy/urls";
    public static final String PATH_GET_URL_BY_NAME = ROOT_PATH + "/networkmap/get/query-url-by-name";
    public static final String PATH_GET_URLS_BY_NAMES = ROOT_PATH + "/networkmap/get/query-urls-by-names";

    public static final String PATH_GET_PROGRESS = ROOT_PATH + "/visualization/progress";
    public static final String PATH_GET_PROCESSING_HARVEST_RESULT = ROOT_PATH + "/visualization/processing-harvest-result";
    public static final String PATH_INITIAL_INDEX = ROOT_PATH + "/visualization/index/initial";

    public static final int RESP_CODE_SUCCESS = 0;
    public static final int RESP_CODE_FILE_EXIST = 1;
    public static final int RESP_CODE_INDEX_NOT_EXIST = 2;
    public static final int RESP_CODE_INVALID_REQUEST = -1000;
    public static final int RESP_CODE_ERROR_FILE_IO = -2000;
    public static final int RESP_CODE_ERROR_NETWORK_IO = -3000;
    public static final int RESP_CODE_ERROR_INVALID_TI_STATE = -8000;
    public static final int RESP_CODE_ERROR_SYSTEM_ERROR = -9000;
    public static final int FILE_EXIST_YES = 1;
    public static final int FILE_EXIST_NO = -1;

}
