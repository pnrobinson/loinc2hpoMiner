package org.monarchinitiative.loinc2hpo;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.monarchinitiative.loinc2hpo.model.Settings;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class OptionalResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionalResources.class);
    /**
     * Use this name to save HP.json file on the local filesystem.
     */
    public static final String DEFAULT_HPO_FILE_NAME = "hp.json";
    public static final String BIOCURATOR_ID_PROPERTY = "biocurator.id";
    public static final String ONTOLOGY_PATH_PROPERTY = "hp.json.path";

    // default value does not harm here
    private final ObjectProperty<Ontology> ontology = new SimpleObjectProperty<>(this, "ontology");

    public Map<LoincId, LoincEntry> getLoincTableMap() {
        return loincTableMap.get();
    }

    public ObjectProperty<Map<LoincId, LoincEntry>> loincTableMapProperty() {
        return loincTableMap;
    }

    public void setLoincTableMap(Map<LoincId, LoincEntry> loincTableMap) {
        this.loincTableMap.set(loincTableMap);
    }

    private final ObjectProperty<Map<LoincId, LoincEntry>> loincTableMap = new SimpleObjectProperty<>(this, "loincmap");

    private final StringProperty biocurator = new SimpleStringProperty(this, "biocurator.id");

    private final StringProperty loincCoreTable = new SimpleStringProperty(this, null);

    private final StringProperty annotationFile = new SimpleStringProperty(this, null);

    private final StringProperty hpoJsonPath = new SimpleStringProperty(this, null);

    public String getAnnotationFile() {
        return annotationFile.get();
    }

    public StringProperty annotationFileProperty() {
        return annotationFile;
    }

    public void setAnnotationFile(String annotationFile) {
        this.annotationFile.set(annotationFile);
    }

    public Ontology getOntology() {
        return ontology.get();
    }


    public void setOntology(Ontology ontology) {
        this.ontology.set(ontology);
    }


    public ObjectProperty<Ontology> ontologyProperty() {
        return ontology;
    }

    public void setBiocurator(String id) {
        biocurator.setValue(id);
    }

    public StringProperty biocuratorIdProperty() { return biocurator; }

    public String getBiocurator() {
        return biocurator.get();
    }

    public StringProperty biocuratorProperty() {
        return biocurator;
    }

    public String getLoincCoreTable() {
        return loincCoreTable.get();
    }

    public StringProperty loincCoreTableProperty() {
        return loincCoreTable;
    }

    public void setLoincCoreTable(String loincCoreTable) {
        this.loincCoreTable.set(loincCoreTable);
    }

    public String getHpoJsonPath() {
        return hpoJsonPath.get();
    }

    public StringProperty hpoJsonPathProperty() {
        return hpoJsonPath;
    }

    public void setHpoJsonPath(String hpoJsonPath) {
        this.hpoJsonPath.set(hpoJsonPath);
    }

    public void addSettings(Settings settings) {
        setBiocurator(settings.getBiocuratorID());
        setLoincCoreTable(settings.getLoincCoreTablePath());
        setAnnotationFile(settings.getAnnotationFile());
        setHpoJsonPath(settings.getHpoJsonPath());
    }
}
