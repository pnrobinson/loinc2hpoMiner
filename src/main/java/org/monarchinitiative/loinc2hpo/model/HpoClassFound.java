package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.loinc2hpocore.annotation.Loinc2HpoAnnotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Optional;


/**
 * This class represents an HPO class returned by a Sparql query. The class
 * keeps a selected information of those HPO classes, id/uri, label and
 * definition. It also keeps record of what loinc code is used for finding
 * the HPO class. The class is comparable by the scores it receives from
 * keyword matching (from the loinc code used for query and the HPO class).
 */
public class HpoClassFound {

    private final TermId id;
    private final String label;
    private final String definition;


    public String getDefinition() {
        return definition;
    }

    public HpoClassFound(Term term) {
        this.id = term.getId();
        this.label = term.getName();
        this.definition = term.getDefinition();
    }



    public String getId() {
        return this.id.getValue();
    }

    public String getLabel() {
        return this.label;
    }


    @Override
    public String toString(){
        return getId() + "\t" + getLabel() ;
    }


    public static Optional<HpoClassFound> fromLoinc2HpoAnnotation(Loinc2HpoAnnotation annot, Ontology ontology) {
        TermId hpoIt = annot.getHpoTermId();
        if (ontology.containsTerm(hpoIt)) {
            Term term = ontology.getTermMap().get(hpoIt);
            return Optional.of(new HpoClassFound(term));
        } else {
            return Optional.empty();
        }
    }
}
