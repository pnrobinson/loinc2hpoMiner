package org.monarchinitiative.loinc2hpo.io.loincparser;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class LoincVsHpoQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoincVsHpoQuery.class);

    private final Ontology hpo;


    public static final String modifier = "increase.*|decrease.*|elevat.*|reduc.*|high.*|low.*|above|below|abnormal.*";

    public LoincVsHpoQuery(Ontology ontology) {
        hpo = ontology;
    }


    public List<HpoClassFound> query_manual(String keysString,
                                            LoincLongNameComponents loincLongNameComponents) {
        String [] keys = keysString.split(" ");
        return query_manual(List.of(keys), loincLongNameComponents);
    }
    /**
     * A method to do manual query with provided keys (literally)
     */
    public List<HpoClassFound> query_manual(List<String> keys,
                                                   LoincLongNameComponents loincLongNameComponents) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
           //HpoClassFound(String id, String label, String definition, LoincLongNameComponents loinc) {
            List<HpoClassFound> foundList = new ArrayList<>();
            List<String> searchItems = new ArrayList<>();
            searchItems.addAll(keys);
            searchItems.add(loincLongNameComponents.getLoincTissue());
            searchItems.add(loincLongNameComponents.getLoincMethod());
            searchItems.add(loincLongNameComponents.getLoincParameter());
            searchItems.add(loincLongNameComponents.getLoincType());
            LOGGER.info("got {} search items",searchItems.size());
            for (Term term : hpo.getTermMap().values()) {
                Set<String> words = getWords(term);
                for (var word : searchItems) {
                    if (words.contains(word)) {
                        foundList.add(new HpoClassFound(term, loincLongNameComponents));
                        break;
                    }
                }
            }
            LOGGER.info("got {} found items",foundList.size());
            return foundList;
        }
    }

    private Set<String> getWords(Term term) {
        Set<String> words = new HashSet<>();
        String label = term.getName();
        String [] labelWords = label.split("\s+");
        words.addAll(List.of(labelWords));
        for (var synonym: term.getSynonyms()) {
            String synonymLabel = synonym.getValue();
            //System.out.println(synonymLabel);
            String [] synonymWords = synonymLabel.split("\s+");
            words.addAll(List.of(synonymWords));
        }
        return words;
    }


}
