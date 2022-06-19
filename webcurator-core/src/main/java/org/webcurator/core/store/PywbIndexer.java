package org.webcurator.core.store;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;

public class PywbIndexer extends IndexerBase {
    private PywbWarcDeposit pywbWarcDeposit;
    private HarvestResultDTO result;
    private File directory;
    private boolean enabled;

    public PywbIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    public PywbIndexer(PywbIndexer original) {
        super(original);
        this.result = original.result;
        this.pywbWarcDeposit = original.pywbWarcDeposit;
        this.directory = original.directory;
        this.enabled = original.enabled;
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
        try {
            pywbWarcDeposit.depositWarc(this.result);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setPywbWarcDeposit(PywbWarcDeposit pywbWarcDeposit) {
        this.pywbWarcDeposit = pywbWarcDeposit;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
