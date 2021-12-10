package org.monarchinitiative.loinc2hpo.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

import static org.monarchinitiative.loinc2hpo.guitools.Platform.getLoinc2HpoDir;
import static org.monarchinitiative.loinc2hpo.guitools.Platform.getPathToSettingsFile;

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

    private final StringProperty hpoJsonPath;
    private final StringProperty loincCoreTablePath;
    /** Path to the LOINC2HPO annotations.tsv file */
    private final StringProperty annotationFile;
    private final StringProperty biocuratorID;
    private final BooleanProperty isComplete = new SimpleBooleanProperty(false);

    public Settings() {
        this.hpoJsonPath = new SimpleStringProperty();
        this.loincCoreTablePath = new SimpleStringProperty();
        this.annotationFile = new SimpleStringProperty();
        this.biocuratorID = new SimpleStringProperty();
    }

    public Settings(String hpoJsonPath, String loincCoreTablePath, String annotationTsv, String biocuratorID) {
        this.hpoJsonPath = new SimpleStringProperty(hpoJsonPath);
        this.loincCoreTablePath = new SimpleStringProperty(loincCoreTablePath);
        this.annotationFile = new SimpleStringProperty(annotationTsv);
        this.biocuratorID = new SimpleStringProperty(biocuratorID);
    }

    public static Settings loadSettings(Settings settings, String settingsPath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(settingsPath));
        String line;
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

            switch (key) {
                case "biocuratorid" -> settings.setBiocuratorID(value);
                case "loincTablePath" -> settings.setLoincCoreTablePath(value);
                case "hp-json" -> settings.setHpoJsonPath(value);
                case "autosave to" -> settings.setAnnotationFile(value);
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
            String pathToAutoSavedFolder = settings.getAnnotationFile();
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
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not write settings at " + path);
        }
    }

    /**
     * This function will create the .loinc2hpo directory in the user's home directory if it does not yet exist.
     * Then it will return the path of the settings file.
     * @return path to the loinc2hpominer settings file
     */
    public static File getPathToSettingsFileAndEnsurePathExists() {
        File loinc2HpoUserDir = getLoinc2HpoDir();
        if (loinc2HpoUserDir == null) {
            logger.error("Unable to create LOINC2HPO config directory.\n"
                    + "Even though this is a serious problem I'm exiting gracefully. Bye.");
            System.exit(1);
        }
        if (!loinc2HpoUserDir.exists()) {
            File fck = new File(loinc2HpoUserDir.getAbsolutePath());
            if (!fck.mkdir()) { // make sure config directory is created, exit if not
                logger.error("Unable to create LOINC2HPO config directory.\n"
                        + "Even though this is a serious problem I'm exiting gracefully. Bye.");
                System.exit(1);
            }
        }
        String defaultSettingsPath = getPathToSettingsFile();
        return new File(defaultSettingsPath);
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

    public String getAnnotationFile() {
        return annotationFile.get();
    }

    public StringProperty annotationFileProperty() {
        return annotationFile;
    }

    public void setAnnotationFile(String annotationFile) {
        this.annotationFile.set(annotationFile);
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

    public BooleanProperty isCompleteProperty() {
        return isComplete;
    }


    public boolean status() {
        return this.hpoJsonPath.get() != null &&
                this.loincCoreTablePath.get() != null &&
                this.annotationFile.get() != null;
    }

    public String toString() {
        return "hp.json: " + hpoJsonPath +
                "\n" +
                "loincCoreTable: " + loincCoreTablePath +
                "\n" +
                "annotationFile: " + annotationFile;
    }


}
