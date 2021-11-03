package org.monarchinitiative.loinc2hpo;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.loinc2hpo.io.loincparser.LoincVsHpoQuery;
import org.monarchinitiative.loinc2hpo.model.Settings;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotation;
import org.monarchinitiative.loinc2hpocore.annotationmodel.LoincAnnotation;
import org.monarchinitiative.loinc2hpocore.io.Loinc2HpoAnnotationParser;
import org.monarchinitiative.loinc2hpocore.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


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


    private final StringProperty hpoJsonPath = new SimpleStringProperty(this, null);

    /** The annotation file and the loinc2HpoAnnotations are the user-defined LOINC2HPO annotations. */
    private final StringProperty annotationFile = new SimpleStringProperty(this, null);
    public String getAnnotationFile() {
        return annotationFile.get();
    }
    public StringProperty annotationFileProperty() {
        return annotationFile;
    }
    public void setAnnotationFile(String annotationFile) {
        this.annotationFile.set(annotationFile);
    }
    private Map<LoincId, LoincAnnotation> loincAnnotationMap = null;
    public Map<LoincId, LoincAnnotation> getLoincAnnotations()   {
        if (loincAnnotationMap == null) {
            if (annotationFileProperty() == null) {
                return Map.of();
            }
            try {
                String path = annotationFileProperty().get();
                Loinc2HpoAnnotationParser parser = new Loinc2HpoAnnotationParser(path);
                this.loincAnnotationMap = parser.loincToHpoAnnotationMap();
            }catch (Loinc2HpoRunTimeException  e) {
                e.printStackTrace();
            }
        }
        return this.loincAnnotationMap;
    }

    public List<Loinc2HpoAnnotation> getIndividualLoinc2HpoAnnotations() {
        Map<LoincId, LoincAnnotation> annotMap = getLoincAnnotations();
        return annotMap.values().stream()
                .map(LoincAnnotation::allAnnotations)
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
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

    public String getLoincCoreTablePath() {
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


    private LoincVsHpoQuery loincVsHpoQuery = null;

    public LoincVsHpoQuery getLoincVsHpoQuery(){
        if (loincVsHpoQuery == null) {
            loincVsHpoQuery = new LoincVsHpoQuery(ontologyProperty().get());
        }
        return this.loincVsHpoQuery;
    }




}
