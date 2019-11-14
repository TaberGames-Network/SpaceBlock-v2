package me.fuzzybot.spaceblock.util;

import java.io.*;

/**
 * Created by Sven on 15/10/2014. All self coded.
 */
public class MyFileUtils {

    //Source: http://www.mkyong.com/java/how-to-copy-directory-in-java/
    public static void copyFolder(File src, File dest)
            throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
    }
}
