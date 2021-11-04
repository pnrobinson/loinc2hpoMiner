package org.monarchinitiative.loinc2hpo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;
import org.monarchinitiative.loinc2hpo.guitools.*;
import org.monarchinitiative.loinc2hpo.io.HpoMenuDownloader;
import org.monarchinitiative.loinc2hpo.io.loincparser.*;
import org.monarchinitiative.loinc2hpo.model.*;
import org.monarchinitiative.loinc2hpocore.annotation.*;
import org.monarchinitiative.loinc2hpocore.codesystems.Outcome;
import org.monarchinitiative.loinc2hpocore.io.Loinc2HpoAnnotationParser;
import org.monarchinitiative.loinc2hpocore.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.loinc2hpocore.loinc.LoincLongName;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;
import static org.monarchinitiative.loinc2hpo.guitools.PopUps.getStringFromUser;

@Component
public class Loinc2HpoMainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(Loinc2HpoMainController.class);
    private final static String MISSINGVALUE = "n/a";

    private final ExecutorService executor;

    private final OptionalResources optionalResources;

    private final Properties pgProperties;


    private Map<LoincId, LoincEntry> loincmap = null;

    private final ObservableMap<LoincId, Loinc2HpoAnnotation> annotationsMap = FXCollections.observableHashMap();

    private final CurrentAnnotationModel currentAnnotationModel;

    @FXML
    public Button nominalAnnotationButton;
    @FXML
    private Button initLOINCtableButton;
    @FXML
    private Button userSuppliedLoincCodes;
    @FXML
    private Button searchForLOINCIdButton;
    @FXML
    private Button createAnnotationButton;
    @FXML
    private TextField loincSearchTextField;
    @FXML
    private Button filterLoincTableByList;
    @FXML
    private TextField userInputForManualQuery;

    @FXML
    private ListView<HpoClassFound> hpoListView;
    private ObservableList<HpoClassFound> hpoQueryResult = FXCollections.observableArrayList();


    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane loincTableTitledpane;
    @FXML
    private TableView<LoincEntry> loincTableView;
    @FXML
    private TableColumn<LoincEntry, String> loincIdTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> componentTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> propertyTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> timeAspectTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> methodTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> scaleTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> systemTableColumn;
    @FXML
    private TableColumn<LoincEntry, String> nameTableColumn;

    @FXML
    private TextArea annotationNoteField;
    @FXML
    private Button clearButton;
    @FXML
    private Button allAnnotationsButton;


    @FXML
    private Button hpoChildTermsButton;
    @FXML
    private ContextMenu contextMenu;

    @FXML
    private Button searchHpoByLoincButton;


    @FXML
    private ContextMenu loincTableContextMenu;
    @FXML
    private Menu loincListsButton;
    @FXML
    private MenuItem backMenuItem;
    @FXML
    private MenuItem forwardMenuItem;
    @FXML
    private Menu groupUngroup2LoincListButton;
    @FXML
    private Menu exportLoincListButton;
    @FXML
    private Menu importLoincGroupButton;

    @FXML
    private VBox vbox4wv;

    @FXML
    private TableView<HpoAnnotationRow> hpoAnnotationTable;
    @FXML
    private TableColumn<HpoAnnotationRow, String> hpoAnnotationType;
    @FXML
    private TableColumn<HpoAnnotationRow, String> hpoAnnotationTid;
    @FXML
    private TableColumn<HpoAnnotationRow, String> hpoAnnotationTermLabel;


    @FXML
    private TableView<Loinc2HpoAnnotation> loincAnnotationTableView;
    @FXML
    private TableColumn<Loinc2HpoAnnotation, String> loincNumberColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotation, String> testResultColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotation, String> annotatedHpoColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotation, String> loincScaleColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotation, String> commentColumn;

    private BooleanProperty isPresentOrd = new SimpleBooleanProperty(false);
    private BooleanProperty configurationComplete = new SimpleBooleanProperty(false);
    /**
     * The allows us to get info from the pom.xml file
     * buildProperties.getName(), buildProperties.getVersion(), etc.
     */
    @Autowired
    BuildProperties buildProperties;

    @Autowired
    Settings settings;


    @Autowired
    public Loinc2HpoMainController(OptionalResources optionalResources,
                                   ExecutorService executorService,
                                   Properties pgProperties,
                                   @Qualifier("appHomeDir") File appHomeDir) {
        this.optionalResources = optionalResources;
        this.executor = executorService;
        this.pgProperties = pgProperties;
        this.currentAnnotationModel = new CurrentAnnotationModel();
    }

    @FXML
    private void initialize() {
        LOGGER.trace("initialize() called");
        File settingsFile = Settings.getPathToSettingsFileAndEnsurePathExists();
        try {
            Settings.loadSettings(settings, settingsFile.getPath());
        } catch (IOException e) {
            LOGGER.trace("okay, this is the first time you use it. Configure the settings now");
        }
        optionalResources.addSettings(settings);
        // run the initialization task on a separate thread
        StartupTask task = new StartupTask(optionalResources, pgProperties);
        this.executor.submit(task);
        userSuppliedLoincCodes.setTooltip(new Tooltip("Filter Loinc by providing a Loinc list in txt file"));
        clearButton.setTooltip(new Tooltip("Clear all textfields"));
        allAnnotationsButton.setTooltip(new Tooltip("Display annotations for currently selected Loinc code"));
        initLOINCtableButton.setTooltip(new Tooltip("Ingest Loinc Core Table and HP (Download the files first)."));
        searchForLOINCIdButton.setTooltip(new Tooltip("Search Loinc with a Loinc code or name"));
        searchHpoByLoincButton.setTooltip(new Tooltip("Find candidate HPO terms"));

        hpoListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<HpoClassFound> call(ListView<HpoClassFound> param) {
                return new ListCell<>() {
                    @Override
                    public void updateItem(HpoClassFound hpo, boolean empty) {
                        super.updateItem(hpo, empty);
                        if (hpo != null) {
                            setText(hpo.toString());
                            Tooltip tooltip = new Tooltip(hpo.getDefinition());
                            tooltip.setPrefWidth(300);
                            tooltip.setWrapText(true);
                            setTooltip(tooltip);
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        //track what is selected in the loincTable. If currently selected LOINC is a Ord type with a Presence/Absence outcome, change the listener isPresentOrd to true; otherwise false.
        loincTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                isPresentOrd.setValue(newValue.isPresentOrd());
            }
        });


        initTableStructure();
        initializeHpoAnnotationTable();
    }

    private void initializeHpoAnnotationTable() {
        this.hpoAnnotationTable.setEditable(false);
        hpoAnnotationTid.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getHpoTermId().getValue()));
        hpoAnnotationType.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincType()));
        hpoAnnotationTermLabel.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getHpoTermLabel()));
        hpoAnnotationTid.setSortable(false);
        hpoAnnotationType.setSortable(false);
        hpoAnnotationTermLabel.setSortable(false);
    }

    public void initializeLoinc2HpoAnnotationTable() {
        LOGGER.trace("initializeLoinc2HpoAnnotationTable called");
        Ontology hpo = optionalResources.getOntology();
        if (hpo == null) {
            PopUps.showWarningDialog("ERROR", "Ontology not initialized",
                    "Cannot show LOINC2HPO annotations before HPO is initialized");
            return;
        }
        Map<TermId, Term> termMap = hpo.getTermMap();
        loincAnnotationTableView.setEditable(false);
        loincNumberColumn.setSortable(true);
        loincNumberColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincId().toString()));
        loincScaleColumn.setSortable(true);
        loincScaleColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincScale().toString()));
        testResultColumn.setSortable(true);
        //belowNormalHpoColumn.setCellValueFactory(cdf -> cdf.getValue().whenValueLow() == null ? new ReadOnlyStringWrapper("\" \"") : new ReadOnlyStringWrapper(termMap.get(cdf.getValue().whenValueLow()).getName()));
        testResultColumn.setCellValueFactory(cdf -> {
            Outcome testOutcome = cdf.getValue().getOutcome();
            return new ReadOnlyStringWrapper(testOutcome.toString());
        });
        annotatedHpoColumn.setSortable(true);
        annotatedHpoColumn.setCellValueFactory(cdf -> {
            TermId termId = cdf.getValue().getHpoTermId();
            if (termId == null) { //no annotation
                return new ReadOnlyStringWrapper("\" \"");
            } else if (!termMap.containsKey(termId)) {//previously annotated with a term not found in current hpo
                return new ReadOnlyStringWrapper(termId.getValue());
            } else { //annotated with a term present in current hpo
                return new ReadOnlyStringWrapper(termMap.get(termId).getName());
            }
        });

        commentColumn.setSortable(true);
        commentColumn.setCellValueFactory(cdf -> cdf.getValue() == null ? new ReadOnlyStringWrapper("") :
                new ReadOnlyStringWrapper(cdf.getValue().getComment()));
        updateAnnotationSummaryWebview();
        refreshLoinc2HpoAnnotationTable();
    }



    private void initTableStructure() {
        loincIdTableColumn.setSortable(true);
        loincIdTableColumn.setCellValueFactory(cdf ->
                new ReadOnlyStringWrapper(cdf.getValue().getLoincId().toString())
        );
        componentTableColumn.setSortable(true);
        componentTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getComponent()));
        propertyTableColumn.setSortable(true);
        propertyTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getProperty()));
        timeAspectTableColumn.setSortable(true);
        timeAspectTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getTimeAspect()));
        methodTableColumn.setSortable(true);
        methodTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getMethod()));
        scaleTableColumn.setSortable(true);
        scaleTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getScale().toString()));
        systemTableColumn.setSortable(true);
        systemTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getSystem()));
        nameTableColumn.setSortable(true);
        nameTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLongName()));
        accordion.setExpandedPane(loincTableTitledpane);
    }



    private void updateHpoTermListView( List<HpoClassFound> result) {
        //among found terms, show those that are 1) HPO terms 2) not obsolete
        hpoQueryResult.addAll(result);
        hpoQueryResult.sort((o1, o2) -> o2.getScore() - o1.getScore());
        LOGGER.trace("hpoQueryResult size: " + hpoQueryResult.size());
        if (hpoQueryResult.size() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No HPO Found");
            alert.setContentText("Try search with synonyms");
            alert.show();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        //do nothing
                    } finally {
                        runLater(alert::close);
                    }
                    return null;
                }
            };
            Thread alertThread = new Thread(task);
            alertThread.start();
        }
        hpoListView.setItems(hpoQueryResult);
    }

    @FXML
    private void searchHpoByString(ActionEvent e) {
        e.consume();
        LoincEntry entry = loincTableView.getSelectionModel()
                .getSelectedItem();
        if (entry == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection ERROR");
            alert.setHeaderText("Select a row in Loinc table");
            alert.setContentText("A loinc code is required for ranking " +
                    "candidate HPO terms. Select one row in the loinc " +
                    "table and query again.");
            alert.showAndWait();
            return;
        }
        String query = userInputForManualQuery.getText().trim();
        String name = entry.getLongName();
        System.err.println("Got name: " + name);
        hpoListView.getItems().clear();
        LoincVsHpoQuery loincVsHpoQuery = optionalResources.getLoincVsHpoQuery();
        List<HpoClassFound> foundHpoList = loincVsHpoQuery.queryByLoincLongName(name, entry.getLoincLongName());
        updateHpoTermListView(foundHpoList);
    }

    /**
     * The user has selected a row in the LOINC table and searches for HPO terms with matching words.
     */
    @FXML
    private void searchHpoByLoinc(ActionEvent e) {
        e.consume();
        LoincEntry entry = loincTableView.getSelectionModel()
                .getSelectedItem();
        if (entry == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection ERROR");
            alert.setHeaderText("Select a row in Loinc table");
            alert.setContentText("A loinc code is required for ranking " +
                    "candidate HPO terms. Select one row in the loinc " +
                    "table and query again.");
            alert.showAndWait();
            return;
        }
        LOGGER.info(String.format("Start auto query for \"%s\"by pressing button", entry));
        String name = entry.getLongName();
        System.err.println("Got name: " + name);
        hpoListView.getItems().clear();
        LoincVsHpoQuery loincVsHpoQuery = optionalResources.getLoincVsHpoQuery();
        List<HpoClassFound> foundHpoList = loincVsHpoQuery.queryByLoincLongName(name, entry.getLoincLongName());
        updateHpoTermListView(foundHpoList);
    }

    @FXML
    private void handleManualQueryButton(ActionEvent e) {
        e.consume();
        LoincEntry entry = loincTableView.getSelectionModel().getSelectedItem();
        if (entry == null) {
            noLoincEntryAlert();
            return;
        }
        String userInput = userInputForManualQuery.getText();
        if (userInput == null || userInput.trim().length() < 2) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Input Error");
            alert.setHeaderText("Type in keys for manual query");
            alert.setContentText("Provide comma seperated keys for query. Do " +
                    "not use quotes(\"\"). Avoid non-specific words " +
                    "or numbers. Synonyms are strongly recommended if " +
                    "auto-query is not working.");
            alert.showAndWait();
            return;
        }
        String[] keys = userInput.split(",");
        List<String> keysInList = new ArrayList<>();
        for (String key : keys) {
            if (key.length() > 0) {
                keysInList.add(key);
            }
        }

        String name = entry.getLongName();
        LoincLongName loincLongName =  LoincLongName.of(name);
        LoincVsHpoQuery loincVsHpoQuery = optionalResources.getLoincVsHpoQuery();
        List<HpoClassFound> queryResults = loincVsHpoQuery.queryByLoincLongName(keysInList, loincLongName);
        if (queryResults.size() != 0) {
            ObservableList<HpoClassFound> items = FXCollections.observableArrayList();
            items.addAll(queryResults);
            this.hpoListView.setItems(items);
            userInputForManualQuery.clear();
        } else {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("0 HPO class is found. Try manual search with " +
                    "alternative keys (synonyms)");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No HPO Found");
            alert.setContentText("Try search with synonyms");
            alert.show();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        //do nothing
                    } finally {
                        runLater(alert::close);
                    }
                    return null;
                }
            };
            Thread alertThread = new Thread(task);
            alertThread.start();
        }
        //clear text in abnormality text fields if not currently editing a term
        if (!createAnnotationButton.getText().equals("Save")) {
            //Got user feedback that they do not want to clear the field when doing manual query
            //clearAbnormalityTextField();
            //inialize the flag field

            annotationNoteField.setText("");
        }
    }

    @FXML
    private void initLOINCtable(ActionEvent e) {
        this.loincmap = optionalResources.getLoincTableMap();
        if (this.loincmap.isEmpty()) {
            runLater(() -> PopUps.showWarningDialog("No LOINC data was imported",
                    "Warning",
                    "We could not import any LOINC data - \n did you import the correct LOINC file?"));
            return;
        }
        List<LoincEntry> lst = new ArrayList<>(loincmap.values());
        loincTableView.getItems().clear(); // remove any previous entries
        loincTableView.getItems().addAll(lst);
        loincTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // now ingest the HPO
        String pathToHPO = settings.getHpoJsonPath();
        LOGGER.info("pathToHPO: " + pathToHPO);
        initializeLoinc2HpoAnnotationTable();
        e.consume();
    }

    @FXML
    private void showAnnotationWindow(ActionEvent e) {
        e.consume();
        System.err.println("[showAnnotationWindow]");
    }




    /**
     * Search for LOINC entries by LOINC id
     */
    @FXML
    private void searchByLoincId(ActionEvent e) {
        e.consume();
        String query = this.loincSearchTextField.getText().trim();
        if (query.isEmpty()) return;
        List<LoincEntry> entrylist = new ArrayList<>();
        try {
            LoincId loincId = new LoincId(query);
            if (this.loincmap.containsKey(loincId)) {
                entrylist.add(this.loincmap.get(loincId));
                LOGGER.debug(this.loincmap.get(loincId).getLoincId() + " : " + this.loincmap.get(loincId).getLongName());
            } else { //correct loinc code form but not valid
                throw new Exception();
            }
        } catch (Exception msg) {
            PopUps.showException("Error", "Could not search LOINC data", msg);
            return;
        }
        LOGGER.trace(String.format("Searching table for:  %s", query));
        LOGGER.trace("# of loinc entries found: " + entrylist.size());
        loincTableView.getItems().clear();
        loincTableView.getItems().addAll(entrylist);
        accordion.setExpandedPane(loincTableTitledpane);
    }

    /**
     * This filters the available LOINC entries for annotation according to whether they
     * are present in a user-supplied List of LOINC Ids
     */
    @FXML
    private void handleLoincFiltering(ActionEvent e) {
        e.consume();
        List<LoincEntry> entrylist = new ArrayList<>();
        FileChooser chooser = new FileChooser();

        chooser.setTitle("File with Loinc codes to be annotated");
        File f = chooser.showOpenDialog(null);

        UserSuppliedLoincIdParser loincIdParser = new UserSuppliedLoincIdParser(f);
        Set<LoincId> userSuppliedLoincIds = loincIdParser.getLoincIdSet();
        if (userSuppliedLoincIds.isEmpty()) {
            LOGGER.error("Found 0 Loinc codes");
            PopUps.showWarningDialog("LOINC filtering",
                        "No hits found",
                        "Could not find any loinc codes");
            return;
        } else {
            LOGGER.trace("# of loinc entries found: " + userSuppliedLoincIds.size());
        }
        // get corresponding LoincEntries for display in the table
        Set<LoincEntry> filteredLoincEntries = optionalResources
                .getLoincTableMap().entrySet().stream()
                .filter(entry -> userSuppliedLoincIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        loincTableView.getItems().clear();
        loincTableView.getItems().addAll(filteredLoincEntries);
        accordion.setExpandedPane(loincTableTitledpane);
    }


    @FXML
    private void handleClear(ActionEvent event) {
        annotationNoteField.clear();
        if (clearButton.getText().equals("Cancel")) {
            clearButton.setText("Clear");
            //appTempData.setLoincUnderEditing(null);
        }
        createAnnotationButton.setText("Create annotation");
    }


    private void noLoincEntryAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Selection ERROR");
        alert.setHeaderText("Select a row in Loinc table");
        alert.setContentText("A loinc code is required for ranking " +
                "candidate HPO terms. Select one row in the loinc " +
                "table and query again.");
        alert.showAndWait();
    }

    /**
     * This method is called from the context menu of a row in the hpoListView.
     * The prupose of the method is to replace the current contents of the
     * hpoList view with the marked term and its descendants.
     */
    @FXML
    private void getChildHpoTerms(ActionEvent e) {
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        Set<TermId> descTids = OntologyAlgorithm.getChildTerms(hpo, tid,true);
        List<HpoClassFound> foundlist = new ArrayList<>();
        for (TermId t : descTids) {
            if (hpo.containsTerm(t)) {
                Term term = hpo.getTermMap().get(t);
                var hpoClass = new HpoClassFound(term.getId().getValue(), term.getName(), term.getDefinition(), null);
                foundlist.add(hpoClass);
            }
        }
        ObservableList<HpoClassFound> result = FXCollections.observableArrayList(foundlist);
        hpoListView.getItems().clear();
        hpoListView.setItems(result);
        e.consume();
    }

    /**
     * Replace the HPO terms in the hpoListView with the ancestor of the marked term.
     * This method is called form the context menu
     */
    @FXML
    private void getParentHpoTerms(ActionEvent e) {
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        Set<TermId> descTids = OntologyAlgorithm.getParentTerms(hpo, tid, true);
        List<HpoClassFound> foundlist = new ArrayList<>();
        for (TermId t : descTids) {
            if (hpo.containsTerm(t)) {
                Term term = hpo.getTermMap().get(t);
                var hpoClass = new HpoClassFound(term.getId().getValue(), term.getName(), term.getDefinition(), null);
                foundlist.add(hpoClass);
            }
        }
        ObservableList<HpoClassFound> result = FXCollections.observableArrayList(foundlist);
        hpoListView.getItems().clear();
        hpoListView.setItems(result);
        e.consume();
    }


    private void updateHpoAnnotationTable(HpoAnnotationRow row) {
        LOGGER.info("Adding " + row);
        List<HpoAnnotationRow> rows = new ArrayList<>(this.hpoAnnotationTable.getItems());
        rows.add(row);
        ObservableList<HpoAnnotationRow> result = FXCollections.observableArrayList(rows);
        runLater(() -> {
            hpoAnnotationTable.getItems().clear();
            hpoAnnotationTable.setItems(result);
        });
    }

    @FXML
    private void addQnLowTerm(ActionEvent e) {
        e.consume();
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        if (! hpo.containsTerm(tid)) {
            // should never happen, ,if it does, ,something is dodgy
            PopUps.showWarningDialog("Error", "addQnLowTerm",
                    String.format("Could not find term for %s", tid.getValue()));
            return;
        }
        Term term = hpo.getTermMap().get(tid);
        currentAnnotationModel.setQnLow(term);
        HpoAnnotationRow row = HpoAnnotationRow.qnLow(tid, term.getName());
        updateHpoAnnotationTable(row);
    }
    @FXML
    private void addQnHighTerm(ActionEvent e) {
        e.consume();
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        if (! hpo.containsTerm(tid)) {
            // should never happen, ,if it does, ,something is dodgy
            PopUps.showWarningDialog("Error", "addQnLowTerm",
                    String.format("Could not find term for %s", tid.getValue()));
            return;
        }
        Term term = hpo.getTermMap().get(tid);
        HpoAnnotationRow row = HpoAnnotationRow.qnHigh(tid, term.getName());
        currentAnnotationModel.setQnHigh(term);
        updateHpoAnnotationTable(row);
    }

    @FXML
    private void addNormalTerm(ActionEvent e) {
        e.consume();
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        if (! hpo.containsTerm(tid)) {
            // should never happen, ,if it does, ,something is dodgy
            PopUps.showWarningDialog("Error", "addQnLowTerm",
                    String.format("Could not find term for %s", tid.getValue()));
            return;
        }
        Term term = hpo.getTermMap().get(tid);
        currentAnnotationModel.setNormal(term);
        HpoAnnotationRow row = HpoAnnotationRow.normal(tid, term.getName());
        updateHpoAnnotationTable(row);
    }

    @FXML
    private void addOrdAbnormal(ActionEvent e) {
        e.consume();
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        if (! hpo.containsTerm(tid)) {
            // should never happen, ,if it does, ,something is dodgy
            PopUps.showWarningDialog("Error", "addQnLowTerm",
                    String.format("Could not find term for %s", tid.getValue()));
            return;
        }
        Term term = hpo.getTermMap().get(tid);
        currentAnnotationModel.setOrdAbnormal(term);
        HpoAnnotationRow row = HpoAnnotationRow.ordAbnormal(tid, term.getName());
        updateHpoAnnotationTable(row);
    }

    @FXML
    private void addNominalTerm(ActionEvent e) {
        e.consume();
        HpoClassFound selectedHpoTerm = this.hpoListView.getSelectionModel().getSelectedItem();
        Ontology hpo = this.optionalResources.getOntology();
        TermId tid = TermId.of(selectedHpoTerm.getId());
        if (! hpo.containsTerm(tid)) {
            // should never happen, ,if it does, ,something is dodgy
            PopUps.showWarningDialog("Error", "addQnLowTerm",
                    String.format("Could not find term for %s", tid.getValue()));
            return;
        }
        Term term = hpo.getTermMap().get(tid);
        currentAnnotationModel.setNominal(term);
        HpoAnnotationRow row = HpoAnnotationRow.nominal(tid, term.getName());
        updateHpoAnnotationTable(row);
    }

    /**
     * This gets called if the user right clicks on the candidate HPO annotations in the upper right.
     */
    @FXML
    public void deleteCandidateHpoAnnotation(ActionEvent e) {
        e.consume();
        HpoAnnotationRow selectedRow = this.hpoAnnotationTable.getSelectionModel().getSelectedItem();
        this.hpoAnnotationTable.getItems().remove(selectedRow);
    }


    @FXML
    private void descendantsOfMarkedTerm(ActionEvent e) {
        System.err.println("[WARNING] suggestNewChildTerm not implemented");
        e.consume();
    }

    /**
     * @param loincTermId a query LOINC id
     * @return LOINC2HPO annotations for this LOINC id (may be an empty list)
     */
    private List<Loinc2HpoAnnotation> getLoinc2HpoAnnotations(LoincId loincTermId) {
        return optionalResources.getIndividualLoinc2HpoAnnotations()
                .stream()
                .filter(annot -> annot.getLoincId().equals(loincTermId))
                .collect(Collectors.toList());
    }

    private String biocurationString() {
        String biocurator = optionalResources.getBiocurator();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s[%s]", biocurator, dtf.format(now));
    }


    @FXML
    protected void createNomAnnotation(ActionEvent event) {
        event.consume();
        if (loincTableView.getSelectionModel().getSelectedItem() == null) {
            PopUps.showInfoMessage("No loinc entry is selected. Try clicking \"Initialize Loinc Table\"",
                    "No Loinc selection Error");
            return;
        }
        LoincEntry loincEntryForAnnotation = loincTableView.getSelectionModel().getSelectedItem();
        LoincId loincCode = loincEntryForAnnotation.getLoincId();
        LoincScale loincScale = loincEntryForAnnotation.getScale();
        if (! loincScale.equals(LoincScale.NOMINAL)) {
            String errorMsg = String.format("You are trying to annotate with NOM but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        List<Loinc2HpoAnnotation> existingAnnotations = getLoinc2HpoAnnotations(loincCode);
        if (! existingAnnotations.isEmpty()) {
            boolean toOverwrite = PopUps.getBooleanFromUser("Do you want to overwrite?",
                    loincCode + " is already annotated", "Overwrite warning");
            if (!toOverwrite) return;
        }
        if (!currentAnnotationModel.validNomAnnotation()) {
            PopUps.showWarningDialog("Error", "Invalid data",
                    "Data for Ord (ordinal annotation) not valid");
            return;
        }
        String biocuration = biocurationString();
        String comment = annotationNoteField.getText().trim();
        Term nominal = currentAnnotationModel.getNominal();
        Loinc2HpoAnnotation nominalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.NOMINAL,
                Outcome.nominal("TODO"),
                nominal.getId(),
                biocuration,
                comment);

        Term normal = currentAnnotationModel.getNormal();
        Loinc2HpoAnnotation normalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.NOMINAL,
                Outcome.nominal("TODO"),
                normal.getId(),
                biocuration,
                comment);
        Map<Outcome, Loinc2HpoAnnotation> outcomeMap = new HashMap<>();
        outcomeMap.put(Outcome.nominal("TODO"),nominalAnnot);
        outcomeMap.put(Outcome.NORMAL(), normalAnnot);
        LoincAnnotation nominalLoincAnnotation = new NominalLoincAnnotation(outcomeMap);
        System.out.println("TODO NOMINAL ANNOTATION");
        addAnnotationAndUpdateGui(loincCode, nominalLoincAnnotation);
    }

    @FXML
    protected void createOrdAnnotation(ActionEvent event) {
        event.consume();
        if (loincTableView.getSelectionModel().getSelectedItem() == null) {
            PopUps.showInfoMessage("No loinc entry is selected. Try clicking \"Initialize Loinc Table\"",
                    "No Loinc selection Error");
            return;
        }
        LoincEntry loincEntryForAnnotation = loincTableView.getSelectionModel().getSelectedItem();
        LoincId loincCode = loincEntryForAnnotation.getLoincId();
        LoincScale loincScale = loincEntryForAnnotation.getScale();
        if (! loincScale.equals(LoincScale.ORDINAL)) {
            String errorMsg = String.format("You are trying to annotate with ORD but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        if (optionalResources.getLoincAnnotations().containsKey(loincCode)) {
            boolean toOverwrite = PopUps.getBooleanFromUser("Do you want to overwrite?",
                    loincCode + " is already annotated", "Overwrite warning");
            if (!toOverwrite) return;
        }

        String comment = annotationNoteField.getText().trim();
        Term abn = currentAnnotationModel.getOrdAbnormal();
        Term normal = currentAnnotationModel.getNormal();
        //map hpo terms to internal codes
        if (!currentAnnotationModel.validOrdAnnotation()) {
            String errorMsg;
            if (normal == null) {
                errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; \"normal\" term was null",
                        loincScale.name());
            } else if (abn == null) {
                errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; \"abnormal\" term was null",
                        loincScale.name());
            } else {
                errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; normal %s, abnormal: %s",
                        loincScale.name(), normal.getName(), abn.getName());
            }
            PopUps.showWarningDialog("Error", "Invalid data",
                    errorMsg);
            return;
        }

        String biocuration = biocurationString();

        Loinc2HpoAnnotation abnormalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.ORDINAL,
                Outcome.PRESENT(),
                abn.getId(),
                biocuration,
                comment);
        Loinc2HpoAnnotation normalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.ORDINAL,
                Outcome.ABSENT(),
                normal.getId(),
                biocuration,
                comment);
        LoincAnnotation ordinalAnnot = new OrdinalHpoAnnotation(normalAnnot, abnormalAnnot);
        addAnnotationAndUpdateGui(loincCode, ordinalAnnot);
    }

    @FXML
    private void createQnAnnotation(ActionEvent e) {
        if (loincTableView.getSelectionModel().getSelectedItem() == null) {
            PopUps.showInfoMessage("No loinc entry is selected. Try clicking \"Initialize Loinc Table\"",
                    "No Loinc selection Error");
            return;
        }
        LoincEntry loincEntryForAnnotation = loincTableView.getSelectionModel().getSelectedItem();
        LoincId loincCode = loincEntryForAnnotation.getLoincId();
        LoincScale loincScale = loincEntryForAnnotation.getScale();
        if (! loincScale.equals(LoincScale.QUANTITATIVE)) {
            String errorMsg = String.format("You are trying to annotate with Qn but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        if (optionalResources.getLoincAnnotations().containsKey(loincCode)) {
            boolean toOverwrite = PopUps.getBooleanFromUser("Do you want to overwrite?",
                    loincCode + " is already annotated", "Overwrite warning");
            if (!toOverwrite) return;
        }
        String comment = annotationNoteField.getText().trim();
        // there are three annotations to add
        Term low = currentAnnotationModel.getQnLow();
        Term normal = currentAnnotationModel.getNormal();
        Term high = currentAnnotationModel.getQnHigh();

       if (!currentAnnotationModel.validQnAnnotation()) {
           String errorMsg;
           if (normal == null) {
               errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; \"normal\" term was null",
                       loincScale.name());
           } else if (low == null) {
               errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; \"low\" term was null",
                       loincScale.name());
           } else if (high == null) {
               errorMsg = String.format("Invalid Ord annotation. LOINC type: %s; \"high\" term was null",
                       loincScale.name());
           } else {
               errorMsg = String.format("Invalid Qn annotation. LOINC type: %s; low: %s, normal %s, high: %s",
                       loincScale.name(), low.getName(), normal.getName(), high.getName());
           }
           PopUps.showWarningDialog("Error", "Invalid data",
                   errorMsg);
           return;
       }
        String biocuration = biocurationString();

        Loinc2HpoAnnotation lowAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.QUANTITATIVE,
                Outcome.LOW(),
                low.getId(),
                biocuration,
                comment);
        Loinc2HpoAnnotation normalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.QUANTITATIVE,
                Outcome.NORMAL(),
                normal.getId(),
                biocuration,
                comment);
        Loinc2HpoAnnotation highAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.QUANTITATIVE,
                Outcome.HIGH(),
                high.getId(),
                biocuration,
                comment);


        LoincAnnotation quantAnnot = new QuantitativeLoincAnnotation(lowAnnot, normalAnnot, highAnnot);
        addAnnotationAndUpdateGui(loincCode, quantAnnot);
        e.consume();
    }

    private void addAnnotationAndUpdateGui(LoincId loincCode, LoincAnnotation annotation) {
        optionalResources.getLoincAnnotations().put(loincCode, annotation);
        LoincAnnotationCreatedViewFactory factory =
                new LoincAnnotationCreatedViewFactory(optionalResources.getOntology(), annotation);
        boolean confirmed = factory.openDialogWithBoolean();
        LOGGER.trace("Confirmed annotation for " + annotation);
        if (! confirmed) {
            PopUps.showInfoMessage("Canceling new annotation", "warning");
            return;
        }
        annotationNoteField.clear();
        this.hpoAnnotationTable.getItems().clear();
        refreshLoinc2HpoAnnotationTable();
        updateAnnotationSummaryWebview();
        if (createAnnotationButton.getText().equals("Save")) {
            createAnnotationButton.setText("Create annotation");
        }
    }

    @FXML
    public void downloadHPO(ActionEvent e) {
        String dirpath = Platform.getLoinc2HpoDir().getAbsolutePath();
        File f = new File(dirpath);
        if (!f.isDirectory()) {
            LOGGER.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }
        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading hp.obo/.owl...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO download");
        window.setScene(scene);
        Task<Void> hpodownload = new HpoMenuDownloader(dirpath);
        new Thread(hpodownload).start();
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            LOGGER.trace(String.format("Successfully downloaded hpo to %s", dirpath));
            String fullpath = String.format("%s%shp.json", dirpath, File.separator);

            settings.setHpoJsonPath(fullpath);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download HPO obo file");
        });

        e.consume();
    }


    @FXML
    public void setPathToLoincCoreTableFile(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose LOINC Core Table file");
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            settings.setLoincCoreTablePath(path);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
            LOGGER.trace(String.format("Setting path to LOINC Core Table file to %s", path));
        } else {
            LOGGER.error("Unable to obtain path to LOINC Core Table file");
        }
        e.consume();
    }

    /**
     * Show the about message
     */
    @FXML
    private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("LOINC2HPO Biocuration tool");
        alert.setHeaderText("Loinc2Hpo");
        String s = "Biocurate HPO mappings for LOINC laboratory codes.\nversion: " + buildProperties.getVersion();
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }

    /**
     * Get a biocurator string such as HP:rrabbit from the user.
     */
    @FXML
    private void setBiocuratorID(ActionEvent e) {
        String current = settings.getBiocuratorID();
        String prompText = (current == null || current.isEmpty()) ? "e.g., MGM:rrabbit" : current;
        String bcid = getStringFromUser("Biocurator ID", prompText, "Enter biocurator ID");
        if (bcid != null && bcid.indexOf(":") > 0) {
            settings.setBiocuratorID(bcid);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
        } else {
            LOGGER.error(String.format("Invalid biocurator ID; must be of the form MGM:rrabbit; you tried: \"%s\"",
                    bcid != null ? bcid : ""));
        }
        e.consume();
    }


    @FXML
    private void setPathToCurationData(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Set path to LOINC2HPO annotation file (local clone from GitHub).");
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            String path = f.getAbsolutePath();
            settings.setAnnotationFile(path);
            Settings.writeSettings(settings, Platform.getPathToSettingsFile());
            LOGGER.trace(String.format("Setting path to LOINC2HPO annotation file to %s", path));
        } else {
            LOGGER.error("Unable to obtain path to LOINC2HPO annotation file");
            PopUps.showWarningDialog("Warning", "Error",
                    "Could not set path to LOINC2HPO annotation file");
        }
    }

    /**
     * Open a help dialog
     */
    @FXML
    private void openHelpDialog() {
        HelpViewFactory.openHelpDialog();
    }

    /**
     * Show the settings
     */
    @FXML
    private void openSettingsDialog() {
        SettingsViewFactory  factory= new SettingsViewFactory(settings);
        factory.openDialog();
    }

    /**
     * The function determines whether the data in annotations map and loincCategories has changed
     *
     * @return true if session data has not been saved yet
     */
    public boolean isSessionDataChanged() {
        //Lazy implementation
        //whenever createAnnotation, saveAnnotation, group/ungroup loinc or create loinc list are called, it return true
        System.err.println("WARNING -- isSessionDataChanged not implemented");
        return true;
    }

    @FXML
    private void handleSave(ActionEvent e) {
        LOGGER.trace("handleSaveSession");
        //Create a session if it is saved for the first time
        if (settings.getAnnotationFile() == null) {
            PopUps.showWarningDialog("Warning", "Error",
                    "Attempt to save annotation file without valid path (see configuration menu).");
            return;
        }
        String annotationTSVSingleFile = settings.getAnnotationFile();
        try {
            Loinc2HpoAnnotationParser.exportToTsv(optionalResources.getIndividualLoinc2HpoAnnotations(),
                    annotationTSVSingleFile);
        } catch (IOException ioe) {
            PopUps.showWarningDialog("Error message",
                    "Failure to Save Session Data" ,
                    String.format("An error occurred when trying to save data to %s. Try again!", annotationTSVSingleFile));
            return;
        }
        e.consume();
    }

    public void saveBeforeExit() {
        LOGGER.trace("SaveBeforeExit() is called");
        if (isSessionDataChanged()) {
            handleSave(null);
        } else {
            LOGGER.trace("data not changed. exit safely");
        }
    }

    @FXML
    public void close(ActionEvent e) {
        e.consume(); //important to consume it first; otherwise,
        //window will always close
        if (isSessionDataChanged()) {
            String[] choices = new String[]{"Yes", "No"};
            Optional<String> choice = PopUps.getToggleChoiceFromUser(choices,
                    "Session has been changed. Save changes? ", "Exit " +
                            "Confirmation");
            if (choice.isPresent() && choice.get().equals("Yes")) {
                saveBeforeExit();
                javafx.application.Platform.exit();
                System.exit(0);
            } else if (choice.isPresent() && choice.get().equals("No")) {
                javafx.application.Platform.exit();
                System.exit(0);
            }
        } else {
            javafx.application.Platform.exit();
            System.exit(0);
        }
    }

    // Annotation table
    private String getAnnotationSummaryHTML() {
        return "<html><body>\n" +
                inlineCSS() +
                "<ul><li>Number of HPO Terms " + optionalResources.getOntology().countNonObsoleteTerms() +"</li>" +
                "<li>Number of annotation LOINC codes: " + optionalResources.getLoincAnnotations().size() + "</li></ol>"
         + "</body></html>";
    }

    public void updateAnnotationSummaryWebview() {
        runLater(() -> {
            WebView wview = new WebView();
            WebEngine contentWebEngine = wview.getEngine();
            contentWebEngine.loadContent(getAnnotationSummaryHTML());
            this.vbox4wv.getChildren().clear();
            this.vbox4wv.getChildren().addAll(wview);
        });
    }




    private static String inlineCSS() {
        return "<head><style>\n" +
                "  html { margin: 0; padding: 0; }" +
                "body { font: 75% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 10; padding: 10; }" +
                "p { margin-top: 0;text-align: justify;}" +
                "  </style></head>";
    }

    public void setData(String html) {
        WebView wview = new WebView();
        WebEngine contentWebEngine = wview.getEngine();
        contentWebEngine.loadContent(html);
        this.vbox4wv.getChildren().addAll(wview);
    }


    /**
     * Ingest the existing annotation file and display it in one of the tabs of the accordeon.
     */
    private void refreshLoinc2HpoAnnotationTable() {
        List<Loinc2HpoAnnotation> annotations = optionalResources.getIndividualLoinc2HpoAnnotations();
        runLater(() -> {
            loincAnnotationTableView.getItems().clear();
            loincAnnotationTableView.getItems().addAll(annotations);
        });
    }

    @FXML
    private void handleReview(ActionEvent event) {
        if (optionalResources.getLoincTableMap() == null || optionalResources.getLoincTableMap().isEmpty()) {
            PopUps.showInfoMessage("The loinc number is not found. Try clicking \"Initialize LOINC Table\"", "Loinc Not Found");
            return;
        }
        if (optionalResources.getOntology() == null) {
            PopUps.showInfoMessage("HPO is not imported yet. Try downloading the HPO first (settings menu).", "HPO not imported");
            return;
        }
        Loinc2HpoAnnotation selected = loincAnnotationTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            PopUps.showInfoMessage("TODO -- IMPLEMENT REVIEW.", "WARNING");
            return;
        }

    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (optionalResources.getLoincTableMap() == null || optionalResources.getLoincTableMap().isEmpty()) {
            PopUps.showInfoMessage("The loinc number is not found. Try clicking \"Initialize LOINC Table\"", "Loinc Not Found");
            return;
        }
        if (optionalResources.getOntology() == null) {
            PopUps.showInfoMessage("HPO is not imported yet. Try downloading the HPO first (settings menu).", "HPO not imported");
            return;
        }
        Loinc2HpoAnnotation toEdit = loincAnnotationTableView.getSelectionModel().getSelectedItem();
        if (toEdit != null) {
            PopUps.showInfoMessage("TODO -- IMPLEMENT EDITING.", "WARNING");
            return;
        }
        event.consume();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        boolean confirmation = PopUps.getBooleanFromUser("Are you sure you want to delete the record?", "Confirm deletion request", "Deletion");
        if (confirmation) {
            Loinc2HpoAnnotation toDelete = loincAnnotationTableView.getSelectionModel().getSelectedItem();
            if (toDelete != null) {
                loincAnnotationTableView.getItems().remove(toDelete);
                optionalResources.getLoincAnnotations().remove(toDelete.getLoincId());
            }
        }
        event.consume();
    }

    protected void exportAnnotationsAsTSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Specify file name");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TSV files (*.txt)", "*.tsv"));
        File f = chooser.showSaveDialog(null);
        boolean overwrite = false;
        String path;
        if (f != null) {
            path = f.getAbsolutePath();
            if (f.exists()) { //check if user wants to overwrite the existing file
                overwrite = PopUps.getBooleanFromUser("Overwrite?",
                        "File will be overwritten", null);
            }

            if (!f.exists() || overwrite) {/*  TODO
                try {
                   // Loinc2HpoAnnotation.to_csv_file(optionalResources.getLoincAnnotationMap(), path);
                } catch (IOException e1) {
                    String errorMsg = String.format("An error occurred when trying to save data to %s. Try again!", path);
                    PopUps.showWarningDialog("Error message",
                            "Failure to Save Session Data" ,errorMsg);
                    LOGGER.error(errorMsg);
                }
                */
            }
        }
    }

    public void editExistingLoincAnnotation(ActionEvent e) {
        e.consume();
        LoincEntry selectedEntry = this.loincTableView.getSelectionModel().getSelectedItem();
        LoincId loincId = selectedEntry.getLoincId();
        if (! this.annotationsMap.containsKey(loincId)) {
            String msg = String.format("The LOINC entry %s has not been annotated yet. " +
                    "Create a new annotation by right clicking on HPO terms", loincId.toString());
            PopUps.showInfoMessage(msg, "Warning");
            return;
        }
        // if we get here, we have an existing annotation that the user wants to edit
        Loinc2HpoAnnotation annot = this.annotationsMap.get(loincId);
        Outcome outcome = annot.getOutcome();
        // clear annotation table if required
        hpoAnnotationTable.getItems().clear();
        TermId hpoTermId = annot.getHpoTermId();
        Optional<String> opt = optionalResources.getOntology().getTermLabel(hpoTermId);
        String label = opt.isPresent() ? opt.get() : "n/a";
        List<HpoAnnotationRow> rows = new ArrayList<>();
        HpoAnnotationRow row;
        switch (outcome.getCode()) {
                case H:
                    row = HpoAnnotationRow.qnHigh(hpoTermId,label);
                    break;
                case L:
                    row = HpoAnnotationRow.qnLow(hpoTermId,label);
                    break;
                case N:
                    row = HpoAnnotationRow.normal(hpoTermId, label);
                    break;
                case A:
                    row = HpoAnnotationRow.ordAbnormal(hpoTermId, label);
                    break;
                case NOM:
                    row = HpoAnnotationRow.nominal(hpoTermId, label);
                    break;
                default:
                    // should never ever happen
                    throw new Loinc2HpoRunTimeException("Did not recognize label: " + outcome.getCode());
        }
        rows.add(row);
        ObservableList<HpoAnnotationRow> result = FXCollections.observableArrayList(rows);
        runLater(() -> {
            hpoAnnotationTable.getItems().clear();
            hpoAnnotationTable.setItems(result);
        });
    }
}
