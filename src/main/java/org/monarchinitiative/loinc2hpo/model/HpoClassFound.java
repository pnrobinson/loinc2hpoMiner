package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.loinc2hpocore.loinc.LoincLongName;
import org.monarchinitiative.phenol.ontology.data.Term;

import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an HPO class returned by a Sparql query. The class
 * keeps a selected information of those HPO classes, id/uri, label and
 * definition. It also keeps record of what loinc code is used for finding
 * the HPO class. The class is comparable by the scores it receives from
 * keyword matching (from the loinc code used for query and the HPO class).
 */
public class HpoClassFound implements Comparable<HpoClassFound> {

    private String id; //uri of HPO class. Actual id is the last
    // (split by '/') in the form of HP_12345
    private String label; //all classes should have a non-null label
    private String definition; //some classes do not have a definition
    private final int score; //how well the HPO class matches the loinc code (long
                        // common name)

    public HpoClassFound(String id, String label, String definition) {
        this.id = id;
        this.label = label;
        this.definition = definition;
        this.score = 0;
    }

    public HpoClassFound(Term term) {
        this.id = term.getId().getValue();
        this.label = term.getName();
        this.definition = term.getDefinition();
        this.score = 0;
    }

    /**
     * A helper method to calculate the score. It does so by three steps:
     * 1. if the class have a modifier (increase/decrease...), it receives 50
     * points.
     * 2. it examines the keys in loinc "parameter". A complete match
     * receives 30 points. A partial match receives points based on how many
     * keys are matched.
     * 3. it examines the keys in loinc "tissue". A complete match receives
     * 20 points. A partial match receives points based on how many keys are
     * matched.
     * @return score
     */
    public int priorityScore() {
        int matchScore = 0;
        String total = this.label;
        if (this.definition != null)
            total += (" " + this.definition);

        Pattern pattern = Pattern.compile(toPattern(LoincVsHpoQuery.modifier));
        Matcher matcher = pattern.matcher(total.toLowerCase());
        if (matcher.matches()) { //test whether the class has modifier
            matchScore += 50;
        }

        return matchScore;
    }

    public String toPattern(String key) {
        return ".*(" + key.toLowerCase() + ").*";
    }



    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public String getDefinition() {
        return this.definition;
    }


    @Override
    public int compareTo(HpoClassFound other) {
        if (other != null) {
            return this.score - other.score;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(this.score);
        String[] id_string = this.id.split("/");
        builder.append("\t\t" + id_string[id_string.length - 1]);
        builder.append("\t\t" + this.label);
        if(this.definition != null){
            builder.append("\t\t" + this.definition);
        } else {
            builder.append("\t\t");
        }
        return builder.toString();
    }
}
