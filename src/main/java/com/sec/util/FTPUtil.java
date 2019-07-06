package com.sec.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FTPUtil {
    private static Logger log = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIP = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPwd = PropertiesUtil.getProperty("ftp.pwd");
    private static int ftpPort = Integer.parseInt(PropertiesUtil.getProperty("ftp.port", "21"));
    private static String ftpRemoteDir = PropertiesUtil.getProperty("ftp.remote.dir");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * 向ftp服务器上传文件
     *
     * @param fileList
     * @return
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIP, ftpPort, ftpUser, ftpPwd);
        log.info("开始连接ftp服务器...");
        boolean result = ftpUtil.uploadFile(ftpRemoteDir, fileList);
        log.info("上传结果:{}", result);
        return result;
    }


    /**
     * 删除ftp服务器指定文件
     *
     * @param targetName
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(String targetName) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIP, ftpPort, ftpUser, ftpPwd);
        log.info("开始连接ftp服务器");
        boolean result = ftpUtil.deleteFile(ftpRemoteDir, targetName);
        log.info("删除结果:{}", result);
        return result;
    }

    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            log.error("连接FTP服务器异常", e);
        }
        return isSuccess;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = false;
        FileInputStream fis = null;
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                log.info("开始上传..");
                boolean flag = ftpClient.changeWorkingDirectory(remotePath);
                if (!flag) {
                    // 创建上传的路径  该方法只能创建一级目录
                    ftpClient.makeDirectory(remotePath);
                    ftpClient.changeWorkingDirectory(remotePath);
                }
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File fileItem : fileList) {
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);
                }
                uploaded = true;
                log.info("上传成功...");
            } catch (IOException e) {
                log.error("上传文件异常!!!", e);
                e.printStackTrace();
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }

        return uploaded;
    }

    private boolean deleteFile(String remotePath, String targetName) throws IOException {
        boolean deleted = false;
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                log.info("开始删除..");
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.dele(targetName);
                deleted = true;
                log.info("删除成功..");
            } catch (IOException e) {
                log.error("删除文件异常", e);
                e.printStackTrace();
            } finally {
                ftpClient.disconnect();
            }
        }

        return deleted;
    }
}
