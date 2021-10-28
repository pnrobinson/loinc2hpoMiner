package org.monarchinitiative.loinc2hpo.model;


import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.loinc2hpo.model.codesystems.InternalCode;
import org.monarchinitiative.loinc2hpo.model.codesystems.OutcomeCode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Loinc2HpoAnnotationCsvEntry {

    private String loincId;
    private String loincScale;
    private String system;
    private final OutcomeCode code;
    private final String hpoTermId;
    private final String isNegated;
    private final String createdOn;
    private final String createdBy;
    private final String lastEditedOn;
    private final String lastEditedBy;
    private String version;
    private final String isFinalized;
    private final String comment;


    public Loinc2HpoAnnotationCsvEntry(String loincId, String loincScale, String system, OutcomeCode code, String hpoTermId, String isNegated, String createdOn, String createdBy, String lastEditedOn, String lastEditedBy, String version, String isFinalized, String comment) {
        this.loincId = loincId;
        this.loincScale = loincScale;
        this.system = system;
        this.code = code;
        this.hpoTermId = hpoTermId;
        this.isNegated = isNegated;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.lastEditedOn = lastEditedOn;
        this.lastEditedBy = lastEditedBy;
        this.version = version;
        this.isFinalized = isFinalized;
        this.comment = comment;
    }


    public static List<Loinc2HpoAnnotationCsvEntry> importAnnotations(String path) {
        List<Loinc2HpoAnnotationCsvEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))){
            //read header
            String line = reader.readLine();
            if (!line.equals("loincId\tloincScale\tsystem\tcode\thpoTermId\tisNegated\tcreatedOn\tcreatedBy\tlastEditedOn\tlastEditedBy\tversion\tisFinalized\tcomment")){
                throw new RuntimeException("header does not match expected!");
            }

            while ((line = reader.readLine()) != null){
                String[] elements = line.split("\t");
                if (elements.length != 13){
                    throw new RuntimeException("Line does not have expected length: " + line);
                }
                for (int i = 0; i < elements.length; i++){
                    elements[i] = elements[i].equals("NA")? null : elements[i];
                }
                String loincId = elements[0];
                String loincScale = elements[1];
                String system = elements[2];
                OutcomeCode code = InternalCode.fromString(elements[3]);
                String hpoTermId = elements[4];
                String isNegated = elements[5];
                String createdOn = elements[6];
                String createdBy = elements[7];
                String lastEditedOn = elements[8];
                String lastEditedBy = elements[9];
                String version = elements[10];
                String isFinalized = elements[11];
                String comment = elements[12];
                Loinc2HpoAnnotationCsvEntry newEntry = new Builder()
                        .withLoincId(loincId)
                        .withLoincScale(loincScale)
                        .withSystem(system)
                        .withCode(code)
                        .withHpoTermId(hpoTermId)
                        .withIsNegated(isNegated)
                        .withCreatedOn(createdOn)
                        .withCreatedBy(createdBy)
                        .withLastEditedOn(lastEditedOn)
                        .withLastEditedBy(lastEditedBy)
                        .withVersion(version)
                        .withIsFinalized(isFinalized)
                        .withComment(comment)
                        .build();
                entries.add(newEntry);
            }
        } catch (IOException e) {
            throw new Loinc2HpoRunTimeException(e.getMessage());
        }
        return entries;
    }


    public String getLoincId() {
        return loincId;
    }

    public String getLoincScale() {
        return loincScale;
    }

    public String getSystem() {
        return system;
    }

    public OutcomeCode getCode() {
        return code;
    }

    public String getHpoTermId() {
        return hpoTermId;
    }

    public String getIsNegated() {
        return isNegated;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastEditedOn() {
        return lastEditedOn;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIsFinalized() {
        return isFinalized;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString(){
        List<String> fields = Arrays.asList(loincId, loincScale, system, code.toString()
                , hpoTermId, isNegated, createdOn, createdBy, lastEditedOn,
                lastEditedBy, version, isFinalized, comment);
        //replace any null value or empty value with "NA"
        List<String> replaceNullWithNA =
                fields.stream().map(f -> f == null || f.equals("")?
                "NA" : f).collect(Collectors.toList());
        return String.join("\t", replaceNullWithNA);
    }


    public static final class Builder {
        private String loincId;
        private String loincScale;
        private String system="Loinc2Hpo";
        private OutcomeCode code;
        private String hpoTermId;
        private String isNegated;
        private String createdOn;
        private String createdBy;
        private String lastEditedOn;
        private String lastEditedBy;
        private String version;
        private String isFinalized;
        private String comment;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withLoincId(String loincId) {
            this.loincId = loincId;
            return this;
        }

        public Builder withLoincScale(String loincScale) {
            this.loincScale = loincScale;
            return this;
        }

        public Builder withSystem(String system) {
            this.system = system;
            return this;
        }

        public Builder withCode(OutcomeCode code) {
            this.code = code;
            return this;
        }

        public Builder withHpoTermId(String hpoTermId) {
            this.hpoTermId = hpoTermId;
            return this;
        }

        public Builder withIsNegated(String isNegated) {
            this.isNegated = isNegated;
            return this;
        }

        public Builder withCreatedOn(String createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withLastEditedOn(String lastEditedOn) {
            this.lastEditedOn = lastEditedOn;
            return this;
        }

        public Builder withLastEditedBy(String lastEditedBy) {
            this.lastEditedBy = lastEditedBy;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withIsFinalized(String isFinalized) {
            this.isFinalized = isFinalized;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Loinc2HpoAnnotationCsvEntry build() {
            return new Loinc2HpoAnnotationCsvEntry(loincId, loincScale, system, code, hpoTermId, isNegated, createdOn, createdBy, lastEditedOn, lastEditedBy, version, isFinalized, comment);
        }
    }
}
