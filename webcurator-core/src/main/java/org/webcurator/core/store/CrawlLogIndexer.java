package org.webcurator.core.store;

import it.unipi.di.util.ExternalSort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class CrawlLogIndexer extends IndexerBase {
    //Static variables
    private static Log log = LogFactory.getLog(CrawlLogIndexer.class);

    //Passed in variables
    private HarvestResultDTO result;
    private File directory;

    //Spring initialised variables (to be copied in copy constructor)
    private String crawlLogFileName;
    private String logsSubFolder;

    private boolean enabled = false;

    public CrawlLogIndexer(){
        super();
    }

    public CrawlLogIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    protected CrawlLogIndexer(CrawlLogIndexer original) {
        super(original);
        crawlLogFileName = original.crawlLogFileName;
        logsSubFolder = original.logsSubFolder;
        enabled = original.enabled;
    }

    @Override
    public RunnableIndex getCopy() {
        return new CrawlLogIndexer(this);
    }

    @Override
    protected HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public Long begin() {
        return getResult().getOid();
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        return;
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    public void setCrawlLogFileName(String crawlLogFileName) {
        this.crawlLogFileName = crawlLogFileName;
    }

    public String getCrawlLogFileName() {
        return crawlLogFileName;
    }

    public void setLogsSubFolder(String logsSubFolder) {
        this.logsSubFolder = logsSubFolder;
    }

    public String getLogsSubFolder() {
        return logsSubFolder;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
