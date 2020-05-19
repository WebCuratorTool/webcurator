package org.webcurator.core.visualization.networkmap.service;

public interface NetworkMapServicePath {
    String PATH_GET_COMMON = "/curator/networkmap/get/common";
    String PATH_GET_NODE = "/curator/networkmap/get/node";
    String PATH_GET_OUTLINKS = "/curator/networkmap/get/outlinks";
    String PATH_GET_CHILDREN = "/curator/networkmap/get/children";
    String PATH_GET_ALL_DOMAINS = "/curator/networkmap/get/all/domains";
    String PATH_GET_ROOT_URLS = "/curator/networkmap/get/root/urls";
    String PATH_GET_MALFORMED_URLS = "/curator/networkmap/get/malformed/urls";
    String PATH_SEARCH_URLS = "/curator/networkmap/search/urls";
    String PATH_GET_HOP_PATH = "/curator/networkmap/get/hop/path";
    String PATH_GET_HIERARCHY_URLS="/curator/networkmap/get/hierarchy/urls";
}
