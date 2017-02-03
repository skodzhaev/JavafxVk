package ru.vkfavs;


import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.account.UserSettings;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ru.vkfavs.fileCreation.CheckingFileCreation;
import ru.vkfavs.fileCreation.NonCheckingFileCreation;

import java.io.File;
import java.util.List;

/**
 * Created by skodz on 28.01.2017.
 */
public class VkOperations {
    private static final int APPLICATION_CODE = 0xDEADBEEF; //Paste here your public app code
    private static final String APPLICATION_SECRET_CODE = "Paste here hour secret code";

    Thread loadingThread;

    VkApiClient vk;
    Stage currentStage;
    TransportClient transportClient;
    UserActor actor;

    @FXML
    TextArea textArea;

    @FXML
    Label nameLabel;

    public VkOperations(Stage currentStage) {
        this.currentStage = currentStage;
    }

    void vkLogin(String code) {
        transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        UserAuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(APPLICATION_CODE, APPLICATION_SECRET_CODE,
                            "https://oauth.vk.com/blank.html", code)
                    .execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());
        try {
            UserSettings execute3 = vk.account().getProfileInfo(actor).execute();
            com.vk.api.sdk.objects.groups.responses.GetResponse execute2 = vk.groups().get(actor).execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public void getProfilePic() {
        nameLabel = (Label) currentStage.getScene().lookup("#nameLabel");
        textArea = (TextArea) currentStage.getScene().lookup("#textArea");
        try {
            UserSettings userSettings = vk.account().getProfileInfo(actor).execute();
            List<UserXtrCounters> execute = vk.users().get(actor).fields(UserField.STATUS, UserField.PHOTO_MAX).execute();
            ImageView profilePic = (ImageView) currentStage.getScene().lookup("#profilePic");
            nameLabel.setText(execute.get(0).getFirstName() + " " + execute.get(0).getLastName());
            profilePic.setImage(new Image(execute.get(0).getPhotoMax()));
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public void operate() {
        getProfilePic();
        RadioButton radioYes = (RadioButton) currentStage.getScene().lookup("#radioYes");
        TextField textField = (TextField) currentStage.getScene().lookup("#pathTextField");
        Button buttonGo = (Button) currentStage.getScene().lookup("#goButton");
        Button buttonChooseDirectory = (Button) currentStage.getScene().lookup("#chooseButton");
        Button buttonStop = (Button) currentStage.getScene().lookup("#stopButton");
        ProgressIndicator progressIndicator = (ProgressIndicator) currentStage.getScene().lookup("#downloadIndicator");

        buttonChooseDirectory.setOnAction(event1 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(currentStage);
            if(selectedDirectory == null){
                textField.setText("C://Test");
            }else{
                textField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        buttonStop.setOnAction(event2 -> {
            if(loadingThread.isAlive()){
                loadingThread.interrupt();
            }
            buttonGo.setDisable(false);
            progressIndicator.progressProperty().unbind();
            progressIndicator.setProgress(0.0);
            progressIndicator.setVisible(false);
        });

        buttonGo.setOnAction(event3 -> {
            Task<Integer> task;
            if (radioYes.isSelected()) {
                task = new FavePhotoLoader(vk,actor,textField.getText(), new CheckingFileCreation());
            } else {
                task = new FavePhotoLoader(vk,actor,textField.getText(), new NonCheckingFileCreation());
            }
            buttonGo.setDisable(true);
            buttonChooseDirectory.setDisable(true);
            progressIndicator.setVisible(true);

            task.setOnSucceeded(e -> {
                buttonGo.setDisable(false);
                buttonChooseDirectory.setDisable(false);
            });

            progressIndicator.progressProperty().bind(task.progressProperty());
            loadingThread = new Thread(task);
            loadingThread.start();
            buttonStop.setDisable(false);
        });


    }
}
