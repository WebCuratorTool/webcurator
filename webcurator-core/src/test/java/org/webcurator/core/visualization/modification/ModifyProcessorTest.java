package org.webcurator.core.visualization.modification;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.BaseVisualizationTest;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.processor.ModifyProcessor;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.core.visualization.modification.processor.ModifyProcessorWarc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;

public class ModifyProcessorTest extends BaseVisualizationTest {
    private ModifyProcessor warcProcessor = null;

    @Before
    public void initTest() throws Exception {
        super.initTest();

        List<File> dirs = new LinkedList<>();
        File destDir = new File(directoryManager.getBaseDir(), targetInstanceId + File.separator + newHarvestResultNumber);
        dirs.add(destDir);

        ModifyApplyCommand cmd = getApplyCommand();
        warcProcessor = new ModifyProcessorWarc(cmd);
        warcProcessor.init(this.directoryManager, this.wctClient, this.networkMapClient);
    }

    @Test
    public void testDownloadFile() throws IOException, DigitalAssetStoreException {
        ModifyRowFullData metadata = new ModifyRowFullData();
        metadata.setCachedFileName("expand.png");

        File downloadedFile = new File(directoryManager.getUploadDir(targetInstanceId), metadata.getCachedFileName());
        Files.write(downloadedFile.toPath(), "test content".getBytes(), StandardOpenOption.CREATE);

        when(wctClient.getDownloadFileURL(anyLong(), anyInt(), anyString(), any(File.class))).thenReturn(downloadedFile);

        File resultFile = warcProcessor.downloadFile(targetInstanceId, newHarvestResultNumber, metadata);
        assertTrue(resultFile.exists());
    }

    @Test
    public void testCopyAndPrune() throws Exception {
        File warcFileFrom = getOneWarcFile();
        assert warcFileFrom != null;

        List<String> listToBePrunedUrl = getRandomUrlsFromWarcFile(warcFileFrom);

        Map<String, ModifyRowFullData> hrsToImport = new HashMap<>();

        ReflectionTestUtils.setField(warcProcessor, "urisToDelete", listToBePrunedUrl);
        ReflectionTestUtils.setField(warcProcessor, "hrsToImport", hrsToImport);

        warcProcessor.copyArchiveRecords(warcFileFrom);

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
        ModifyRowFullData m = new ModifyRowFullData();
        m.setOption("file");
        m.setUrl(targetUrl);
        m.setUploadFileName("expand.png");
        m.setCachedFileName("expand.png");
        m.setModifiedMode("TBC");
        m.setContentType("image/png");
        cmd.getDataset().add(m);
        Map<String, ModifyRowFullData> hrsToImport = new HashMap<>();
        hrsToImport.put(targetUrl, m);

        File downloadedFile = new File(directoryManager.getUploadDir(cmd.getTargetInstanceId()), UUID.randomUUID().toString());
        Files.write(downloadedFile.toPath(), "test content".getBytes(), StandardOpenOption.CREATE);

        when(wctClient.getDownloadFileURL(anyLong(), anyInt(), anyString(), any(File.class))).thenReturn(downloadedFile);

        ReflectionTestUtils.setField(warcProcessor, "hrsToImport", hrsToImport);
        warcProcessor.importFromFile();

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
