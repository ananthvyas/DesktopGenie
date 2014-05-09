package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public enum Notifier {

    INSTANCE;
    private Duration popupTime;
    private Stage stage;
    private StackPane pane;
    private Scene scene;
    private static double width = 300;
    private static double height = 80;
    private SimpleStringProperty notificationMessage = new SimpleStringProperty();

    private Notifier() {
        popupTime = Duration.seconds(2);

        pane = new StackPane();
        scene = new Scene(pane);
        scene.setFill(null);

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
    }

    public void notify(final String notification) {
        Region body = new Region();
        body.setPrefSize(width, height);

        Label message = new Label(notification);
        notificationMessage.setValue(notification);
        //message.setStyle("-fx-font: Monospace;");
        message.setFont(new Font("Cambria", 30));
        VBox popupLayout = new VBox();
        popupLayout.setSpacing(10);
        popupLayout.setPadding(new Insets(10, 10, 10, 10));
        popupLayout.getChildren().add(message);

        StackPane popupPane = new StackPane();
        popupPane.getChildren().addAll(body, popupLayout);
        popupPane.setStyle("-fx-background-color: rgba(100, 100, 100, 1); -fx-background-radius: 10;");

        final Popup popup = new Popup();
        popup.setX(Screen.getPrimary().getBounds().getWidth() - width);
        popup.setY(25);
        popup.getContent().addAll(popupPane);
        popup.setAutoHide(true);

        KeyValue fadeOutBegin = new KeyValue(popup.opacityProperty(), 1);
        KeyValue fadeOutEnd = new KeyValue(popup.opacityProperty(), 0);

        KeyFrame keyFrameBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame keyFrameEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

        Timeline timeline = new Timeline(keyFrameBegin, keyFrameEnd);
        timeline.setDelay(popupTime);
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                popup.hide();
            }
        });

        if (stage.isShowing()) {
            stage.toFront();
        } else {
            stage.show();
        }

        popup.show(stage);
        timeline.play();
    }

    public void stop() {
        stage.close();
    }
    
    public String getMessage() {
    	return notificationMessage.getValue();
    }
}
