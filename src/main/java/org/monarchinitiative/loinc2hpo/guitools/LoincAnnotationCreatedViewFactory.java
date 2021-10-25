package org.monarchinitiative.loinc2hpo.guitools;

import org.monarchinitiative.loinc2hpo.model.Loinc2HpoAnnotationModel;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincId;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoincAnnotationCreatedViewFactory extends AbstractWebviewFactory {

    private final LoincId loincCode;
    private final Loinc2HpoAnnotationModel annotation;


    public LoincAnnotationCreatedViewFactory(LoincId loincCode, Loinc2HpoAnnotationModel annotation) {
        this.loincCode = loincCode;
        this.annotation = annotation;
    }
    @Override
    String getHTML() {
        String sb = "<html><body>\n" +
                inlineCSS() +
                "<h1>Created Loinc2Hpo annotation</h1>" +
                getLoincAnnotationHtml() +
                "</body></html>";
        return sb;
    }



    private String getLoincAnnotationHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>").append(loincCode).append("</p>");
        sb.append("<table>").append("<tr><th>LOINC id</th><th>scale</th><th>Created by</th>");

        List<String> fields = new ArrayList<>();
        fields.add(annotation.getLoincId().toString());
        fields.add(annotation.getLoincScale().toString());
        fields.add(annotation.getCreatedBy());
        fields = fields.stream().map( f -> String.format("<td>%s</td>", f)).collect(Collectors.toList());
        sb.append("<tr>").append(String.join(" ", fields)).append("</tr>\n");
        sb.append("</table>\n");
        return sb.toString();
    }

    public void openDialog() {
        String windowTitle = "LOINC2HPO New Annotation";
        String html = getHTML();
        openDialog(windowTitle, html);
    }

}
