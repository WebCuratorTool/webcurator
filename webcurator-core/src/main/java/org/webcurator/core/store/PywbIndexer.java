package org.webcurator.core.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.util.ProcessBuilderUtils;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PywbIndexer extends IndexerBase {
    private static final Log log = LogFactory.getLog(PywbIndexer.class);
    private HarvestResultDTO result;
    private File directory;
    private boolean enabled;

    private String pywbManagerColl;

    private File pywbManagerStoreDir;

    public PywbIndexer() {
        super();
    }

    public PywbIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    public PywbIndexer(PywbIndexer original) {
        super(original);
        this.result = original.result;
        this.directory = original.directory;
        this.enabled = original.enabled;
        this.pywbManagerColl = original.pywbManagerColl;
        this.pywbManagerStoreDir = original.pywbManagerStoreDir;
    }

    @Override
    protected HarvestResultDTO getResult() {
        return this.result;
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public RunnableIndex getCopy() {
        return new PywbIndexer(this);
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    @Override
    public Long begin() {
        return this.getResult().getOid();
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        if (!this.isEnabled()) {
            return;
        }

        //Added the WARC files to pywb, and pywb will index automatically
        File[] warcFiles = this.directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null || warcFiles.length == 0) {
            String err = "No warc files found in folder: " + this.directory.getAbsolutePath();
            log.error(err);
            return;
        }

        String wb_manager = ProcessBuilderUtils.getFullPathOfCommand("wb-manager");
        if (wb_manager == null) {
            log.error("The command tool [wb-manager] is not installed or not in the env PATH.");
            return;
        }

        //Added all warc files to the PYWB
        for (File warc : warcFiles) {
            String[] commands = {wb_manager, "add", pywbManagerColl, warc.getAbsolutePath()};
            List<String> commandList = Arrays.asList(commands);
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.directory(pywbManagerStoreDir);
            try {
                Process process = processBuilder.inheritIO().start();
                int processStatus = process.waitFor();

                if (processStatus != 0) {
                    throw new Exception("Process ended with a failed status: " + processStatus);
                }
            } catch (Exception e) {
                log.error("Unable to process the command in a new thread.", e);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setPywbManagerColl(String pywbManagerColl) {
        this.pywbManagerColl = pywbManagerColl;
    }

    public void setPywbManagerStoreDir(File pywbManagerStoreDir) {
        this.pywbManagerStoreDir = pywbManagerStoreDir;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
