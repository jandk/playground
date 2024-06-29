package be.twofold.playground.fx;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class DragAndDropTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Drag a file to me.");
        Label dropped = new Label("");
        VBox dragTarget = new VBox();
        dragTarget.getChildren().addAll(label, dropped);
        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget
                && event.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dragTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                dropped.setText(db.getFiles().toString());
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });


        StackPane root = new StackPane();
        root.getChildren().add(dragTarget);

        Scene scene = new Scene(root, 500, 250);

        primaryStage.setTitle("Drag Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
