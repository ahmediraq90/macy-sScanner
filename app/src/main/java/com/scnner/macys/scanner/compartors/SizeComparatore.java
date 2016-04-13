package com.scnner.macys.scanner.compartors;

import java.io.File;

/**
 * Created by ahmed on 4/12/2016.
 */
public class SizeComparatore implements Comparable {

    private File file;

    public SizeComparatore(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(Object another) {
       SizeComparatore sizeComparatore =  (SizeComparatore) another;
        if(file.length() == sizeComparatore.file.length())
        return 0;
        else if(file.length()< sizeComparatore.file.length())
            return 1;
        else
            return -1;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
