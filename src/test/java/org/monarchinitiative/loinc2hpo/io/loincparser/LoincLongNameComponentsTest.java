package org.monarchinitiative.loinc2hpo.io.loincparser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoincLongNameComponentsTest {


    @Test
    public void testBasics() {
        String parameter = "myParameter";
        String tissue = "myTissue";
        String assayMethod = "myMethod";
        String assayType = "myType";
        LoincLongNameComponents components = new LoincLongNameComponents(parameter, tissue, assayMethod, assayType);
        assertEquals(parameter, components.getLoincParameter());
        assertEquals(tissue, components.getLoincTissue());
        assertEquals(assayMethod, components.getLoincMethod());
        assertEquals(assayType, components.getLoincType());
    }
}
