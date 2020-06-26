package org.webcurator.core.visualization;

public class VisualizationConstants {
    public static final String PATH_ROOT = "/curator";
    public static final String PATH_UPLOAD_FILE = PATH_ROOT + "/modification/upload-file-stream";
    public static final String PATH_DOWNLOAD_FILE = PATH_ROOT + "/modification/download-file-stream";
    public static final String PATH_CHECK_FILES = PATH_ROOT + "/modification/check-files";
    public static final String PATH_APPLY_PRUNE_IMPORT = PATH_ROOT + "/modification/apply";

    public static final String PATH_GET_COMMON = PATH_ROOT + "/networkmap/get/common";
    public static final String PATH_GET_NODE = PATH_ROOT + "/networkmap/get/node";
    public static final String PATH_GET_OUTLINKS = PATH_ROOT + "/networkmap/get/outlinks";
    public static final String PATH_GET_CHILDREN = PATH_ROOT + "/networkmap/get/children";
    public static final String PATH_GET_ALL_DOMAINS = PATH_ROOT + "/networkmap/get/all/domains";
    public static final String PATH_GET_ROOT_URLS = PATH_ROOT + "/networkmap/get/root/urls";
    public static final String PATH_GET_MALFORMED_URLS = PATH_ROOT + "/networkmap/get/malformed/urls";
    public static final String PATH_SEARCH_URLS = PATH_ROOT + "/networkmap/search/urls";
    public static final String PATH_SEARCH_URL_NAMES = PATH_ROOT + "/networkmap/search/url-names";
    public static final String PATH_GET_HOP_PATH = PATH_ROOT + "/networkmap/get/hop/path";
    public static final String PATH_GET_HIERARCHY_URLS = PATH_ROOT + "/networkmap/get/hierarchy/urls";
    public static final String PATH_GET_URL_BY_NAME = PATH_ROOT + "/networkmap/get/query-url-by-name";

    public static final String PATH_GET_PROGRESS = PATH_ROOT + "/visualization/progress";
    public static final String PATH_INDEX = PATH_ROOT + "/visualization/index";

    public static final int RESP_CODE_SUCCESS = 0;
    public static final int RESP_CODE_FILE_EXIST = 1;
    public static final int RESP_CODE_INVALID_REQUEST = -1000;
    public static final int RESP_CODE_ERROR_FILE_IO = -2000;
    public static final int RESP_CODE_ERROR_NETWORK_IO = -3000;
    public static final int RESP_CODE_ERROR_INVALID_TI_STATE = -8000;
    public static final int RESP_CODE_ERROR_SYSTEM_ERROR = -9000;
    public static final int FILE_EXIST_YES = 1;
    public static final int FILE_EXIST_NO = -1;

}
