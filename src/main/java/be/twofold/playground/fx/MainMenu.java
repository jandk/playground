package be.twofold.playground.fx;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class MainMenu extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Main Menu");

        MenuItem menuFileOpen = new MenuItem("Open");
        menuFileOpen.setAccelerator(KeyCombination.valueOf("Shortcut+O"));
        MenuItem menuFileQuit = new MenuItem("Quit");
        menuFileQuit.setAccelerator(KeyCombination.valueOf("Shortcut+Q"));

        Menu menuFile = new Menu("File");
        menuFile.getItems().add(menuFileOpen);
        menuFile.getItems().add(new SeparatorMenuItem());
        menuFile.getItems().add(menuFileQuit);

        MenuItem menuHelpAbout = new MenuItem("About");
        Menu menuHelp = new Menu("Help");
        menuHelp.getItems().add(menuHelpAbout);

        MenuBar menuBar = new MenuBar(menuFile, menuHelp);

        VBox vBox = new VBox(menuBar);

        Scene scene = new Scene(vBox, 400, 300);

        primaryStage.addEventHandler(Event.ANY, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                System.out.println(event);
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
