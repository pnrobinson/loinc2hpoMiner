package org.monarchinitiative.loinc2hpo.sparql;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class SparqlQuery {

    private static final Logger logger = LogManager.getLogger();
    public static boolean modelCreated = false; //check whether the model for
    // hpo has been created


    public static final String modifier = "increase.*|decrease.*|elevat.*|reduc.*|high.*|low.*|above|below|abnormal.*";





    /**
     * Build a standard sparql query from a single key. It searches for HPO classes that
     * have the key in class label or definition, AND the class should have a modifier
     * (increase/decrease/abnormal etc) in class label or definition.
     * @param single_word_key
     * @return a sparql query string to select HPO classes that
     */
    public static String buildStandardQueryWithSingleKey(String single_word_key) {

        StringBuilder standardQuery = new StringBuilder("NOT IMPLEMENTED");

        return standardQuery.toString();
    }

    /**
     * Build a loose sparql query from a single key. It simply searches for HPO classes that
     * have the key in class label or definition, WITHOUT the requirement for modifiers
     * (decrease/increase/abnormal etc)
     * @param single_word_key
     * @return a sparql query string to select HPO classes that
     */
    public static String buildLooseQueryWithSingleKey(String single_word_key) {

        StringBuilder looseQuery = new StringBuilder();

        String condition = String.format(" WHERE {" +
                "{?phenotype obo:IAO_0000115 ?definition . " +
                " ?phenotype rdfs:label ?label . " +
                " FILTER (regex(?definition, \"%s\", \"i\")) " +
                " } " +
                "UNION" +
                " {?phenotype rdfs:label ?label . " +
                " OPTIONAL {?phenotype obo:IAO_0000115 ?definition .} " +
                " FILTER (regex(?label, \"%s\", \"i\")) " +
                " }" +
                "}", single_word_key, single_word_key);
        looseQuery.append(condition);
        return looseQuery.toString();

    }

    /**
     * Build a standard Sparql query from a list of keys. It searches for HPO classes that have
     * all the keys in class label or definition, AND the classes should have modifiers (increase/
     * decrease/abnormal etc) in class label or definition.
     * @param keys
     * @return
     */
    public static String buildStandardQueryWithMultiKeys(List<String> keys) { //provide multiple keywords for query
        if (keys == null) {
            throw new IllegalArgumentException("Key list is empty");
        }
        StringBuilder multiKeyQuery = new StringBuilder();

        StringBuilder labelfilters = new StringBuilder();
        StringBuilder definitionfilters = new StringBuilder();
        for (String key : keys) {
            labelfilters.append(String.format("FILTER (regex(?label, \"%s\", \"i\")) ", key));
            //if a HPO class has a definition, then filter keys in concat(label, definition)
            //no need to ask all the keys appear in definition!
            definitionfilters.append(String.format("FILTER (regex(concat(?label, \" \", ?definition), \"%s\", \"i\")) ", key));
        }
        String condition = String.format(" WHERE " +
                "{" +
                " {?phenotype obo:IAO_0000115 ?definition . " +
                " ?phenotype rdfs:label ?label . " +
                " FILTER (regex(concat(?label, \" \", ?definition), \"%s\", \"i\")) " +
                " %s } " +
                "UNION" +
                " {?phenotype rdfs:label ?label . " +
                " OPTIONAL {?phenotype obo:IAO_0000115 ?definition .} " +
                " FILTER (regex(?label, \"%s\", \"i\"))" +
                " %s }" +
                "}", modifier, definitionfilters.toString(), modifier, labelfilters.toString());
        multiKeyQuery.append(condition);
        return multiKeyQuery.toString();
    }

    /**
     * Build a loose Sparql query from a list of keys. It searches for HPO classes that have all the keys
     * in class label or definition, without the requirement that the classes should have modifiers
     * (increase/decrease/abnormal etc) in class label or modifier.
     * @param keys
     * @return
     */
    public static String buildLooseQueryWithMultiKeys(List<String> keys) { //provide multiple keywords for query
        if (keys == null) {
            throw new IllegalArgumentException("Key list is empty");
        }
        StringBuilder multiKeyQuery = new StringBuilder();

        StringBuilder labelfilters = new StringBuilder();
        StringBuilder definitionfilters = new StringBuilder();
        for (String key : keys) {
            labelfilters.append(String.format("FILTER (regex(?label, \"%s\", \"i\")) ", key));
            definitionfilters.append(String.format("FILTER (regex(concat(?label, \" \", ?definition), \"%s\", \"i\")) ", key));
        }
        String condition = String.format(" WHERE " +
                "{" +
                " {?phenotype obo:IAO_0000115 ?definition . " +
                " ?phenotype rdfs:label ?label . " +
                " %s } " +
                "UNION" +
                " {?phenotype rdfs:label ?label . " +
                " OPTIONAL {?phenotype obo:IAO_0000115 ?definition .} " +
                " %s }" +
                "}", definitionfilters.toString(), labelfilters.toString());
        multiKeyQuery.append(condition);
        return multiKeyQuery.toString();
    }



    /**
     * A method to do manual query with provided keys (literally)
     */
    public static List<HPO_Class_Found> query_manual(List<String> keys,
                                                     LoincLongNameComponents loincLongNameComponents) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            String looseQueryString = buildLooseQueryWithMultiKeys(keys);
            return List.of();
        }
    }

    /** @TODO: use recursive call to refactor it
     * A method to automatically query the HPO
     * @param loincLongCommonName
     * @return
     */
    public static List<HPO_Class_Found> query_auto(String loincLongCommonName) {


        List<HPO_Class_Found> HPO_classes_found = new ArrayList<>();

        LoincLongNameComponents loincClass = LoincLongNameParser.parse(loincLongCommonName);
        Queue<String> keys_in_parameter = loincClass.keysInLoincParameter();
        Queue<String> keys_in_tissue = loincClass.keysInLoincTissue();
        Stack<String> keys_in_use = new Stack<>();


        //first loop through keys in loinc parameter, until it finds a good key that yields > 0 class
        Queue<String> keys_in_parameter_copy1 = new LinkedList<>(keys_in_parameter);
        while (!keys_in_parameter_copy1.isEmpty()) {
            logger.info("Enter loop1");
            String key = keys_in_parameter_copy1.remove();
            keys_in_use.clear();
            keys_in_use.push(key);
            logger.info("new key used for query: " + key);
            String standardQueryString = buildStandardQueryWithSingleKey(key);
            if (HPO_classes_found.size() != 0) break;
        }

        //loop through remaining keys in loinc parameter, until it reduces results to < 20
        boolean forceToContinue = false;
        while ((HPO_classes_found.size() > 20 || forceToContinue) && !keys_in_parameter_copy1.isEmpty()) {
            logger.info("Enter loop2");
            forceToContinue = false;
            String key = keys_in_parameter_copy1.remove();
            keys_in_use.add(key);
            logger.info("query with " + keys_in_use.size() + " keys");
            logger.info("new key used for query: " + key);
            String stardardQueryString = buildStandardQueryWithMultiKeys(keys_in_use);
            HPO_classes_found = List.of();
            if (HPO_classes_found.size() == 0) {
                if (!keys_in_parameter_copy1.isEmpty()) {//if adding a key suddenly fails the query, remove the last key and continue
                    keys_in_use.pop();
                    forceToContinue = true;
                    logger.info("A bad key is detected and poped" + key);
                } else { //if cannot continue, go back one step
                    keys_in_use.pop();
                    logger.info("Going back one step");
                    HPO_classes_found = List.of();
                }
            }
        }

        if (HPO_classes_found.size() > 20 && !keys_in_tissue.isEmpty()) { //use tissue to search
            logger.info("start to use tissue for query");
            keys_in_use.add(new Synset().getSynset((List<String>)keys_in_tissue).convertToRe());
            String standardQueryString = buildStandardQueryWithMultiKeys(keys_in_use);
            logger.info("query with " + keys_in_use.size() + " keys");
            for (String key : keys_in_use) {
                logger.info(key);
            }
            logger.info("query string: \n" + standardQueryString);

            HPO_classes_found = List.of();
            if (HPO_classes_found.size() == 0) {
                keys_in_use.pop();
                HPO_classes_found = List.of();
            }
        }  //if there are still more than 20 classes, then we can do nothing


        //if no HPO classes are found using standard query, then lower threshold (remove modifier)
        if (HPO_classes_found.size() == 0) {

            Queue<String> keys_in_parameter_copy2 = new LinkedList<>(keys_in_parameter);
            while (!keys_in_parameter_copy2.isEmpty()) {
                logger.info("enter loop to find the first key that can HPO classes with loose method. ");
                String key = keys_in_parameter_copy2.remove();
                keys_in_use.clear();
                keys_in_use.push(key);
                logger.info("key used: " + key);
                String standardQueryString = buildLooseQueryWithSingleKey(key);
                HPO_classes_found = List.of();
                if (HPO_classes_found.size() != 0) break;
            }

            //if a single key finds too many classes, add additional keys to reduce the number
            forceToContinue = false;
            while ((HPO_classes_found.size() > 20 || forceToContinue) && !keys_in_parameter_copy2.isEmpty()) {
                logger.info("Enter loop to use additional keys to reduce the number of classes.");
                forceToContinue = false;
                String key = keys_in_parameter_copy2.remove();
                keys_in_use.add(key);
                logger.info("query with " + keys_in_use.size() + " keys");
                logger.info("new key used for query: " + key);
                String looseQueryString = buildLooseQueryWithMultiKeys(keys_in_use);

                HPO_classes_found = List.of();
                if (HPO_classes_found.size() == 0) {
                    if (!keys_in_parameter_copy2.isEmpty()) {//if adding a key suddenly fails the query, remove the last key and continue
                        keys_in_use.pop();
                        forceToContinue = true;
                        logger.info("A bad key is detected and poped: " + key);
                    } else { //if cannot continue, go back one step
                        keys_in_use.pop();
                        logger.info("Going back one step");
                        HPO_classes_found = List.of();
                    }
                }
            }

            //if there are still too many classes, use tissue for search
            if (HPO_classes_found.size() > 20 && !keys_in_tissue.isEmpty()) { //use tissue to search
                logger.info("start to use tissue for query");
                keys_in_use.add(new Synset().getSynset((List<String>)keys_in_tissue).convertToRe());
                String looseQueryString = buildLooseQueryWithMultiKeys(keys_in_use);
                logger.info("query with " + keys_in_use.size() + " keys");
                for (String key : keys_in_use) {
                    logger.info(key);
                }
                logger.info("query string: \n" + looseQueryString);

                HPO_classes_found = List.of();
                if (HPO_classes_found.size() == 0) { //if adding tissue reduces HPO classes to 0, then go back
                    keys_in_use.pop();
                    HPO_classes_found = List.of();
                }
            }  //if there are still more than 20 classes, then we can do nothing
        }


        //if no HPO classes can be found after looping through all keys in loinc parameter, then
        //none of the keys are good; such HPO classes either do not exist, or the user should try
        //some synomes.
        if (HPO_classes_found.size() == 0) {
            logger.info("NO HPO terms are found. Try some synonymes.");
        }
        logger.info(HPO_classes_found.size() + " HPO classes are found!");

        return HPO_classes_found;
    }




}
