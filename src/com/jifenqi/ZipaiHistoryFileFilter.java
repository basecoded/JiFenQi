package com.jifenqi;

import java.io.File;
import java.io.FilenameFilter;

public class ZipaiHistoryFileFilter implements FilenameFilter{

    @Override
    public boolean accept(File dir, String filename) {
        if(filename.startsWith(Const.ZIPAI)) {
            return true;
        }
        return false;
    }

}
