package com.uniloop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        try {
            Connection connection = DatabaseConnection.getConnection();

            System.out.println("Successfully connected to UniLoop database!");

            connection.close();

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }

        Label message = new Label("Welcome to UniLoop!");

        StackPane root = new StackPane();
        root.getChildren().add(message);

        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("UniLoop");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}