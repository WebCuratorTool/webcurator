package org.webcurator.core.reader;

public class LogReaderPaths {
    public static final String ROOT_PATH = "/log-file";
    public static final String LOG_FILE = ROOT_PATH;
    public static final String LOG_FILE_PROPERTIES = ROOT_PATH + "/{job}/properties";
    public static final String LOG_FILE_LINE_COUNT = ROOT_PATH + "/{job}/line-count";
    public static final String LOG_FILE_TAIL = ROOT_PATH + "/{job}/tail";
    public static final String LOG_FILE_GET = ROOT_PATH + "/{job}";
    public static final String LOG_FILE_FIRST_LINE_BEGINNING = ROOT_PATH + "/{job}/first-line-beginning";
    public static final String LOG_FILE_FIRST_LINE_CONTAINING_MATCH = ROOT_PATH + "/{job}/first-line-containing-match";
    public static final String LOG_FILE_FIRST_LINE_AFTER_TIMESTAMP = ROOT_PATH + "/{job}/first-line-containing-timestamp";
    public static final String LOG_FILE_BY_REGULAR_EXPRESSION = ROOT_PATH + "/{job}/by-regular-expression";
    public static final String LOG_FILE_RETRIEVE_LOG_FILE = ROOT_PATH + "/{job}/retrieve-log-file";
    public static final String LOG_FILE_HOP_PATH = ROOT_PATH + "/{job}/hop-path";

    private LogReaderPaths() {
    }
}
