package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.loinc2hpocore.annotation.Loinc2HpoAnnotation;
import org.monarchinitiative.loinc2hpocore.annotation.LoincAnnotation;
import org.monarchinitiative.loinc2hpocore.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LoincQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoincQuery.class);

    private final Map<LoincId, LoincEntry> loincEntryMap;
    /** Do not try to match on these words in LOINC labels. */
    private final static Set<String> omitWords = Set.of("in", "or", "and", "Serum", "Plasma", "Dose", "Blood");

    public static final String modifier = "increase.*|decrease.*|elevat.*|reduc.*|high.*|low.*|above|below|abnormal.*";

    public LoincQuery(Map<LoincId, LoincEntry>  loincMap) {
        this.loincEntryMap = loincMap;
    }

    public List<LoincEntry> query(String queryText) {
        List<LoincId> idsList = this.loincEntryMap.keySet()
                .stream()
                .filter(loincId -> loincId.toString().equals(queryText))
                .collect(Collectors.toList());
        Set<LoincEntry> annots = this.loincEntryMap.values()
                .stream()
                .filter(entry -> entry.getLoincLongName().getLoincParameter().equalsIgnoreCase(queryText))
                .collect(Collectors.toSet());
        for (LoincId id : idsList) {
            annots.add(loincEntryMap.get(id));
        }
        return new ArrayList<>(annots);
    }

}
