package org.yx.wx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;

@Slf4j
public class WxMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //检查系统中是否包含slf4j的StaticLoggerBinder.class
        Enumeration<URL> resources = ClassLoader.getSystemResources("org/slf4j/impl/StaticLoggerBinder.class");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            System.out.println("找到StaticLoggerBinder.class：" + url.toString());
        }

        URL resource = getClass().getResource("/index.fxml");

        Parent root = FXMLLoader.load(resource);

        //默认选中用户目录桌面
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File homeDirectory = fsv.getHomeDirectory();
        ((TextField) root.lookup("#pathInput")).setText(homeDirectory.getPath());

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.setOpacity(0.9);

        primaryStage.setTitle("openId转换器");

        primaryStage.show();


        //禁止缩放
        primaryStage.setResizable(false);
//        double width = primaryStage.getWidth();
//        double height = primaryStage.getHeight();
//        primaryStage.setMinHeight(height);
//        primaryStage.setMaxHeight(height);
//        primaryStage.setMinWidth(width);
//        primaryStage.setMaxWidth(width);

    }
}
