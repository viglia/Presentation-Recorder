package com.android.viglia.pdfaudiorecorderapp.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by viglia on 11/8/15.
 */
public class FileUtility {


    public static boolean deleteFolder(File folder){
        String listFile[] = folder.list();
        for(int i=0;i<listFile.length;i++){
            File file = new File(folder,listFile[i]);
            if(file.isDirectory())
                deleteFolder(file);
            else
                file.delete();
        }
        return folder.delete();
    }



    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
