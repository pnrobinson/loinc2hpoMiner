package org.monarchinitiative.loinc2hpo.query;

import org.monarchinitiative.loinc2hpo.model.HpoClassFound;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HpoQuery {

    private final Ontology hpo;


    public HpoQuery(Ontology hpo) {
        this.hpo = hpo;
    }

    /**
     * @param query one or more words or an HPO Id
     * @return list of HPO terms with exact match to HPO id or at least partial match with text
     */
    public List<HpoClassFound> query(String query) {
        query = query.trim();
        if (query.startsWith("HP:") && query.length() == 10) {
            TermId tid = TermId.of(query);
            return queryByTermId(tid);
        }
        // if we get here, try a text match
        List<HpoClassFound> hits = new ArrayList<>();
        for (Term term : hpo.getTermMap().values()) {
            if (term.getName().contains(query)) {
                hits.add(new HpoClassFound(term));
            } else if (term.getDefinition().contains(query)) {
                hits.add(new HpoClassFound(term));
            } else {
                for (TermSynonym syn : term.getSynonyms()) {
                    if (syn.getValue().contains(query)) {
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



}
