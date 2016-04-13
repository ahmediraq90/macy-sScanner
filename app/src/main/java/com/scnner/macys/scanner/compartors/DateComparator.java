package com.scnner.macys.scanner.compartors;

import java.io.File;

/**
 * Created by ahmed on 4/12/2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public class DateComparator implements Comparable {
    private File file;

    public DateComparator(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(Object another) {
        DateComparator anotherFile = (DateComparator) another;
        if(file.lastModified() == anotherFile.getFile().lastModified())
            return 0;
        else if(file.lastModified() < anotherFile.getFile().lastModified())
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
