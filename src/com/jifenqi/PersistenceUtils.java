package com.jifenqi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.os.Environment;

public class PersistenceUtils {

    public static boolean saveZipaiLastGame(GameInfo gi) {
        if(!checkSDCardAvailable()) {
            return false;
        }
        
        String zipaiDir = Utils.getZipaiDir();
        if(zipaiDir == null) {
            return false;
        }
        
        String filePath = zipaiDir + "/" + Const.LASTGAME_NAME;
        return doSave(gi, filePath);
    }
    
    
    public static boolean checkSDCardAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    
    public static boolean doSave(GameInfo gi, String filePath) {
        Serializer serializer = new Persister();
        try {
            File file = new File(filePath);
            serializer.write(gi, file);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static GameInfo doLoad(String filePath) {
        GameInfo gi = null;
        Serializer serializer = new Persister();
        try {
            File file = new File(filePath);
            gi = serializer.read(GameInfo.class, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return gi;
    }
    
    public static boolean saveGame(GameInfo gi, String filePath) {
        String zipaiDir = Utils.getZipaiDir();
        if(zipaiDir == null) {
            return false;
        }
        
        boolean ret = false;
        String lastGamePath = zipaiDir + "/" + Const.LASTGAME_NAME;
        String newSavePath = zipaiDir + "/" + filePath;
        File lastGame = new File(lastGamePath);
        
        saveZipaiLastGame(gi);
        
        File saveFile = new File(newSavePath);
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            inStream = new BufferedInputStream(new FileInputStream(lastGame));
            outStream = new BufferedOutputStream(new FileOutputStream(saveFile));
            byte[] buffer = new byte[1024];
            int byteRead;
            while((byteRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, byteRead);
            }
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inStream != null)
                    inStream.close();
                if(outStream != null)
                    outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return ret;
    }
    
    public static boolean deleteHistory(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }
}
