package org.monarchinitiative.loinc2hpo.guitools;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;


/**
 * To use this class
 * final BooleanBox warning = new AlertBox("Warning!",
 * "Are you sure you would like to do this?");
 * warning.addCancelListener(new ChangeListener<Boolean>() {
 *
 * @Override public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
 * if (newValue) {
 * System.out.println("Tschüss");
 * } else {
 * System.out.println("Thanks for confidence");
 * }
 * }
 * });
 */
public class BooleanBox {
    Stage window;
    ObjectProperty<Boolean> cancel = new SimpleObjectProperty<>(null);
    private final String title;
    private final String html;

    public BooleanBox(final String title, final String html) {
        cancel.setValue(null);
        this.title = title;
        this.html = html;
    }

    public void display() {
        window = new Stage(); // Create the stage.

        window.initModality(Modality.APPLICATION_MODAL); // If window is up, make user handle it.
        window.setTitle(title);
        window.setMinHeight(350);
        window.setMinWidth(250);
        window.setAlwaysOnTop(true);

        final int PREFERRED_WIDTH = 600;
        final int PREFERRED_HEIGHT = 500;
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        final Scene scene = alert.getDialogPane().getScene();
        Pane pane = alert.getDialogPane();
        VBox vbox = new VBox();
        vbox.setPrefHeight(PREFERRED_HEIGHT);
        vbox.setPrefWidth(PREFERRED_WIDTH);
        WebView wview = new WebView();
        wview.getEngine().loadContent(html);
        pane.getChildren().add(vbox);
        HBox hbox = new HBox();
        hbox.setPrefHeight(20);
        hbox.setPrefWidth(PREFERRED_WIDTH);
        Region region = new Region();
        region.setPrefHeight(20);
        region.setPrefWidth(PREFERRED_WIDTH);
        HBox.setHgrow(region, Priority.ALWAYS);
        final Button yesButton = new Button("Accept"); // Yes button for the user.
        final Button noButton = new Button("Cancel"); // No button for the user.
        hbox.getChildren().addAll(region);
        vbox.getChildren().addAll(wview, hbox, yesButton, noButton);
        String css = Objects.requireNonNull(SettingsViewFactory.class.getResource("/css/loinc2hpo.css")).toExternalForm();
        scene.getStylesheets().add(css);

        yesButton.setOnAction(e -> {
            cancel.set(false);
            close();
        });

        noButton.setOnAction(e -> {
            cancel.set(true);
            close();
        });


        window.setScene(scene);
        window.showAndWait();
    }

    private void close() {
        window.close();
    }

    public void addCancelListener(final ChangeListener<Boolean> listener) {
        cancel.addListener(listener);
    }
}
