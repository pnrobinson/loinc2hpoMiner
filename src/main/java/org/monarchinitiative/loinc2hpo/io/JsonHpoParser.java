package org.monarchinitiative.loinc2hpo.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.geneontology.obographs.model.GraphDocument;
import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.utils.CurieUtilBuilder;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.prefixcommons.CurieUtil;

import java.io.File;
import java.io.IOException;

public class JsonHpoParser {

    private final Ontology hpo;

    public JsonHpoParser(String hpoJsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        // skip fields not used in OBO such as domainRangeAxioms
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            File f = new File(hpoJsonPath);
            if (! f.isFile()) {
                throw new Loinc2HpoRunTimeException("Could not file hp.json file at " + f.getAbsolutePath());
            }
            GraphDocument gdoc = mapper.readValue(f, GraphDocument.class);
            //System.out.println(gdoc.toString());
            CurieUtil curieUtil =  CurieUtilBuilder.defaultCurieUtil();
            this.hpo = OntologyLoader.loadOntology(gdoc, curieUtil, "HP");

            } catch (IOException e) {
            throw new Loinc2HpoRunTimeException(e.getLocalizedMessage());
        }
    }

    public static Ontology loadOntology(String hpoJsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        // skip fields not used in OBO such as domainRangeAxioms
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            File f = new File(hpoJsonPath);
            if (! f.isFile()) {
                throw new Loinc2HpoRunTimeException("Could not file hp.json file at " + f.getAbsolutePath());
            }
            GraphDocument gdoc = mapper.readValue(f, GraphDocument.class);
            //System.out.println(gdoc.toString());
            CurieUtil curieUtil =  CurieUtilBuilder.defaultCurieUtil();
            return OntologyLoader.loadOntology(gdoc, curieUtil, "HP");

        } catch (IOException e) {
            throw new Loinc2HpoRunTimeException(e.getLocalizedMessage());
        }
    }

    public Ontology getHpo() {
        return hpo;
    }
}
