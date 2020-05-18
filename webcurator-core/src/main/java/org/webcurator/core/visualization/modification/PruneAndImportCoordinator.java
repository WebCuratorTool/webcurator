package org.webcurator.core.visualization.modification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.store.Indexer;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class PruneAndImportCoordinator {
    protected static final Logger log = LoggerFactory.getLogger(PruneAndImportCoordinator.class);
    protected static final int BYTE_BUFF_SIZE = 1024;
    /**
     * Arc files meta data date format.
     */
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    protected static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    /**
     * The Indexer
     */
    protected Indexer indexer = null;

    protected String fileDir; //Upload files
    protected String baseDir; //Harvest WARC files dir


    public String getArchiveType() {
        return archiveType();
    }

    abstract protected String archiveType();

    abstract protected void copyArchiveRecords(File fileFrom, List<String> urisToDelete, Map<String, PruneAndImportCommandRowMetadata> hrsToImport, int newHarvestResultNumber) throws IOException, URISyntaxException;

    abstract protected void importFromFile(Map<String, PruneAndImportCommandRowMetadata> hrsToImport) throws IOException;

    abstract protected void importFromRecorder(File fileFrom, List<String> urisToDelete, int newHarvestResultNumber) throws IOException, URISyntaxException;
}
