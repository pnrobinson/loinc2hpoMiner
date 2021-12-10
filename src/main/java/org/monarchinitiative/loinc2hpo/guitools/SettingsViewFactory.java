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
        return "<html><body>\n" +
                inlineCSS() +
                "<h1>LOINC2HPO Biocuration Settings</h1>" +
                settingsList() +
                "</body></html>";
    }

    private String settingsList() {
        return "<p>Settings:</p><ul>\n" +
                String.format("<li>Path to LOINC Core Table: %s</li>", settings.getLoincCoreTablePath()) +
                String.format("<li>Path to <tt>hp.json</tt> file: %s</li>", settings.getHpoJsonPath()) +
                String.format("<li>Path to annotation file: %s</li>", settings.getAnnotationFile()) +
                String.format("<li>Biocurator ID: %s</li>", settings.getBiocuratorID()) +
                "</ul>\n";
    }

    public void openDialog() {
        String windowTitle = "LOINC2HPO Biocuration App Settings";
        String html = getHTML();
        openDialog(windowTitle, html);
    }

    public boolean openDialogWithBoolean() {
        openDialog(); // dont need this here
        return true;
    }

}
