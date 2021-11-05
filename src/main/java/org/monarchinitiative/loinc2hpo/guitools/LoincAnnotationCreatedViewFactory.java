package org.monarchinitiative.loinc2hpo.guitools;

import org.monarchinitiative.loinc2hpocore.annotation.LoincAnnotation;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoincAnnotationCreatedViewFactory extends AbstractWebviewFactory {

    private final LoincAnnotation annotation;
    private final Ontology ontology;


    public LoincAnnotationCreatedViewFactory(Ontology ontology, LoincAnnotation annotation) {
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
        sb.append("<table style=\"color:blue;font-size:10px;\">").append("<tr><th>LOINC id</th>" +
                "<th>scale</th><th>TermId</th><th>Label</th><th>Outcome</th><th>Created by</th>");
       for (var annot : annotation.allAnnotations()) {
            TermId hpoTermId = annot.getHpoTermId();
            Optional<String> opt = ontology.getTermLabel(hpoTermId);
            String label = opt.orElse("n/a");
            List<String> fields = new ArrayList<>();
            fields.add(annot.getLoincId().toString());
            fields.add(annot.getLoincScale().shortName());
            fields.add(hpoTermId.getValue());
            fields.add(label);
            fields.add(annot.getOutcome().toString());
            fields.add(annot.getBiocuration());
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

    public boolean openDialogWithBoolean() {
        String windowTitle = "LOINC2HPO New Annotation";
        String html = getHTML();
        return openDialogWithBoolean(windowTitle, html);
    }

}
