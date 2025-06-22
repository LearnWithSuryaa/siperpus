package com.perpustakaan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/perpustakaan/view/LoginView.fxml"));
        // Atur ukuran awal jendela aplikasi
        Scene scene = new Scene(root, 960, 540); 
        
        // Tambahkan stylesheet untuk tema dan styling
        String baseCss = getClass().getResource("/com/perpustakaan/view/base.css").toExternalForm();
        String themeCss = getClass().getResource("/com/perpustakaan/view/theme-login.css").toExternalForm();
        scene.getStylesheets().addAll(baseCss, themeCss);

        // Set judul dan tampilkan jendela aplikasi
        primaryStage.setTitle("Sistem Informasi Perpustakaan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.centerOnScreen();
    }
    public static void main(String[] args) {
        launch(args);
    }
}