package org.monarchinitiative.loinc2hpo;

import javafx.concurrent.Task;
import org.monarchinitiative.loinc2hpo.io.JsonHpoParser;
import org.monarchinitiative.loinc2hpocore.io.LoincTableCoreParser;
import org.monarchinitiative.loinc2hpocore.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        LOGGER.info("Starting Startup task");
        ingestOntology();
        ingestLoincTable();
        LOGGER.info("Finishing Startup task");
        return null;
    }

    private void ingestLoinc2HpoAnnotations() {
       // optionalResources.lo
    }


    private void ingestLoincTable() {
        String loincTablePath = optionalResources.getLoincCoreTablePath();
        LOGGER.info("Loading LOINC from {}", loincTablePath);
        LoincTableCoreParser coreParser = new LoincTableCoreParser(loincTablePath);
        Map<LoincId, LoincEntry> loincMap = coreParser.getLoincEntries();
        // set to an immutable map
        optionalResources.setLoincTableMap(Map.copyOf(loincMap));
    }


    /**
     * Load the HPO using phenol and set the corresponding fields in OptionalResources
     */
    private void ingestOntology() {
        String ontologyPath = optionalResources.getHpoJsonPath();
        LOGGER.info("Ingesting HPO from {}", ontologyPath);
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
                    LOGGER.info("Ontology loaded");
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
