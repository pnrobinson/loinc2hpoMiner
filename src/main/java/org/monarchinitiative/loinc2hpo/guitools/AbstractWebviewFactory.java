package org.monarchinitiative.loinc2hpo.guitools;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.monarchinitiative.loinc2hpo.model.Settings;

public abstract class AbstractWebviewFactory {

    AbstractWebviewFactory() {

    }

    abstract String getHTML();

    abstract void openDialog();

    protected static String inlineCSS() {
        return "<head><style>\n" +
                "  html { margin: 0; padding: 0; }" +
                "body { font: 100% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 10; padding: 10; }"+
                "p { margin-top: 0;text-align: justify;}"+
                "h2,h3 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;"+
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}"+
                "  </style></head>";
    }



    /** Open a dialog that provides concise help for using PhenoteFX. */
    public static void openDialog(String windowTitle, String html) {
        Stage window;
        window = new Stage();
        window.setOnCloseRequest( event -> {window.close();} );
        window.setTitle(windowTitle);
        Pane pane = new Pane();
        VBox vbox =  new VBox();
        vbox.setPrefHeight(600);
        vbox.setPrefWidth(800);
        WebView wview = new WebView();
        wview.getEngine().loadContent(html);
        pane.getChildren().add(vbox);
        HBox hbox = new HBox();
        hbox.setPrefHeight(40);
        hbox.setPrefWidth(800);
        Region region=new Region();
        region.setPrefHeight(40);
        region.setPrefWidth(400);
        hbox.setHgrow(region, Priority.ALWAYS);
        Button button = new Button("Close");
        HBox.setMargin(button,new Insets(10, 10, 10, 0));
        button.setOnAction( e->window.close());
        hbox.getChildren().addAll(region,button);
        vbox.getChildren().addAll(wview,hbox);
        Scene scene = new Scene(pane, 1200, 600);
        String css = SettingsViewFactory.class.getResource("/css/loinc2hpo.css").toExternalForm();
        scene.getStylesheets().add(css);
        window.setScene(scene);
        window.showAndWait();
    }

}
