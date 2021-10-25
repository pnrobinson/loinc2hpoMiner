package org.monarchinitiative.loinc2hpo.guitools;

import org.monarchinitiative.loinc2hpo.model.Loinc2HpoAnnotationModel;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoincAnnotationCreatedViewFactory extends AbstractWebviewFactory {

    private final LoincId loincCode;
    private final Loinc2HpoAnnotationModel annotation;
    private final Ontology ontology;


    public LoincAnnotationCreatedViewFactory(Ontology ontology, Loinc2HpoAnnotationModel annotation) {
        this.loincCode = annotation.getLoincId();
        this.annotation = annotation;
        this.ontology = ontology;
    }
    @Override
    String getHTML() {
        return "<html><body>\n" +
                inlineCSS() +
                "<h1>Created Loinc2Hpo annotation</h1>" +
                getLoincAnnotationHtml() +
                "</body></html>";
    }



    private String getLoincAnnotationHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>").append("LOINC2HPO annotations").append("</p>");
        sb.append("<table style=\"color:blue;font-size:10px;\">").append("<tr><th>LOINC id</th><th>scale</th><th>TermId</th><th>Label</th><th>Code</th><th>Created by</th>");
        for (var e : annotation.getCandidateHpoTerms().entrySet()) {
            var k = e.getKey();
            var v = e.getValue();
            TermId tid = v.getId();
            Optional<String> opt = ontology.getTermLabel(tid);
            String label = opt.orElse("n/a");
            List<String> fields = new ArrayList<>();
            fields.add(annotation.getLoincId().toString());
            fields.add(annotation.getLoincScale().toString());
            fields.add(tid.getValue());
            fields.add(label);
            fields.add(k.getDisplay());
            fields.add(annotation.getCreatedBy());
            fields = fields.stream().map( f -> String.format("<td>%s</td>", f)).collect(Collectors.toList());
            sb.append("<tr>").append(String.join(" ", fields)).append("</tr>\n");
        }

        sb.append("</table>\n");
        return sb.toString();
    }

    public void openDialog() {
        String windowTitle = "LOINC2HPO New Annotation";
        String html = getHTML();
        openDialog(windowTitle, html);
    }

}
