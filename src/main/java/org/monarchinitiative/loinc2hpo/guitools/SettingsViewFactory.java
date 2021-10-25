package org.monarchinitiative.loinc2hpo.guitools;

import org.monarchinitiative.loinc2hpo.model.Settings;

/**
 * A helper class that displays the settings for this project
 * @author Peter Robinson
 * @version 0.1.3 (2017-11-12)
 */
public class SettingsViewFactory extends AbstractWebviewFactory {


    private final Settings settings;

    public SettingsViewFactory(Settings settings) {
        this.settings = settings;
    }

    @Override
    String getHTML() {
        String sb = "<html><body>\n" +
                inlineCSS() +
                "<h1>LOINC2HPO Biocuration App Setting</h1>" +
                settingsList() +
                "</body></html>";
        return sb;
    }

    private String settingsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>Settings:</p><ul>\n");
        sb.append(String.format("<li>Path to LOINC Core Table: %s</li>",settings.getLoincCoreTablePath()));
        sb.append(String.format("<li>Path to <tt>hp.json</tt> file: %s</li>",settings.getHpoJsonPath()));
        sb.append(String.format("<li>Path to annotation file: %s</li>",settings.getAnnotationFile()));
        sb.append(String.format("<li>Biocurator ID: %s</li>",settings.getBiocuratorID()));
        sb.append("</ul>\n");
        return sb.toString();
    }

    public void openDialog() {
        String windowTitle = "LOINC2HPO Biocuration App Settings";
        String html = getHTML();
        openDialog(windowTitle, html);
    }

}
