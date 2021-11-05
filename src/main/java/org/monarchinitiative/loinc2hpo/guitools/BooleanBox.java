package org.monarchinitiative.loinc2hpo.guitools;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.stage.Modality;

import java.util.Objects;


/**
 * Dialog with HTML/Webview that allows the user to accept or cancel a new annotation.
 */
public class BooleanBox {
    Stage window;
    ObjectProperty<Boolean> acceptProperty = new SimpleObjectProperty<>(false);
    private final String title;
    private final String html;

    public BooleanBox(final String title, final String html) {
        acceptProperty.setValue(false);
        this.title = title;
        this.html = html;
    }

    public void display() {
        window = new Stage(); // Create the stage.
        final int PREFERRED_WIDTH = 600;
        final int PREFERRED_HEIGHT = 500;
        window.initModality(Modality.APPLICATION_MODAL); // If window is up, make user handle it.
        window.setTitle(title);
        window.setMinHeight(PREFERRED_WIDTH);
        window.setMinWidth(PREFERRED_HEIGHT);
        window.setAlwaysOnTop(true);
        VBox vbox = new VBox();
        vbox.setPrefHeight(PREFERRED_HEIGHT);
        vbox.setPrefWidth(PREFERRED_WIDTH);
        WebView wview = new WebView();
        wview.getEngine().loadContent(html);
        Pane pane = new Pane();
        HBox hbox = new HBox();
        hbox.setPrefHeight(20);
        hbox.setPrefWidth(PREFERRED_WIDTH);
        Region region = new Region();
        region.setPrefHeight(20);
        region.setPrefWidth(PREFERRED_WIDTH);
        HBox.setHgrow(region, Priority.ALWAYS);
        final Button yesButton = new Button("Accept"); // Yes button for the user.
        final Button noButton = new Button("Cancel"); // No button for the user.
        VBox areaRight = new VBox();
        areaRight.setPrefSize(70, 20);
        VBox.setMargin(areaRight, new Insets(0, 0, 0, 50));
        hbox.getChildren().addAll(yesButton, areaRight, noButton);
        vbox.getChildren().addAll(wview, hbox);
        pane.getChildren().add(vbox);
        String css = Objects.requireNonNull(SettingsViewFactory.class.getResource("/css/loinc2hpo.css")).toExternalForm();
        Scene scene = new Scene(pane, PREFERRED_WIDTH, PREFERRED_HEIGHT);
        scene.getStylesheets().add(css);

        yesButton.setOnAction(e -> {
            acceptProperty.set(true);
            close();
        });

        noButton.setOnAction(e -> {
            acceptProperty.set(false);
            close();
        });


        window.setScene(scene);
        window.showAndWait();
    }

    private void close() {
        window.close();
    }

    public void addAcceptListener(final ChangeListener<Boolean> listener) {
        acceptProperty.addListener(listener);
    }
}
