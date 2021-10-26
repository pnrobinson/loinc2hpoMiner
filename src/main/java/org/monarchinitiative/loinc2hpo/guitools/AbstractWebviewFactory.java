package org.monarchinitiative.loinc2hpo.guitools;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractWebviewFactory {

    AbstractWebviewFactory() {

    }

    abstract String getHTML();

    abstract void openDialog();

    abstract boolean openDialogWithBoolean();

    private boolean accept;

    protected static String inlineCSS() {
        return "<head><style>\n" +
                "  html { margin: 0; padding: 0; }" +
                "body { font: 80% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 10; padding: 10; }"+
                "p { margin-top: 0;text-align: justify;}"+
                "h2,h3 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;"+
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}"+
                "  </style></head>";
    }



    /** Open a dialog that provides concise help for using PhenoteFX. */
    public static void openDialog(String windowTitle, String html) {
        final int PREFERRED_WIDTH = 600;
        final int PREFERRED_HEIGHT = 500;
        Stage window;
        window = new Stage();
        window.setOnCloseRequest( event -> window.close());
        window.setTitle(windowTitle);
        Pane pane = new Pane();
        VBox vbox =  new VBox();
        vbox.setPrefHeight(PREFERRED_HEIGHT);
        vbox.setPrefWidth(PREFERRED_WIDTH);
        WebView wview = new WebView();
        wview.getEngine().loadContent(html);
        pane.getChildren().add(vbox);
        HBox hbox = new HBox();
        hbox.setPrefHeight(20);
        hbox.setPrefWidth(PREFERRED_WIDTH);
        Region region=new Region();
        region.setPrefHeight(20);
        region.setPrefWidth(PREFERRED_WIDTH);
        HBox.setHgrow(region, Priority.ALWAYS);
        Button button = new Button("Close");
        HBox.setMargin(button,new Insets(10, 10, 10, 0));
        button.setOnAction( e->window.close());
        hbox.getChildren().addAll(region);
        vbox.getChildren().addAll(wview,hbox,button);
        Scene scene = new Scene(pane, PREFERRED_WIDTH, PREFERRED_HEIGHT);
        String css = Objects.requireNonNull(SettingsViewFactory.class.getResource("/css/loinc2hpo.css")).toExternalForm();
        scene.getStylesheets().add(css);
        window.setScene(scene);
        window.showAndWait();
    }

    private void setAccept(boolean b) {
        accept = b;
    }

    /** Open a dialog that provides concise help for using PhenoteFX. */
    public boolean openDialogWithBoolean(String windowTitle, String html) {
        boolean accept = false;
        final BooleanBox bbox = new BooleanBox(windowTitle,html);
        bbox.addCancelListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setAccept(true);
            } else {
                setAccept(false);
            }
        });
        bbox.display();
        return accept;
    }

}
