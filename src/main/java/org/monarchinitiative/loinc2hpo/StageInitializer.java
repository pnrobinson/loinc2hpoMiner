package org.monarchinitiative.loinc2hpo;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.monarchinitiative.loinc2hpo.guitools.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Component
public class StageInitializer implements ApplicationListener<Loinc2HpoApplication.StageReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInitializer.class);

    @Value("classpath:/fxml/main.fxml")
    private Resource loinc2hpoFxmlResource;
    private final String applicationTitle;

    private final ApplicationContext applicationContext;


    public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle, ApplicationContext context) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = context;
    }


    @Override
    public void onApplicationEvent(Loinc2HpoApplication.StageReadyEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(loinc2hpoFxmlResource.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 1200, 900));
            stage.setTitle(applicationTitle);
            stage.setResizable(false);
            readAppIcon().ifPresent(stage.getIcons()::add);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Image> readAppIcon() {
        if (Platform.isMacintosh()) {
            try {
                URL iconURL = StageInitializer.class.getResource("/img/phenomenon.png");
                java.awt.Image macimage = new ImageIcon(iconURL).getImage();
                // not working
                // com.apple.eawt.Application.getApplication().setDockIconImage(macimage);
            } catch (Exception e) {
                // Won't work on Windows or Linux. Just skip it!
            }
        }
        try (InputStream is = StageInitializer.class.getResourceAsStream("/img/phenomenon.png")) {
            if (is != null) {
                return Optional.of(new Image(is));
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading app icon {}", e.getMessage());
        }
        return Optional.empty();
    }
}
