package org.monarchinitiative.loinc2hpo;

import javafx.concurrent.Task;
import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.loinc2hpo.io.JsonHpoParser;
import org.monarchinitiative.loinc2hpocore.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Initialization of the GUI resources is being done here. Information from {@link Properties} parsed from
 * the Project settings that are stored in the user home directory entry for this application.
 * Changes made by user are stored for the next run in the stop() method. We place the Ontology, the
 * Loinc Table, and the annotation file path in the OptionalResourses object if we can.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.0.3
 * @since 0.0
 */
public final class StartupTask extends Task<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupTask.class);

    private final OptionalResources optionalResources;

    private final Properties pgProperties;


    public StartupTask(OptionalResources optionalResources, Properties pgProperties) {
        this.optionalResources = optionalResources;
        this.pgProperties = pgProperties;
    }

    /**
     * Read {@link Properties} and initialize app resources in the {@link OptionalResources}:
     *
     * <ul>
     * <li>HPO ontology</li>
     * </ul>
     *
     * @return nothing
     */
    @Override
    protected Void call() {
        /*
        This is the place where we deserialize HPO ontology if we know path to the OBO file.
        We need to make sure to set ontology property of `optionalResources` to null if loading fails.
        This way we ensure that GUI elements dependent on ontology presence (labels, buttons) stay disabled
        and that the user will be notified about the fact that the ontology is missing.
         */
        ingestOntology();
        ingestLoincTable();
        return null;
    }


    private void ingestLoincTable() {
        String loincTablePath = optionalResources.getLoincCoreTablePath();
        Map<LoincId, LoincEntry> loincMap = new HashMap<>();
        int count_malformed = 0;
        int count_correct = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(loincTablePath))){
            String line = br.readLine(); // first line is header
            if (! line.contains("\"LOINC_NUM\"")) {
                LOGGER.error(String.format("Malformed header line (%s) in Loinc File %s",line,loincTablePath));
                optionalResources.setLoincTableMap(Map.of());
            }
            while ((line=br.readLine())!=null) {
                try {
                    LoincEntry entry = LoincEntry.fromQuotedCsvLine(line);
                    loincMap.put(entry.getLoincId(),entry);
                    count_correct++;
                } catch (Loinc2HpoRunTimeException e) {
                    LOGGER.error("Malformed loinc code in the line:\n " + line);
                    count_malformed++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info(count_correct+ " loinc entries were ingested");
        if (count_malformed > 0) {
            LOGGER.warn(count_malformed + " loinc numbers (identifiers) were malformed");
        }
        // set to an immutable map
        optionalResources.setLoincTableMap(Map.copyOf(loincMap));
    }


    /**
     * Load the HPO using phenol and set the corresponding fields in OptionalResources
     */
    private void ingestOntology() {
        String ontologyPath = optionalResources.getHpoJsonPath();
        if (ontologyPath != null) {
            final File hpJsonFile = new File(ontologyPath);
            if (hpJsonFile.isFile()) {
                String msg = String.format("Loading HPO from file '%s'", hpJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                try {
                    final Ontology ontology = JsonHpoParser.loadOntology(ontologyPath);
                    optionalResources.setOntology(ontology);
                    updateMessage("Ontology loaded");
                } catch (Exception e) {
                    updateMessage(String.format("Error loading HPO file : %s", e.getMessage()));
                    LOGGER.warn("Error loading HPO file: ", e);
                    optionalResources.setOntology(null);
                }
            } else {
                optionalResources.setOntology(null);
            }
        } else {
            String msg = "Need to set path to hp.obo file (See edit menu)";
            updateMessage(msg);
            LOGGER.info(msg);
            optionalResources.setOntology(null);
        }
    }

}
