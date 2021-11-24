package nz.govt.natlib.ndha.wctdpsdepositor.filemover;

import nz.govt.natlib.ndha.wctdpsdepositor.WctDepositParameter;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
public class SftpFileMoverTest {
    private static SftpFileMover testInstance = new SftpFileMover();
    private static String USER_DIR = System.getenv("HOME");

    @BeforeClass
    public static void init() {
        WctDepositParameter depositParameter = new WctDepositParameter();
        depositParameter.setFtpHost("127.0.0.1");
        depositParameter.setFtpUserName("test");
        depositParameter.setFtpPassword("test");
        testInstance.connect(depositParameter);
    }

    @Test
    public void testCreateAndChangeDirectory() throws IOException {
        {
            String path = "Downloads/test-sftp";
            Path absolutePath = Paths.get(USER_DIR, path);
            //Files.deleteIfExists(absolutePath);
            FileUtils.deleteDirectory(absolutePath.toFile());
            testInstance.createAndChangeToDirectory(path);
            assert true;
        }

        {
            String path = "folder-b";
            testInstance.createAndChangeToDirectory(path);
            assert true;

            Path absolutePath = Paths.get(USER_DIR, "Downloads/test-sftp");
            FileUtils.deleteDirectory(absolutePath.toFile());
        }
    }

    @Test
    public void testChangeDirectoryAndStoreFile() throws IOException {
        Path absolutePath = Paths.get(USER_DIR, "Downloads");
        testInstance.changeToDirectory(absolutePath.toString());
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        testInstance.storeFile("sftp-test.txt", inputStream);

        absolutePath = Paths.get(USER_DIR, "Downloads", "sftp-test.txt");
        assert absolutePath.toFile().exists();

        FileUtils.forceDelete(absolutePath.toFile());
    }

}
