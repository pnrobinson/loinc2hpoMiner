package org.monarchinitiative.loinc2hpo.io.loincparser;


import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Let users provide a file containing loinc codes for annotation
 * Expected format:
 * 6457-6
 * 802-9
 * 35707-9
 * 33460-7
 * (...)
 */
public class UserSuppliedLoincIdParser {
    private static final Logger logger = LoggerFactory.getLogger(UserSuppliedLoincIdParser.class);
    private final Set<LoincId> loincIdList;


    public UserSuppliedLoincIdParser(File userSuppliedLoincFile)  {
        loincIdList = ingestLoincIds(userSuppliedLoincFile);
    }

    private Set<LoincId> ingestLoincIds(File userSuppliedLoincFile)  {
        Set<LoincId> loincSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(userSuppliedLoincFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    loincSet.add(new LoincId(line.trim()));
                } catch (Loinc2HpoRunTimeException e ) {
                    logger.error("Malformed user-supplied LOINC id: \"" + line + "\"");
                }
            }
        } catch (IOException e) {
            logger.error("cannot read in a loinc line");
        }
        return loincSet;
    }

    public Set<LoincId> getLoincIdSet() {
        return loincIdList;
    }
}
