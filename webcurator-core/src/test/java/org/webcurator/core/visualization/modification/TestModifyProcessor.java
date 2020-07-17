package org.webcurator.core.visualization;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.processor.ModifyProcessor;
import org.webcurator.core.visualization.modification.metadata.ModifyRowMetadata;
import org.webcurator.core.visualization.modification.processor.ModifyProcessorWarc;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestModifyProcessor extends MockVisualizationCommonConfigureItems {
    private ModifyProcessor warcProcessor = null;

    @Before
    public void initTest() throws IOException, DigitalAssetStoreException {
        super.initTest();

        List<File> dirs = new LinkedList<>();
        File destDir = new File(directoryManager.getBaseDir(), targetInstanceId + File.separator + newHarvestResultNumber);
        dirs.add(destDir);

        ModifyApplyCommand cmd = getApplyCommand();
        warcProcessor = new ModifyProcessorWarc(cmd);
        warcProcessor.init(this.processorManager, this.directoryManager, this.wctClient);
    }

    @Test
    public void testDownloadFile() throws IOException, DigitalAssetStoreException {
        ModifyRowMetadata metadata = new ModifyRowMetadata();
        metadata.setName("expand.png");
        File downloadedFile = warcProcessor.downloadFile(targetInstanceId, newHarvestResultNumber, metadata);
        assertTrue(downloadedFile.exists());
    }

    @Test
    public void testCopyAndPrune() throws Exception {
        File warcFileFrom = getOneWarcFile();
        assert warcFileFrom != null;

        List<String> listToBePrunedUrl = getRandomUrlsFromWarcFile(warcFileFrom);

        Map<String, ModifyRowMetadata> hrsToImport = new HashMap<>();

        warcProcessor.copyArchiveRecords(warcFileFrom, listToBePrunedUrl, hrsToImport, newHarvestResultNumber);

        File destDirectory = new File(directoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, newHarvestResultNumber));
        File warcFileNew = new File(destDirectory, getModifiedWarcFileName(warcFileFrom));
        assert warcFileNew.exists();
        assert warcFileNew.isFile();

        assertFalse("URLs not be pruned completely.", isUrlExistInWarcFile(warcFileNew, listToBePrunedUrl));
    }

    @Test
    public void testImportByFile() throws Exception {
        String targetUrl = String.format("http://www.weikeduo.com/%s/", UUID.randomUUID().toString());
        ModifyApplyCommand cmd = getApplyCommand();
        ModifyRowMetadata m = new ModifyRowMetadata();
        m.setOption("file");
        m.setUrl(targetUrl);
        m.setName("expand.png");
        m.setModifiedMode("TBC");
        m.setContentType("image/png");
        cmd.getDataset().add(m);
        Map<String, ModifyRowMetadata> hrsToImport = new HashMap<>();
        hrsToImport.put(targetUrl, m);
        warcProcessor.importFromFile(targetInstanceId, harvestResultNumber, newHarvestResultNumber, hrsToImport);

        List<String> importedUrl = new ArrayList<>();
        importedUrl.add(targetUrl);

        File warcFileNew = getOneImportedWarcFile("File");
        assert warcFileNew != null;

        assertTrue(isUrlExistInWarcFile(warcFileNew, importedUrl));
    }

    private File getOneWarcFile() {
        File directory = new File(directoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, harvestResultNumber));
        List<File> fileList = PatchUtil.listWarcFiles(directory);
        assert fileList.size() > 0;

        return fileList.get(0);
    }

    private File getOneImportedWarcFile(String option) {
        File directory = new File(directoryManager.getBaseDir(), String.format("%d%s%d", targetInstanceId, File.separator, newHarvestResultNumber));
        List<File> fileList = PatchUtil.listWarcFiles(directory);
        assert fileList.size() > 0;

        if (option.equalsIgnoreCase("FILE")) {
            for (File f : fileList) {
                if (f.getName().contains("mod~import~file")) {
                    return f;
                }
            }
        }

        return null;
    }
}
