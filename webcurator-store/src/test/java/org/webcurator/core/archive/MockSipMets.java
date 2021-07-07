package org.webcurator.core.archive;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MockSipMets {
    public static String getSipMetsXml() {
        try {
            Resource resource = new ClassPathResource("mets-sip.xml");
            Path tempDataFilePath = Files.createTempFile("mets", ".xml");
            tempDataFilePath.toFile().delete();
            Files.copy(resource.getInputStream(), tempDataFilePath);
            return FileUtils.readFileToString(tempDataFilePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
