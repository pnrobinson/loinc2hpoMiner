package org.monarchinitiative.loinc2hpo.query;

import org.monarchinitiative.loinc2hpo.model.HpoClassFound;
import org.monarchinitiative.loinc2hpocore.annotation.Loinc2HpoAnnotation;
import org.monarchinitiative.loinc2hpocore.annotation.LoincAnnotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This class implements querying capabilities that allow client code to
 * submit a query with one or more words and search for HPO terms that
 * contain these words. Users can also query according to HPO id, e.g., HP:0001234.
 * @author Peter Robinson, Aaron Zhang
 */
public record HpoQuery(Ontology hpo) {
    /**
     * Search for either an HPO id (if the query string consists of an HP term id) or
     * by strings that can match part or all of an HPO term label or synonym
     * @param query one or more words or an HPO Id
     * @return list of HPO terms with exact match to HPO id or at least partial match with text
     */
    public List<HpoClassFound> query(String query) {
        query = query.trim().toLowerCase(Locale.ROOT);
        if (query.startsWith("hp:") && query.length() == 10) {
            TermId tid = TermId.of(query);
            return queryByTermId(tid);
        }
        // if we get here, try a text match
        List<HpoClassFound> hits = new ArrayList<>();
        for (Term term : hpo.getTermMap().values()) {
            if (term.getName().toLowerCase(Locale.ROOT).contains(query)) {
                hits.add(new HpoClassFound(term));
            } else if (term.getDefinition().toLowerCase(Locale.ROOT).contains(query)) {
                hits.add(new HpoClassFound(term));
            } else {
                for (TermSynonym syn : term.getSynonyms()) {
                    if (syn.getValue().toLowerCase(Locale.ROOT).contains(query)) {
                        hits.add(new HpoClassFound(term));
                    }
                }
            }
        }
        return hits;
    }

    private List<HpoClassFound> queryByTermId(TermId tid) {
        for (TermId t : hpo.getTermMap().keySet()) {
            if (t.equals(tid)) {
                HpoClassFound hpoFound = new HpoClassFound(hpo.getTermMap().get(t));
                return List.of(hpoFound);
            }
        }
        return List.of();
    }

    /**
     * This function is called when the user wants to edit an existing term. By right click,
     * we get a {@link LoincAnnotation} object, and here we translate that into {@link HpoClassFound}
     * objects for display.
     * @param annot annotation to be edited
     * @return Corresponding {@link HpoClassFound} objects.
     */
    public List<HpoClassFound> getHposFromAnnotations(LoincAnnotation annot) {
        List<HpoClassFound> hpos = new ArrayList<>();
        List<Loinc2HpoAnnotation> allAnnots =  annot.allAnnotations();
        List<TermId> termIds = allAnnots.stream().map(Loinc2HpoAnnotation::getHpoTermId).collect(Collectors.toList());
        for (var tid : termIds) {
            if (this.hpo.getTermMap().containsKey(tid)) {
                hpos.add(new HpoClassFound(this.hpo.getTermMap().get(tid)));
            }
        }
        return hpos;
    }



}
