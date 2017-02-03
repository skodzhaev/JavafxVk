package ru.vkfavs.fileCreation;

/**
 * Created by skodz on 01.02.2017.
 */
public class NonCheckingFileCreation implements FileCreationStrategy {
    @Override
    public boolean createFile(String path) {
        return true;
    }
}
