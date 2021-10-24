package org.monarchinitiative.loinc2hpo.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class parses the key:value setting file for LOINC2HPO app that is written to the user's home
 * directory, something like the following.
 * <pre>
 * loincTablePath:/some/path/LoincTableCore.csv
 * hp-json:/home/user/.loinc2hpo/hp.json
 * autosave to:/some/path
 * </pre>
 */
@Component
public class Settings {

    private static Logger logger = LoggerFactory.getLogger(Settings.class);

    private StringProperty hpoJsonPath;
    private StringProperty loincCoreTablePath;
    private StringProperty annotationFolder;
    private StringProperty biocuratorID;
    private Map<String, String> userCreatedLoincListsColor;
    private BooleanProperty isComplete = new SimpleBooleanProperty(false);

    public Settings() {
        this.hpoJsonPath = new SimpleStringProperty();
        this.loincCoreTablePath = new SimpleStringProperty();
        this.annotationFolder = new SimpleStringProperty();
        this.biocuratorID = new SimpleStringProperty();
        this.userCreatedLoincListsColor = new HashMap<>();
    }

    public Settings(String hpoJsonPath, String loincCoreTablePath, String annotationFolder, String biocuratorID, Map<String, String> userCreatedLoincListsColor) {
        this.hpoJsonPath = new SimpleStringProperty(hpoJsonPath);
        this.loincCoreTablePath = new SimpleStringProperty(loincCoreTablePath);
        this.annotationFolder = new SimpleStringProperty(annotationFolder);
        this.biocuratorID = new SimpleStringProperty(biocuratorID);
        this.userCreatedLoincListsColor = userCreatedLoincListsColor;
    }

    public static Settings loadSettings(Settings settings, String settingsPath) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(settingsPath));
        String line = null;
        while ((line = br.readLine()) != null) {
            int idx=line.indexOf(":");
            if (idx<0) {
                logger.error("Malformed settings line (no semicolon): "+line);
            }
            if (line.length()<idx+2) {
                logger.error("Malformed settings line (value too short): "+line);
            }
            String key,value;
            key=line.substring(0,idx).trim();
            value=line.substring(idx+1).trim();

            if (key.equals("biocuratorid")) settings.setBiocuratorID(value);
            else if (key.equals("loincTablePath")) settings.setLoincCoreTablePath(value);
            else if (key.equals("hp-json")) settings.setHpoJsonPath(value);
            else if (key.equals("autosave to")) settings.setAnnotationFolder(value);
            else if (key.equals("loinc-list-color")) {
                String[] entries = value.split("\\|");
                settings.setUserCreatedLoincListsColor(
                        Arrays.stream(entries)
                        .map(e -> e.split(",")) //has to be two elements
                        .collect(Collectors.toMap(e -> e[0], e -> e[1])));
            }
        }
        br.close();

        return settings;
    }

    public static void writeSettings(Settings settings, String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            String biocuratorID = settings.getBiocuratorID();
            String pathToLoincCoreTableFile = settings.getLoincCoreTablePath();
            String pathToHpoJsonFile = settings.getHpoJsonPath();
            String pathToAutoSavedFolder = settings.getAnnotationFolder();
            Map<String, String> userCreatedLoincListsColor = settings.getUserCreatedLoincListsColor();
            if (biocuratorID!=null) {
                bw.write(String.format("biocuratorid:%s\n",biocuratorID));
            }
            if (pathToLoincCoreTableFile!=null) {
                bw.write(String.format("loincTablePath:%s\n",pathToLoincCoreTableFile));
            }

            if (pathToHpoJsonFile !=null) {
                bw.write(String.format("hp-json:%s\n", pathToHpoJsonFile));
            }
            if (pathToAutoSavedFolder != null) {
                bw.write(String.format("autosave to:%s\n", pathToAutoSavedFolder));
            }
            if (!userCreatedLoincListsColor.isEmpty()) {
                bw.write("loinc-list-color:");
                List<String> list_color_pair = userCreatedLoincListsColor.entrySet().stream()
                        .map(e -> e.getKey() + "," + e.getValue())
                        .collect(Collectors.toList());
                bw.write(String.join("|", list_color_pair));
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not write settings at " + path);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        Settings.logger = logger;
    }

    public String getHpoJsonPath() {
        return hpoJsonPath.get();
    }

    public StringProperty hpoJsonPathProperty() {
        return hpoJsonPath;
    }

    public void setHpoJsonPath(String hpoJsonPath) {
        this.hpoJsonPath.set(hpoJsonPath);
        this.isComplete.set(status());
    }


    public String getLoincCoreTablePath() {
        return loincCoreTablePath.get();
    }

    public StringProperty loincCoreTablePathProperty() {
        return loincCoreTablePath;
    }

    public void setLoincCoreTablePath(String loincCoreTablePath) {
        this.loincCoreTablePath.set(loincCoreTablePath);
        this.isComplete.set(status());
    }

    public String getAnnotationFolder() {
        return annotationFolder.get();
    }

    public StringProperty annotationFolderProperty() {
        return annotationFolder;
    }

    public void setAnnotationFolder(String annotationFolder) {
        this.annotationFolder.set(annotationFolder);
        this.isComplete.set(status());
    }

    public String getBiocuratorID() {
        return biocuratorID.get();
    }

    public StringProperty biocuratorIDProperty() {
        return biocuratorID;
    }

    public void setBiocuratorID(String biocuratorID) {
        this.biocuratorID.set(biocuratorID);
        this.isComplete.set(status());
    }

    public Map<String, String> getUserCreatedLoincListsColor() {
        return userCreatedLoincListsColor;
    }

    public void setUserCreatedLoincListsColor(Map<String, String> userCreatedLoincListsColor) {
        this.userCreatedLoincListsColor = userCreatedLoincListsColor;
    }

    public BooleanProperty isCompleteProperty() {
        return isComplete;
    }


    public boolean status() {
        return this.hpoJsonPath.get() != null &&
                this.loincCoreTablePath.get() != null &&
                this.annotationFolder.get() != null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("hp.json: " + hpoJsonPath);
        builder.append("\n");
        builder.append("loincCoreTable: " + loincCoreTablePath);
        builder.append("\n");
        builder.append("annotationFolder: " + annotationFolder);
        return builder.toString();
    }


}
