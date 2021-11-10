package nz.govt.natlib.ndha.wctdpsdepositor.filemover;

import nz.govt.natlib.ndha.wctdpsdepositor.WctDepositParameter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class SftpFileMoverTest {
    private static SftpFileMover testInstance=new SftpFileMover();

    @BeforeClass
    public static void init() {
        WctDepositParameter depositParameter = new WctDepositParameter();
        depositParameter.setFtpHost("localhost");
        depositParameter.setFtpUserName("leefr");
        depositParameter.setFtpPassword("wangyang@111");
        testInstance.connect(depositParameter);
    }

    @Test
    public void testCreateDirectory() throws IOException {
        {
            String path = "Downloads/test-sftp";
            testInstance.createAndChangeToDirectory(path);

        }
    }

}
