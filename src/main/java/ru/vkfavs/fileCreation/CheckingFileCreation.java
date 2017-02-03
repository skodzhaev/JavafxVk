package ru.vkfavs.fileCreation;

import java.io.File;

/**
 * Created by skodz on 01.02.2017.
 */
public class CheckingFileCreation implements FileCreationStrategy{

    @Override
    public boolean createFile(String path) {
        boolean isFile = new File(path).isFile();
        return !isFile;
    }
}
