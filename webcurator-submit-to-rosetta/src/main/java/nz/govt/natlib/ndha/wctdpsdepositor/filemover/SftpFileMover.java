package nz.govt.natlib.ndha.wctdpsdepositor.filemover;

import com.jcraft.jsch.*;
import nz.govt.natlib.ndha.wctdpsdepositor.WctDepositParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


public class SftpFileMover implements FileMoverStrategy {
    private static final Logger log = LoggerFactory.getLogger(SftpFileMover.class);
    private ChannelSftp channelSftp;

    @Override
    public void connect(WctDepositParameter depositParameter) {
        try {
            JSch jsch = new JSch();
            //        jsch.setKnownHosts("C:/Users/a_leefr/.ssh/known_hosts");
            Session jschSession = jsch.getSession(depositParameter.getFtpUserName(), depositParameter.getFtpHost());
            jschSession.setPassword(depositParameter.getFtpPassword());

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);
            jschSession.connect();
            this.channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
        } catch (Exception e) {
            log.error("Failed to initial the SFTP Client", e);
        }
    }

    @Override
    public void createAndChangeToDirectory(String depositDirectory) throws IOException {
        try {
            if (!this.channelSftp.isConnected()) {
                this.channelSftp.connect();
            }

            String pwd = this.channelSftp.pwd();
            log.debug("Current directory: {}", pwd);
            this.channelSftp.mkdir(depositDirectory);
            this.channelSftp.cd(depositDirectory);
            pwd = this.channelSftp.pwd();
            log.debug("Current directory: {}", pwd);
        } catch (JSchException | SftpException e) {
            log.error("Failed to create directory: {}", depositDirectory, e);
            throw new IOException(e);
        }
    }

    @Override
    public void changeToDirectory(String depositDirectory) throws IOException {
        try {
            if (!this.channelSftp.isConnected()) {
                this.channelSftp.connect();
            }
            String pwd = this.channelSftp.pwd();
            log.debug("Current directory: {}", pwd);
            this.channelSftp.cd(depositDirectory);
            pwd = this.channelSftp.pwd();
            log.debug("Current directory: {}", pwd);
        } catch (JSchException | SftpException e) {
            log.error("Failed to change directory: {}", depositDirectory, e);
            throw new IOException(e);
        }
    }

    @Override
    public void storeFile(String fileName, InputStream stream) throws IOException {
        try {
            if (!this.channelSftp.isConnected()) {
                this.channelSftp.connect();
            }
            this.channelSftp.put(stream, fileName);
        } catch (JSchException | SftpException e) {
            log.error("Failed to store file: {}", fileName, e);
            throw new IOException(e);
        }
    }

    @Override
    public void close() {
        this.channelSftp.disconnect();
    }
}
