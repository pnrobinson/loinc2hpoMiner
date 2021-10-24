package org.monarchinitiative.loinc2hpo;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.monarchinitiative.loinc2hpo.model.Loinc2HpoAnnotationModel;
import org.springframework.stereotype.Component;

import java.io.File;


@Component
public class Loinc2HpoAnnotationsTabController {
    private static final Logger logger = LogManager.getLogger();
    /** Reference to the second tab. When the user adds a new annotation, we update the table, therefore, we need a reference. */

   // private AnnotateTabController annotateTabController;

    private Loinc2HpoMainController mainController;

    //private AppResources appResources;

    /** This is the message users will see if they open the analysis tab before they have entered the genes
     * and started the analysis of the viewpoints. */
    private static final String INITIAL_HTML_CONTENT = "<h3>LOINC2HPO Biocuration App</h3>";

   // private WebEngine contentWebEngine;





    //@FXML private WebView wview;



    public void initializeTable() {
        /*
logger.trace("Loinc2HpoAnnotationsTabController initialize() called");
       //ap<TermId, Term> termMap = appResources.getTermidTermMap();
        loincAnnotationTableView.setEditable(false);
        loincNumberColumn.setSortable(true);
        loincNumberColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincId().toString()));
        loincScaleColumn.setSortable(true);
        loincScaleColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincScale().toString()));
        belowNormalHpoColumn.setSortable(true);
        //belowNormalHpoColumn.setCellValueFactory(cdf -> cdf.getValue().whenValueLow() == null ? new ReadOnlyStringWrapper("\" \"") : new ReadOnlyStringWrapper(termMap.get(cdf.getValue().whenValueLow()).getName()));
        belowNormalHpoColumn.setCellValueFactory(cdf -> {
            TermId termId = cdf.getValue().whenValueLow();
            if (termId == null) { //no annotation for low
                return new ReadOnlyStringWrapper("\" \"");
            } else if (!termMap.containsKey(termId)) { //annotation termid not found in current hpo
                return new ReadOnlyStringWrapper(termId.getValue());
            } else { //show term name
                return new ReadOnlyStringWrapper(termMap.get(termId).getName());
            }
        });
        notAbnormalHpoColumn.setSortable(true);
        //notAbnormalHpoColumn.setCellValueFactory(cdf -> cdf.getValue().whenValueNormalOrNegative() == null ? new ReadOnlyStringWrapper("\" \"")
        //        : new ReadOnlyStringWrapper(cdf.getValue().whenValueNormalOrNegative().getName()));
        notAbnormalHpoColumn.setCellValueFactory(cdf -> {
            TermId termId = cdf.getValue().whenValueNormalOrNegative();
            if (termId == null) { //no annotation
                return new ReadOnlyStringWrapper("\" \"");
            } else if (!termMap.containsKey(termId)){//previously annotated with a term not found in current hpo
                    return new ReadOnlyStringWrapper(termId.getValue());
            } else { //annotated with a term present in current hpo
                    return new ReadOnlyStringWrapper(termMap.get(termId).getName());
            }
        });
        aboveNormalHpoColumn.setSortable(true);
//        aboveNormalHpoColumn.setCellValueFactory(cdf -> cdf.getValue().whenValueHighOrPositive() == null ? new ReadOnlyStringWrapper("\" \"")
//        : new ReadOnlyStringWrapper(cdf.getValue().whenValueHighOrPositive().getName()));
        aboveNormalHpoColumn.setCellValueFactory(cdf -> {
            TermId termId = cdf.getValue().whenValueHighOrPositive();
            if (termId == null) { //no annotation
                return new ReadOnlyStringWrapper("\" \"");
            } else if (!termMap.containsKey(termId)){//previously annotated with a term not found in current hpo
                return new ReadOnlyStringWrapper(termId.getValue());
            } else { //annotated with a term present in current hpo
                return new ReadOnlyStringWrapper(termMap.get(termId).getName());
            }
        });
        loincFlagColumn.setSortable(true);
        loincFlagColumn.setCellValueFactory(cdf -> cdf.getValue() != null && cdf.getValue().getFlag() ?
                new ReadOnlyStringWrapper("Y") : new ReadOnlyStringWrapper(""));
        noteColumn.setSortable(true);
        noteColumn.setCellValueFactory(cdf -> cdf.getValue() == null ? new ReadOnlyStringWrapper("") :
                new ReadOnlyStringWrapper(cdf.getValue().getNote()));
        updateSummary();

         */

    }








    protected void saveAnnotations() {
        //@TODO: implement saving if necessary
        throw new UnsupportedOperationException();
    }


    protected void newAppend() {
        //@TODO: implement if necessary
        throw new UnsupportedOperationException();
    }

    protected void saveAnnotationsAs() {

        String path = null;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose LOINC Core Table file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TSV files (*.txt)", "*.tsv"));
        File f = chooser.showSaveDialog(null);
        if (f != null) {
            path = f.getAbsolutePath();
            logger.trace("Save annotation data to new file: {}",path);
        } else {
            logger.error("Unable to obtain path to a new file to save " +
                    "annotation data to");
            return;

        }

        //@TODO: implement if necessary
        throw new UnsupportedOperationException();

    }

    protected void clear() {
       // appResources.getLoincAnnotationMap().clear();
       // loincAnnotationTableView.getItems().clear();
    }

}
