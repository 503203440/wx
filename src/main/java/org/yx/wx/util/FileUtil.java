package org.yx.wx.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    public static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 向一个文件中增量写入一段文本
     */
    public static void writeString(File file, String content) {

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
            bufferedWriter.write(content);
            bufferedWriter.flush();
        } catch (FileNotFoundException e) {
            log.error("文件没有找到！", e);
        } catch (IOException e) {
            log.error("IO异常", e);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static String projectPath() {
        File file = new File("");
        String canonicalPath = null;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            log.error("获取项目路径错误");
        }
        return canonicalPath;
    }


}
