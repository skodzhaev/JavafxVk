package ru.vkfavs;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.fave.responses.GetPhotosResponse;
import com.vk.api.sdk.objects.photos.Photo;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import ru.vkfavs.fileCreation.FileCreationStrategy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by skodz on 30.01.2017.
 */
public class FavePhotoLoader extends Task<Integer> {
    VkApiClient vk;
    UserActor actor;
    String path;
    int photoCounter;

    FileCreationStrategy strategy;

    public FavePhotoLoader(VkApiClient vk, UserActor actor, String path, FileCreationStrategy strategy) {
        this.vk = vk;
        this.actor = actor;
        this.path = path + "//";
        this.photoCounter = 0;
        this.strategy = strategy;
    }

    void saveFile(URL url, Photo photo) throws IOException {

        FileUtils.copyURLToFile(url, new File(path + photo.getId() + "_" + photo.getOwnerId() + ".jpg"));
        photoCounter++;

    }

    @Override
    public Integer call() throws Exception {
        updateProgress(-1.0, 100);
        int offset = 0;
        int count = 50;
        try {
            while (true) {
                GetPhotosResponse execute1 = vk.fave().getPhotos(actor).offset(offset).count(count).execute();
                List<Photo> photoList = execute1.getItems();
                for (Photo photo : photoList) {
                    if (Thread.interrupted()) {
                        return new Integer(photoCounter);
                    }
                    if (strategy.createFile(path + photo.getId() + "_" + photo.getOwnerId() + ".jpg")) {
                        if (photo.getPhoto2560() != null) {
                            URL url = new URL(photo.getPhoto2560());
                            saveFile(url, photo);
                            continue;
                        }
                        if (photo.getPhoto1280() != null) {
                            URL url = new URL(photo.getPhoto1280());
                            saveFile(url, photo);
                            continue;
                        }
                        if (photo.getPhoto807() != null) {
                            URL url = new URL(photo.getPhoto807());
                            saveFile(url, photo);
                            continue;
                        }
                        if (photo.getPhoto604() != null) {
                            URL url = new URL(photo.getPhoto604());
                            saveFile(url, photo);
                            continue;
                        }
                        if (photo.getPhoto130() != null) {
                            URL url = new URL(photo.getPhoto130());
                            saveFile(url, photo);
                            continue;
                        }
                        if (photo.getPhoto75() != null) {
                            URL url = new URL(photo.getPhoto75());
                            saveFile(url, photo);
                            continue;
                        }
                    }
                    if (photoList.size() != 50) {
                        break;
                    } else {
                        offset += 50;
                    }
                }
            }
            }catch(ClientException e){
                e.printStackTrace();
            }catch(ApiException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                updateProgress(100, 100);
                return new Integer(photoCounter);
            }
        }
    }
