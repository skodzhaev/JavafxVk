package ru.vkfavs;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by skodz on 26.01.2017.
 */
public class MainApp extends Application {
    private static final int APPLICATION_CODE = 0xDEADBEEF;

    ChangeListener<Worker.State> loginListener;

    public boolean checkLogin(String location) {
        return location.matches("(.*?)code=(.*?)");
    }

    public String getCode(String location) {
        Pattern pattern = Pattern.compile("code=(.*)");
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public void setWebViewListener(final Stage primaryStage, final VkOperations vkOperations) {
        WebView webView = (WebView) primaryStage.getScene().lookup("#webView");
        final WebEngine webEngine = webView.getEngine();
        loginListener = new ChangeListener<Worker.State>() {
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if ((newState == Worker.State.SUCCEEDED) && (checkLogin(webEngine.getLocation()))) {
                    String code = getCode(webEngine.getLocation());
                    vkOperations.vkLogin(code);
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("/MainWindow.fxml"));
                        primaryStage.setTitle("Загрузчик картинок");
                        primaryStage.setScene(new Scene(root, 700, 500));
                        webEngine.getLoadWorker().stateProperty().removeListener(loginListener);
                        vkOperations.operate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        webEngine.getLoadWorker().stateProperty().addListener(loginListener);
    }

    public void showLoginWindow(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/startup.fxml"));
        primaryStage.setTitle("Залогиньтесь, пожалуйста");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.setScene(new Scene(root, 700, 500));
        primaryStage.show();
    }

    public void loadStartupPage(Stage primaryStage) throws Exception{
        WebView webView = (WebView) primaryStage.getScene().lookup("#webView");
        webView.getEngine().load("https://oauth.vk.com/authorize?client_id="
                + APPLICATION_CODE
                + "&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=friends&response_type=code&v=5.62");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        showLoginWindow(primaryStage);
        VkOperations vkOperations = new VkOperations(primaryStage);
        setWebViewListener(primaryStage, vkOperations);
        loadStartupPage(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
