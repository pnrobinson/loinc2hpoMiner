package org.monarchinitiative.loinc2hpo;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monarchinitiative.loinc2hpo.guitools.Platform;
import org.monarchinitiative.loinc2hpo.guitools.PopUps;
import org.monarchinitiative.loinc2hpo.guitools.SettingsViewFactory;
import org.monarchinitiative.loinc2hpo.io.HpoMenuDownloader;
import org.monarchinitiative.loinc2hpo.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.monarchinitiative.loinc2hpo.guitools.PopUps.getStringFromUser;

@SuppressWarnings({"unchecked", "rawtypes"})
@Component
public class Loinc2HpoMainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(Loinc2HpoMainController.class);


    private final ExecutorService executor;

    private final OptionalResources optionalResources;

    private final Properties pgProperties;

    private final File appHomeDirectory;
    /**
     * The allows us to get info from the pom.xml file
     * buildProperties.getName()
     * buildProperties.getVersion();
     * buildProperties.getTime();
     * buildProperties.getArtifact();
     * buildProperties.getGroup();
     */
    @Autowired
    BuildProperties buildProperties;

    @Autowired
    Settings settings;


    @Autowired
    public Loinc2HpoMainController(OptionalResources optionalResources,
                                   ExecutorService executorService,
                                   Properties pgProperties,
                                   @Qualifier("appHomeDir") File appHomeDir) {
        this.optionalResources = optionalResources;
        this.executor = executorService;
        this.pgProperties = pgProperties;
        this.appHomeDirectory = appHomeDir;
        // this.tableHidden = new SimpleBooleanProperty(true);
    }


    @FXML
    public void downloadHPO(ActionEvent e) {
        String dirpath = Platform.getLoinc2HpoDir().getAbsolutePath();
        File f = new File(dirpath);
        if (f == null || !(f.exists() && f.isDirectory())) {
            LOGGER.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }
        String BASENAME = "hp.json";

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading hp.obo/.owl...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO download");
        window.setScene(scene);
        Task<Void> hpodownload = new HpoMenuDownloader(dirpath);
        new Thread(hpodownload).start();
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            LOGGER.trace(String.format("Successfully downloaded hpo to %s", dirpath));
            String fullpath = String.format("%s%shp.json", dirpath, File.separator);

            settings.setHpoJsonPath(fullpath);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download HPO obo file");
        });

        e.consume();
    }


    @FXML
    public void setPathToLoincCoreTableFile(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose LOINC Core Table file");
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            settings.setLoincCoreTablePath(path);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
            LOGGER.trace(String.format("Setting path to LOINC Core Table file to %s", path));
        } else {
            LOGGER.error("Unable to obtain path to LOINC Core Table file");
        }
        e.consume();
    }

    /**
     * Show the about message
     */
    @FXML
    private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("LOINC2HPO Biocuration tool");
        alert.setHeaderText("Loinc2Hpo");
        String s = "Biocurate HPO mappings for LOINC laboratory codes.\nversion: " + buildProperties.getVersion();
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }

    /**
     * Get a biocurator string such as HP:rrabbit from the user.
     */
    @FXML
    private void setBiocuratorID(ActionEvent e) {
        String current = settings.getBiocuratorID();
        String prompText = (current == null || current.isEmpty()) ? "e.g., MGM:rrabbit" : current;
        String bcid = getStringFromUser("Biocurator ID", prompText, "Enter biocurator ID");
        if (bcid != null && bcid.indexOf(":") > 0) {
            settings.setBiocuratorID(bcid);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
        } else {
            LOGGER.error(String.format("Invalid biocurator ID; must be of the form MGM:rrabbit; you tried: \"%s\"",
                    bcid != null ? bcid : ""));
        }
        e.consume();
    }


    @FXML
    private void setPathToCurationData(ActionEvent e) {
        e.consume();
        System.err.println("WARNING -- SET PATH NOT IMPL");
    }

    /**
     * Open a help dialog
     */
    @FXML
    private void openHelpDialog() {
        System.err.println("[WARNING] Help dialog not implemented");
        //HelpViewFactory.openHelpDialog();
    }

    /**
     * Show the settings
     */
    @FXML
    private void openSettingsDialog() {
        SettingsViewFactory.openSettingsDialog(settings);
    }

    /**
     * The function determines whether the data in annotations map and loincCategories has changed
     *
     * @return
     */
    public boolean isSessionDataChanged() {
        //Lazy implementation
        //whenever createAnnotation, saveAnnotation, group/ungroup loinc or create loinc list are called, it return true
        //return appTempData.isSessionChanged();
        System.err.println("WARNING -- isSessionDataChanged not implemented");
        return true;
    }

    @FXML
    private void handleSave(ActionEvent e) {
        LOGGER.trace("handleSaveSession");
        //Create a session if it is saved for the first time
        if (settings.getAnnotationFolder() == null) {
            PopUps.showWarningDialog("Warning", "Error",
                    "Attempt to save files without annotation folder");
            return;
        }

        String dataDir = settings.getAnnotationFolder() + File.separator + "Data";
        System.err.println("WARNING -- SAVE NOT IMPLEMENTED");
//
 /*
        Path folderTSVSingle = Paths.get(dataDir + File.separator + Constants.TSVSingleFileFolder);
        if (!Files.exists(folderTSVSingle)) {
            try {
                Files.createDirectory(folderTSVSingle);
            } catch (IOException e1) {
                PopUps.showWarningDialog("Error message",
                        "Failure to create folder" ,
                        String.format("An error occurred when trying to make a directory at %s. Try again!", folderTSVSingle));
                return;
            }
        }

        String annotationTSVSingleFile = folderTSVSingle.toString() + File.separator + Constants.TSVSingleFileName;
        try {
            Loinc2HpoAnnotationModel.to_csv_file(appResources.getLoincAnnotationMap(), annotationTSVSingleFile);
        } catch (IOException e1) {
            PopUps.showWarningDialog("Error message",
                    "Failure to Save Session Data" ,
                    String.format("An error occurred when trying to save data to %s. Try again!", annotationTSVSingleFile));
            return;
        }

        String pathToLoincCategory = dataDir + File.separator + LOINC_CATEGORY_folder;
        if (!new File(pathToLoincCategory).exists()) {
            new File(pathToLoincCategory).mkdir();
        }
        appResources.getUserCreatedLoincLists().entrySet()
                .forEach(p -> {
                    String path = pathToLoincCategory + File.separator + p.getKey() + ".txt";
                    Set<LoincId> loincIds = appResources.getUserCreatedLoincLists().get(p.getKey());
                    StringBuilder builder = new StringBuilder();
                    loincIds.forEach(l -> {
                        builder.append (l);
                        builder.append("\n");
                    });
                    WriteToFile.writeToFile(builder.toString().trim(), path);
                });

        //reset the session change tracker
        appTempData.setSessionChanged(false);
           */
        if (e != null) {
            e.consume();
        }

    }

    public void saveBeforeExit() {
        LOGGER.trace("SaveBeforeExit() is called");
        if (isSessionDataChanged()) {
            handleSave(null);
        } else {
            LOGGER.trace("data not changed. exit safely");
        }
    }

    @FXML
    public void close(ActionEvent e) {

        e.consume(); //important to consume it first; otherwise,
        //window will always close
        if (isSessionDataChanged()) {

            String[] choices = new String[]{"Yes", "No"};
            Optional<String> choice = PopUps.getToggleChoiceFromUser(choices,
                    "Session has been changed. Save changes? ", "Exit " +
                            "Confirmation");


            if (choice.isPresent() && choice.get().equals("Yes")) {
                saveBeforeExit();
                javafx.application.Platform.exit();
                System.exit(0);
                //window.close();
            } else if (choice.isPresent() && choice.get().equals("No")) {
                javafx.application.Platform.exit();
                System.exit(0);
                //window.close();
            } else {
                //hang on. No action required
            }
        } else {
            javafx.application.Platform.exit();
            System.exit(0);
            //window.close();
        }
    }

}
