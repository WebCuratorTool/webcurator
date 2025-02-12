package org.archive.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.netpreserve.jwarc.*;
import org.netpreserve.jwarc.cdx.CdxFormat;
import org.webcurator.core.store.IndexerBase;
import org.webcurator.core.store.RunnableIndex;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDXIndexer extends IndexerBase {
    private static Log log = LogFactory.getLog(CDXIndexer.class);

    private HarvestResultDTO result;
    private File directory;
    private boolean enabled = false;
    private String format = CdxFormat.CDX11.legend();
    private boolean useSurt = false;

    public CDXIndexer() {
        super();
    }

    protected CDXIndexer(CDXIndexer original) {
        super(original);
        enabled = original.enabled;
        format = original.format;
        useSurt = original.useSurt;
    }

    /**
     * Adapted from the jwarc CdxTool
     *
     * @param archiveFile
     * @throws IOException
     */
    private void writeCDXIndex(File archiveFile) throws IOException {
        CdxFormat cdxFormat = new CdxFormat(format);
        String cdxFilename = getCdxFilename(archiveFile);
        try (WarcReader reader = new WarcReader(archiveFile.toPath());
             BufferedWriter cdxWriter = new BufferedWriter(new FileWriter(cdxFilename))) {
            reader.onWarning(log::warn);
            WarcRecord record = reader.next().orElse(null);
            String filename = archiveFile.getName();

            // Write cdx header
            cdxWriter.write(" CDX " + format);
            cdxWriter.newLine();
            while (record != null) {
                try {
                    if ((record instanceof WarcResponse || record instanceof WarcResource) &&
                            ((WarcCaptureRecord) record).payload().isPresent()) {
                        long position = reader.position();
                        WarcCaptureRecord capture = (WarcCaptureRecord) record;
                        URI id = record.version().getProtocol().equals("ARC") ? null : record.id();

                        // advance to the next record so we can calculate the length
                        record = reader.next().orElse(null);
                        long length = reader.position() - position;

                        if (useSurt) {
                            cdxWriter.write(cdxFormat.format(capture, filename, position, length));
                        } else {
                            // we don't want the N-field to be SURT-formatted, so we supply our own value
                            URI uri = capture.targetURI();
                            String massagedUrl = null;
                            if (uri != null) {
                                massagedUrl = uri.toASCIIString().replaceAll("[^//]+://(www\\.)?", "");
                            }
                            cdxWriter.write(cdxFormat.format(capture, filename, position, length, massagedUrl));
                        }
                        cdxWriter.newLine();
                    } else {
                        record = reader.next().orElse(null);
                    }
                } catch (ParsingException e) {
                    log.error("ParsingException at record " + reader.position() + ": " + e.getMessage());
                    record = reader.next().orElse(null);
                }
            }
        }
    }

    private String getCdxFilename(File archiveFile) {
        String cdxFilename = archiveFile.getAbsolutePath();
        String suffixRegex = ".w?arc(.gz)?$";
        Pattern p = Pattern.compile(suffixRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(cdxFilename);
        cdxFilename = m.replaceAll("");
        cdxFilename += ".cdx";
        return cdxFilename;
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        File[] fileList = directory.listFiles(new ARCFilter());
        if (fileList == null) {
            log.error("Could not find any archive files in directory: " + directory.getAbsolutePath());
        } else {
            for (File f : fileList) {
                try {
                    log.info("Indexing " + f.getName());
                    writeCDXIndex(f);
                    log.info("Completed indexing of " + f.getName());
                } catch (IOException ex) {
                    log.error("Could not index file " + f.getName() + ". Ignoring and continuing with other files. " + ex.getClass().getCanonicalName() + ": " + ex.getMessage());
                }
            }
        }
        log.info("Completed indexing for job " + getResult().getTargetInstanceOid());

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
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    @Override
    protected HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public RunnableIndex getCopy() {
        return new CDXIndexer(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format.trim();
    }

    public boolean useSurt() {
        return useSurt;
    }

    public void setUseSurt(boolean useSurt) {
        this.useSurt = useSurt;
    }


}
