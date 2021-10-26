package org.monarchinitiative.loinc2hpo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.monarchinitiative.loinc2hpo.guitools.LoincAnnotationCreatedViewFactory;
import org.monarchinitiative.loinc2hpo.guitools.Platform;
import org.monarchinitiative.loinc2hpo.guitools.PopUps;
import org.monarchinitiative.loinc2hpo.guitools.SettingsViewFactory;
import org.monarchinitiative.loinc2hpo.io.HpoMenuDownloader;
import org.monarchinitiative.loinc2hpo.io.loincparser.HpoClassFound;
import org.monarchinitiative.loinc2hpo.io.loincparser.LoincLongNameComponents;
import org.monarchinitiative.loinc2hpo.io.loincparser.LoincLongNameParser;
import org.monarchinitiative.loinc2hpo.io.loincparser.LoincVsHpoQuery;
import org.monarchinitiative.loinc2hpo.model.*;
import org.monarchinitiative.loinc2hpo.model.codesystems.InternalCodeSystem;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincEntry;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincId;
import org.monarchinitiative.loinc2hpo.model.loinc.LoincScale;
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

    private final ObservableMap<LoincId, LoincEntry> loincMap = FXCollections.observableHashMap();

    private ObservableMap<LoincId, Loinc2HpoAnnotationModel> annotationsMap = FXCollections.observableHashMap();

    private final CurrentAnnotationModel currentAnnotationModel;

    //private final Stage primarystage;
    @FXML
    private Button initLOINCtableButton;
    @FXML
    private Button filterButton;
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
    private CheckMenuItem loincTableEnableMultiSelection;
    private Loinc2HpoAnnotationModel toCopy;


    private final ObservableList<AdvancedAnnotationTableComponent> tempAdvancedAnnotations = FXCollections.observableArrayList();

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
    private Button autoQueryButton;


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
    private TableView<Loinc2HpoAnnotationModel> loincAnnotationTableView;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> loincNumberColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> belowNormalHpoColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> notAbnormalHpoColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> aboveNormalHpoColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> loincScaleColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> loincFlagColumn;
    @FXML
    private TableColumn<Loinc2HpoAnnotationModel, String> noteColumn;
    final ObservableList<String> userCreatedLoincLists = FXCollections
            .observableArrayList();
    final private String LOINCWAITING4NEWHPO = "require_new_HPO_terms";
    final private String LOINCUNABLE2ANNOTATE = "unable_to_annotate";
    final private String UNSPECIFIEDSPECIMEN = "unspecified_specimen";
    final private String LOINC4QC = "test_for_QC";

    private BooleanProperty isPresentOrd = new SimpleBooleanProperty(false);
    private BooleanProperty configurationComplete = new SimpleBooleanProperty(false);
    /**
     * The allows us to get info from the pom.xml file
     * buildProperties.getName()
     * buildProperties.getVersion();
     * buildProperties.getTime();
     * buildProperties.getArtifact();
     * buildProperties.getGroup();
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
        // this.tableHidden = new SimpleBooleanProperty(true);
    }

    @FXML
    private void initialize() {
        LOGGER.trace("initialize() called");
        //read in settings from file
        File settingsFile = Settings.getPathToSettingsFileAndEnsurePathExists();
        try {
            Settings.loadSettings(settings, settingsFile.getPath());
        } catch (IOException e) {
            LOGGER.trace("okay, this is the first time you use it. Configure the settings now");
        }
        optionalResources.addSettings(settings);
        // run the initialization task on a separate thread
        StartupTask task = new StartupTask(optionalResources, pgProperties);
        //this.hpoReadyLabel.textProperty().bind(task.messageProperty());
        //task.setOnSucceeded(e -> this.hpoReadyLabel.textProperty().unbind());
        this.executor.submit(task);
        hpoChildTermsButton.setTooltip(new Tooltip("Suggest new HPO terms"));
        filterButton.setTooltip(new Tooltip("Filter Loinc by providing a Loinc list in txt file"));
        clearButton.setTooltip(new Tooltip("Clear all textfields"));
        allAnnotationsButton.setTooltip(new Tooltip("Display annotations for currently selected Loinc code"));
        initLOINCtableButton.setTooltip(new Tooltip("Ingest Loinc Core Table and HP (Download the files first)."));
        searchForLOINCIdButton.setTooltip(new Tooltip("Search Loinc with a Loinc code or name"));
        autoQueryButton.setTooltip(new Tooltip("Find candidate HPO terms"));

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


        //if user creates a new Loinc group, add two menuitems for it, and specify the actions when those menuitems are clicked
        userCreatedLoincLists.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    LOGGER.trace(c + " was added");
                    c.getAddedSubList()
                            .stream()
                            .forEach(p -> {
                                //putIfAbsent is important to prevent overwritten
                                //appTempData.addUserCreatedLoincList(p, new LinkedHashSet<>());
                                MenuItem newListMenuItem = new MenuItem(p);
                                groupUngroup2LoincListButton.getItems().add(newListMenuItem);
                                newListMenuItem.setOnAction((event -> {
                                    LOGGER.trace("action detected");
                                    if (loincTableView.getSelectionModel().getSelectedItem() != null) {
                                        LoincId loincId = loincTableView.getSelectionModel()
                                                .getSelectedItem().getLOINC_Number();


                                        changeColorLoincTableView();

                                    }
                                }));

                                MenuItem newExportMenuItem = new MenuItem(p);
                                exportLoincListButton.getItems().add(newExportMenuItem);
                                newExportMenuItem.setOnAction((event -> {
                                    LOGGER.trace("action detected");
                                    if (loincTableView.getSelectionModel().getSelectedItem() != null) {
                                        Set<LoincId> loincIds = Set.of(); //appResources.getUserCreatedLoincLists().get(p);
                                        if (loincIds.isEmpty()) {
                                            return;
                                        }
                                        FileChooser chooser = new FileChooser();
                                        chooser.setTitle("Save Loinc List: ");
                                        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TSV files (*.txt)", "*.txt"));
                                        chooser.setInitialFileName(p);
                                        File f = chooser.showSaveDialog(null);
                                        String filepath;
                                        if (f == null) {
                                            return;
                                        } else {
                                            filepath = f.getAbsolutePath();
                                        }

                                        StringBuilder builder = new StringBuilder();
                                        loincIds.forEach(l -> {
                                            builder.append(l);
                                            builder.append("\n");
                                        });

                                        //WriteToFile.writeToFile(builder.toString().trim(), filepath);
                                    }
                                }));

                                MenuItem newImportMenuItem = new MenuItem(p);
                                importLoincGroupButton.getItems().add(newImportMenuItem);
                                newImportMenuItem.setOnAction((event) -> {
                                    LOGGER.trace("user wants to import " + p);
                                    FileChooser chooser = new FileChooser();
                                    chooser.setTitle("Select file to import from");
                                    File f = chooser.showOpenDialog(null);
                                    if (f == null) {
                                        return;
                                    }
                                    List<String> malformed = new ArrayList<>();
                                    List<String> notFound = new ArrayList<>();
                                    if (!malformed.isEmpty() || !notFound.isEmpty()) {
                                        String malformedString = String.join("\n", malformed);
                                        String notFoundString = String.join("\n", notFound);
                                        PopUps.showInfoMessage(String.format("Malformed Loinc: %d\n%s\nNot Found: %d\n%s", malformed.size(), malformedString, notFound.size(), notFoundString), "Error during importing");
                                    }

                                });
                            });
                } else {
                    LOGGER.error("This should never happen");
                }
            }
        });

        //track what is selected in the loincTable. If currently selected LOINC is a Ord type with a Presence/Absence outcome, change the listener isPresentOrd to true; otherwise false.
        loincTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                isPresentOrd.setValue(newValue.isPresentOrd());
            }
        });

        loincTableEnableMultiSelection.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (observable != null) {
                if (newValue) {
                    LOGGER.trace("multi selection is enabled");
                    loincTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                } else {
                    LOGGER.trace("multi selection is not enabled");
                    loincTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                }
            } else {
                return;
            }
        });
        initTableStructure();
        initializeHpoAnnotationTable();
    }

    private void initializeHpoAnnotationTable() {
        this.hpoAnnotationTable.setEditable(false);
        hpoAnnotationTid.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getTid().getValue()));
        hpoAnnotationType.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLoincType()));
        hpoAnnotationTermLabel.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLabel()));
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
            } else if (!termMap.containsKey(termId)) {//previously annotated with a term not found in current hpo
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
            } else if (!termMap.containsKey(termId)) {//previously annotated with a term not found in current hpo
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
        refreshLoinc2HpoAnnotationTable();
    }



    private void initTableStructure() {
        loincIdTableColumn.setSortable(true);
        loincIdTableColumn.setCellValueFactory(cdf ->
                new ReadOnlyStringWrapper(cdf.getValue().getLOINC_Number().toString())
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
        scaleTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getScale()));
        systemTableColumn.setSortable(true);
        systemTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getSystem()));
        nameTableColumn.setSortable(true);
        nameTableColumn.setCellValueFactory(cdf -> new ReadOnlyStringWrapper(cdf.getValue().getLongName()));
        //hpoListView.setOrientation(Orientation.HORIZONTAL);

        accordion.setExpandedPane(loincTableTitledpane);
    }

    @FXML
    private void handleDoubleClickLoincTable(MouseEvent event) {
        if (event.getClickCount() == 2) {
            LoincEntry rowData = loincTableView.getSelectionModel().getSelectedItem();
            if (rowData == null) {
                return;
            }

            //disable further action if the user is not under Editing mode
//            if(appTempData.getLoincUnderEditing() != null && !appTempData.getLoincUnderEditing().equals(rowData)){
//                PopUps.showInfoMessage("You are currently editing " + rowData.getLOINC_Number() +
//                                ". Save or cancel editing current loinc annotation before switching to others",
//                        "Under Editing mode");
//            } else {
//                updateHpoTermListView(rowData);
//            }

        }

        if (!createAnnotationButton.getText().equals("Save")) { //under saving mode
            annotationNoteField.setText("");
            this.hpoAnnotationTable.getItems().clear();
        }
        event.consume();
    }


    private void updateHpoTermListView(LoincEntry entry) {
        String name = entry.getLongName();
        System.err.println("Got name: " + name);
        hpoListView.getItems().clear();
        LoincVsHpoQuery loincVsHpoQuery = optionalResources.getLoincVsHpoQuery();
        List<HpoClassFound> result = loincVsHpoQuery.query_manual(name, entry.getLongNameComponents());
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
    private void searchHpoTermButtonClicked(ActionEvent e) {
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
        updateHpoTermListView(entry);
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
        LoincLongNameComponents loincLongNameComponents = LoincLongNameParser.parse(name);
        LoincVsHpoQuery loincVsHpoQuery = optionalResources.getLoincVsHpoQuery();
        List<HpoClassFound> queryResults = loincVsHpoQuery.query_manual(keysInList, loincLongNameComponents);
        if (queryResults.size() != 0) {
            ObservableList<HpoClassFound> items = FXCollections.observableArrayList();
            for (HpoClassFound candidate : queryResults) {
                items.add(candidate);
            }
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

    @FXML
    private void search(ActionEvent e) {
        e.consume();
        String query = this.loincSearchTextField.getText().trim();
        if (query.isEmpty()) return;
        List<LoincEntry> entrylist = new ArrayList<>();
        try {
            LoincId loincId = new LoincId(query);
            if (this.loincmap.containsKey(loincId)) {
                entrylist.add(this.loincmap.get(loincId));
                LOGGER.debug(this.loincmap.get(loincId).getLOINC_Number() + " : " + this.loincmap.get(loincId).getLongName());
            } else { //correct loinc code form but not valid
                throw new Exception();
            }
        } catch (Exception msg) { //catch all kind of exception
            //logger.debug(loincEntry.getLOINC_Number() + " : " + loincEntry.getLongName());
            loincmap.values().stream()
                    .filter(loincEntry -> containedIn(query, loincEntry.getLongName()))
                    .forEach(entrylist::add);
            //.forEach(loincEntry -> entryListInOrder.add(loincEntry));
        }
        if (entrylist.isEmpty()) {
            //if (entryListInOrder.isEmpty()){
            LOGGER.error(String.format("Could not identify LOINC entry for \"%s\"", query));
            PopUps.showWarningDialog("LOINC Search",
                    "No hits found",
                    String.format("Could not identify LOINC entry for \"%s\"", query));
            return;
        } else {
            LOGGER.trace(String.format("Searching table for:  %s", query));
            LOGGER.trace("# of loinc entries found: " + entrylist.size());
            //logger.trace("# of loinc entries found: " + entryListInOrder.size());
        }
        loincTableView.getItems().clear();
        loincTableView.getItems().addAll(entrylist);
        accordion.setExpandedPane(loincTableTitledpane);
    }

    private boolean containedIn(String query, String text) {
        String[] keys = query.split("\\W");
        for (String key : keys) {
            if (!text.toLowerCase().contains(key.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    @FXML
    private void handleLoincFiltering(ActionEvent e) {
        e.consume();
        List<LoincEntry> entrylist = new ArrayList<>();
        String enlistName;
        FileChooser chooser = new FileChooser();
        if (settings.getAnnotationFile() != null) {
            chooser.setInitialDirectory(new File(settings.getAnnotationFile()));
        }
        chooser.setTitle("Choose File containing a list of interested Loinc " +
                "codes");
        File f = chooser.showOpenDialog(null);
        List<String> notFoundList = new ArrayList<>();
        List<String> malformedList = new ArrayList<>();
        int malformedLoincCount = 0;
        if (f != null) {
            String path = f.getAbsolutePath();
            enlistName = f.getName();
//            try {
//                Set<String> loincOfInterest = new LoincOfInterest(path).getLoincOfInterest();
//                //loincOfInterest.stream().forEach(System.out::print);
//                for (String loincString : loincOfInterest) {
//                    LoincId loincId;
//                    LoincEntry loincEntry;
//                    try {
//                        loincId = new LoincId(loincString);
//                        loincEntry = appResources.getLoincEntryMap().get(loincId);
//                    } catch (MalformedLoincCodeException e2) {
//                        //try to see whether user provided Loinc long common name
//                        loincEntry = appResources.getLoincEntryMapFromName().
//                                get(loincString);
//                        if (loincEntry == null) {
//                            logger.error("Malformed loinc");
//                            malformedList.add(loincString);
//                            continue;
//                        }
//                    }
//                    if (loincEntry != null) {
//                        entrylist.add(loincEntry);
//                    } else {
//                        notFoundList.add(loincString);
//                    }
//                }
//            } catch (FileNotFoundException e1) {
//                e1.printStackTrace();
//            }

            if (!malformedList.isEmpty() || !notFoundList.isEmpty()) {
                String malformed = String.join(",\n", malformedList);
                String notfound = String.join(",\n", notFoundList);
                String popupMessage = String.format("# malformed Loinc codes: %d\n %s\n\n# Loinc codes not found: %d\n%s",
                        malformedList.size(), malformed, notFoundList.size(), notfound);
                PopUps.showInfoMessage(popupMessage, "Incomplete import of Loinc codes");
            }
            if (entrylist.isEmpty()) {
                LOGGER.error("Found 0 Loinc codes");
                PopUps.showWarningDialog("LOINC filtering",
                        "No hits found",
                        "Could not find any loinc codes");
                return;
            } else {
                LOGGER.trace("Loinc filtering result: ");
                LOGGER.trace("# of loinc entries found: " + entrylist.size());
            }
            loincTableView.getItems().clear();
            loincTableView.getItems().addAll(entrylist);
            // appTempData.addFilteredList(enlistName, new ArrayList<>(entrylist)); //keep a record in appTempData
            //entrylist.forEach(p -> logger.trace(p.getLOINC_Number()));
            accordion.setExpandedPane(loincTableTitledpane);
        } else {
            LOGGER.info("Unable to obtain path to LOINC of interest file");
        }
    }

    @FXML
    private void lastLoincList(ActionEvent e) {
        e.consume();
//        List<LoincEntry> lastLoincList = appTempData.previousLoincList();
//        if (lastLoincList != null && !lastLoincList.isEmpty()) {
//            loincTableView.getItems().clear();
//            loincTableView.getItems().addAll(lastLoincList);
//        }
    }

    @FXML
    private void nextLoincList(ActionEvent e) {
        e.consume();

//        List<LoincEntry> nextLoincList = appTempData.nextLoincList();
//        if (nextLoincList != null && !nextLoincList.isEmpty()) {
//            loincTableView.getItems().clear();
//            loincTableView.getItems().addAll(nextLoincList);
//        }
    }

    @FXML
    private void newLoincList(ActionEvent e) {

        e.consume();
        String nameOfList = PopUps.getStringFromUser("New Loinc List", "Type in the name", "name");
        if (nameOfList == null) {
            return;
        }
        //appTempData.addUserCreatedLoincList(nameOfList, new LinkedHashSet<>());
        userCreatedLoincLists.add(nameOfList);
        Random rand = new Random();
        double[] randColorValues = rand.doubles(3, 0, 1).toArray();
        Color randColor = Color.color(randColorValues[0], randColorValues[1], randColorValues[2]);
        //  settings.getUserCreatedLoincListsColor().put(nameOfList, ColorUtils.colorValue(randColor));
    }

    @FXML
    private void setLoincGroupColor(ActionEvent e) {
        LOGGER.trace("user wants to set the color of LOINC groups");
        Stage window = new Stage();

        VBox root = new VBox();
        root.setSpacing(10);


        ToolBar toolBar = new ToolBar();
        final ComboBox<String> loincGroupCombo = new ComboBox<>();

        loincGroupCombo.getItems().addAll(userCreatedLoincLists);

        final ColorPicker colorPicker = new ColorPicker();
        loincGroupCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (observable != null) {
                final String colorString = settings.getUserCreatedLoincListsColor().get(newValue);
                if (colorString != null) {
                    final Color color = Color.web(colorString);
                    colorPicker.setValue(color);
                }

            }
        });
        loincGroupCombo.getSelectionModel().select(0);

        toolBar.getItems().addAll(loincGroupCombo, colorPicker);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(2));

        Map<String, TextField> gridPaneColors = new HashMap<>();
        for (int i = 0; i < userCreatedLoincLists.size(); i++) {
            TextField name = new TextField(userCreatedLoincLists.get(i));
            gridPane.add(name, 0, i);
            TextField color = new TextField();
            color.setBackground(new Background(new BackgroundFill(Color.web(settings.getUserCreatedLoincListsColor().get(name.getText())), null, null)));
            gridPane.add(color, 1, i);
            gridPaneColors.put(name.getText(), color);
            LOGGER.trace("color" + color.getBackground().getFills().toString());
        }


        colorPicker.setOnAction(t -> {
            settings.getUserCreatedLoincListsColor().put(loincGroupCombo.getSelectionModel().getSelectedItem(), colorPicker.getValue().toString());
            LOGGER.trace("new color: " + settings.getUserCreatedLoincListsColor().get(loincGroupCombo.getSelectionModel().getSelectedItem()));
            gridPaneColors.get(loincGroupCombo.getSelectionModel().getSelectedItem()).setBackground(new Background(new BackgroundFill(colorPicker.getValue(), null, null)));
            changeColorLoincTableView();
            // Settings.writeSettings(settings, Loinc2HpoPlatform.getPathToSettingsFile());
        });

        root.getChildren().addAll(toolBar, gridPane);
        Scene scene = new Scene(root, 400, 400);
        window.setScene(scene);
        window.showAndWait();
    }

    /**
     * This function is normally not useful because of MainController::openSession()
     */
    private void initializeUserCreatedLoincListsIfNecessary() {
        //execute the functionalities only once in each secession
//        if (!appResources.getUserCreatedLoincLists().isEmpty()) {
//            logger.trace("initializeUserCreatedLoincListsIfNecessary(): 1111");
//            return;
//        }
        //by default, there will be two user created lists
        //This is not scaling well. @TODO: consider other ways
        //consider detecting existing lists by scanning the folder
        List<String> initialListNames = new ArrayList<>();
        initialListNames.add(LOINCWAITING4NEWHPO);
        initialListNames.add(LOINCUNABLE2ANNOTATE);
        initialListNames.add(UNSPECIFIEDSPECIMEN);
        initialListNames.add(LOINC4QC);
        userCreatedLoincLists.addAll(initialListNames);
        LOGGER.trace("initializeUserCreatedLoincListsIfNecessary(): 2222");
        /*
        //create a menuitem for each and add to two menus; also create a list to record data
        groupUngroup2LoincListButton.getItems().clear();
        exportLoincListButton.getItems().clear();
        initialListNames.forEach(p -> {
            groupUngroup2LoincListButton.getItems().add(new MenuItem(p));
            exportLoincListButton.getItems().add(new MenuItem(p));
            appTempData.addUserCreatedLoincList(p, new ArrayList<>());
        });
         */
    }


    private void initializeMenuItemsForFilteredLists() {
//        if (!appTempData.getFilteredLoincListsMap().isEmpty()) {
//            loincListsButton.setDisable(false);
//            loincListsButton.getItems().clear();
//            List<MenuItem> menuItems = new ArrayList<>();
//            appTempData.getFilteredLoincListsMap().keySet().forEach(p -> {
//                MenuItem menuItem = new MenuItem(p);
//                menuItems.add(menuItem);
//            });
//            loincListsButton.getItems().addAll(menuItems);
//            logger.trace("menu items added");
//            //loincListsButton.getItems().forEach(p -> logger.trace("current: " + p.getText()));
//            loincListsButton.getItems().forEach(p -> p.setOnAction((event) -> {
//                logger.trace(p.getText());
//                List<LoincEntry> loincList = appTempData.getLoincList(p.getText());
//                if (loincList != null && !loincList.isEmpty()) {
//                    loincTableView.getItems().clear();
//                    loincTableView.getItems().addAll(loincList);
//                }
//            } ));
//
//        } else {
//            loincListsButton.setDisable(true);
//        }
    }

    @FXML
    private void buildContextMenuForLoinc(Event e) {
        e.consume();
        LOGGER.trace("context memu for loinc table requested");
        initializeMenuItemsForFilteredLists();
        //initializeUserCreatedLoincListsIfNecessary(); //usually not run
        LOGGER.trace("exit buildContextMenuForLoinc()");
    }



    /**
     * This gets called if the user right clicks on an HPO term in the
     * hpoListView
     */
    @FXML
    private void handleCandidateHPODoubleClick(MouseEvent e) {

//        if (e.getClickCount() == 2 && hpoListView.getSelectionModel()
//                .getSelectedItem() != null && hpoListView.getSelectionModel()
//                .getSelectedItem() instanceof HPO_Class_Found) {
//            HPO_Class_Found hpo_class_found = (HPO_Class_Found) hpoListView
//                    .getSelectionModel().getSelectedItem();
//            List<HPO_Class_Found> parents = SparqlQuery.getParents
//                    (hpo_class_found.getId());
//            List<HPO_Class_Found> children = SparqlQuery.getChildren
//                    (hpo_class_found.getId());
//
//            TreeItem<HPO_TreeView> rootItem = new TreeItem<>(new HPO_TreeView()); //dummy root node
//            rootItem.setExpanded(true);
//            TreeItem<HPO_TreeView> current = new TreeItem<>
//                    (new HPO_TreeView(hpo_class_found));
//
//            parents.stream() //add parent terms to root; add current to each parent term
//                    .map(p -> new TreeItem<>(new HPO_TreeView(p)))
//                    .forEach(p -> {
//                        rootItem.getChildren().add(p);
//                        p.getChildren().add(current);
//                        p.setExpanded(true);
//                    });
//            current.setExpanded(true);
//            children.stream() //add child terms to current
//                    .map(p -> new TreeItem<>(new HPO_TreeView(p)))
//                    .forEach(current.getChildren()::add);
//
//            this.treeView.setRoot(rootItem);
//        }
        e.consume();
    }



    //ensure that at least one LOINC entry is selected
    private boolean checkSelectedLoinc() {
        switch (loincTableView.getSelectionModel().getSelectionMode()) {
            case SINGLE:
                if (loincTableView.getSelectionModel().getSelectedItem() != null &&
                        loincTableView.getSelectionModel().getSelectedItem().getScale().equals(toCopy.getLoincScale().toString())) {
                    return true;
                }
            case MULTIPLE:
                if (loincTableView.getSelectionModel().getSelectedItems() != null &&
                        !loincTableView.getSelectionModel().getSelectedItems().isEmpty()) {
                    List<String> scaleTypes = loincTableView.getSelectionModel().getSelectedItems()
                            .stream().map(LoincEntry::getScale)
                            .distinct().collect(Collectors.toList());
                    if (scaleTypes.size() == 1 && scaleTypes.get(0).equals(toCopy.getLoincScale().toString())) {
                        return true;
                    }
                }
            default:
                return false;
        }
    }

    @FXML
    private void copyAnnotation(ActionEvent event) {
        LOGGER.trace("copy loincAnnotation to other LOINCs");
        LoincEntry selectedLoinc = loincTableView.getSelectionModel().getSelectedItem();
        if (selectedLoinc == null) {
            LOGGER.error("Select a LOINC entry to copy");
            return;
        }
//        if (!appResources.getLoincAnnotationMap().containsKey(selectedLoinc.getLOINC_Number())) {
//            PopUps.showWarningDialog("Error Selection", "LOINC does not have annotation", "Select a LOINC code that has already been annotated");
//            logger.error("Annotation does not exist for " + selectedLoinc.getLOINC_Number());
//            return;
//        }
//        toCopy = appResources.getLoincAnnotationMap().get(selectedLoinc.getLOINC_Number());
//        loincTableEnableMultiSelection.setSelected(true);
        //loincTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void pasteAnnotation(ActionEvent e) {
        LOGGER.trace("paste annotation");
        if (toCopy == null) {
            PopUps.showWarningDialog("Error Selection", "Unspecified item to copy", "Select a LOINC code to copy annotation from in the LOINC table");
            return;
        }
        if (!checkSelectedLoinc()) { //make sure there is at least one valid LOINC entry selection
            PopUps.showWarningDialog("Error Selection", "Possible Errors:", "1) No LOINC entry is selected;\n2) >= 1 selected LOINC entry does match the scale of origin LOINC");
            return;
        }

        if (!loincTableEnableMultiSelection.isSelected()) {
            pasteAnnotationTo(loincTableView.getSelectionModel().getSelectedItem().getLOINC_Number());
        } else {
            loincTableView.getSelectionModel().getSelectedItems().stream()
                    .forEach(loinc -> {
                        LOGGER.trace("copy to: " + loinc.getLongName());
                        pasteAnnotationTo(loinc.getLOINC_Number());
                    });
        }

        //refresh annotation tab
        //refreshTable();
        changeColorLoincTableView();
        //  appTempData.setSessionChanged(true);

        //reset
        toCopy = null;
        //loincTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        loincTableEnableMultiSelection.setSelected(false);

    }


    private LoincEntry getLoincIdSelected() {
        return loincTableView.getSelectionModel().getSelectedItem();
    }


    /**
     * loincTableView.getSelectionModel().select(loincEntry);
     * logger.debug("selected: " + loincTableView.getSelectionModel().getSelectedItem().getLOINC_Number().toString());
     * loincTableView.requestFocus();
     * int focusindex = 0;
     * for (int i = 0; i < loincTableView.getItems().size(); i++) {
     * if (loincTableView.getSelectionModel().isSelected()) {
     * focusindex = i;
     * }
     * }
     * logger.debug("focusindex: " + focusindex);
     **/
    protected void setLoincIdSelected(LoincEntry loincEntry) {
        //@TODO: this is a lazy implementation. We should try to put selected item in view
        loincTableView.getItems().clear();
        loincTableView.getItems().addAll(loincEntry);
    }






    /**
     * This method is called from the pop up window
     *
     * @param loincAnnotation passed from the pop up window
     */
    void editCurrentAnnotation(Loinc2HpoAnnotationModel loincAnnotation) {
        /*


         */

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

    void setLoincIdSelected(LoincId loincId) {
//        LoincEntry loincEntry = appResources.getLoincEntryMap().get(loincId);
//        setLoincIdSelected(loincEntry);
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



    @FXML
    private void descendantsOfMarkedTerm(ActionEvent e) {
        System.err.println("[WARNING] suggestNewChildTerm not implemented");
        e.consume();
    }


    private void pasteAnnotationTo(LoincId loincId) {
//        if (appResources.getLoincAnnotationMap().containsKey(loincId)) {
//            logger.trace("Overwrite: " + loincId);
//        }
        String comments = toCopy.getNote() == null ? "" : "@original comment: " + toCopy.getNote();
        String copyInfo = String.format("copied from: %s %s", toCopy.getLoincId().toString(), comments);

        Loinc2HpoAnnotationModel.Builder builder = new Loinc2HpoAnnotationModel.Builder()
                .setLoincId(loincId)
                .setLoincScale(toCopy.getLoincScale())
                .setCreatedBy(toCopy.getCreatedBy())
                .setCreatedOn(toCopy.getCreatedOn())
                .setLastEditedBy(toCopy.getLastEditedBy())
                .setLastEditedOn(toCopy.getLastEditedOn())
                .setNote(copyInfo)
                .setFlag(toCopy.getFlag())
                .setVersion(toCopy.getVersion());
        toCopy.getCandidateHpoTerms().entrySet().stream()
                .forEach(entry -> builder.addAnnotation(entry.getKey(), entry.getValue()));

        // appResources.getLoincAnnotationMap().put(loincId, builder.build());

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
        LoincId loincCode = loincEntryForAnnotation.getLOINC_Number();
        LoincScale loincScale = LoincScale.string2enum(loincEntryForAnnotation.getScale());
        if (! loincScale.equals(LoincScale.Nom)) {
            String errorMsg = String.format("You are trying to annotate with NOM but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        if (optionalResources.getLoincAnnotationMap().containsKey(loincCode)) {
            boolean toOverwrite = PopUps.getBooleanFromUser("Do you want to overwrite?",
                    loincCode + " is already annotated", "Overwrite warning");
            if (!toOverwrite) return;
        }
        if (!currentAnnotationModel.validNomAnnotation()) {
            PopUps.showWarningDialog("Error", "Invalid data",
                    "Data for Ord (ordinal annotation) not valid");
            return;
        }
        String comment = annotationNoteField.getText().trim();
        Term nominal = currentAnnotationModel.getNominal();
        Term normal = currentAnnotationModel.getNormal();
        Loinc2HpoAnnotationModel.Builder builder = new Loinc2HpoAnnotationModel.Builder();
        builder.setLoincId(loincCode)
                .setLoincScale(loincScale)
                .setNote(comment);
        builder.setNormalHpoTerm(normal.getId(), true)
                .setHighValueHpoTerm(nominal.getId());
        builder.setCreatedBy(settings.getBiocuratorID() == null ? MISSINGVALUE : settings.getBiocuratorID())
                .setCreatedOn(LocalDateTime.now().withNano(0))
                .setVersion(0.1);
        System.out.println("TODO NOMINAL ANNOTATION");
        Loinc2HpoAnnotationModel loinc2HPOAnnotation = builder.build();
        addAnnotationAndUpdateGui(loincCode, loinc2HPOAnnotation);
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
        LoincId loincCode = loincEntryForAnnotation.getLOINC_Number();
        LoincScale loincScale = LoincScale.string2enum(loincEntryForAnnotation.getScale());
        if (! loincScale.equals(LoincScale.Ord)) {
            String errorMsg = String.format("You are trying to annotate with ORD but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        if (optionalResources.getLoincAnnotationMap().containsKey(loincCode)) {
            boolean toOverwrite = PopUps.getBooleanFromUser("Do you want to overwrite?",
                    loincCode + " is already annotated", "Overwrite warning");
            if (!toOverwrite) return;
        }

        String comment = annotationNoteField.getText().trim();
        // there are three annotations to add
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
        Loinc2HpoAnnotationModel.Builder builder = new Loinc2HpoAnnotationModel.Builder();
        builder.setLoincId(loincCode)
                .setLoincScale(loincScale)
                .setNote(comment);
        builder.setNormalHpoTerm(normal.getId(), true)
                .setHighValueHpoTerm(abn.getId());
        builder.setCreatedBy(settings.getBiocuratorID() == null ? MISSINGVALUE : settings.getBiocuratorID())
                .setCreatedOn(LocalDateTime.now().withNano(0))
                .setVersion(0.1);
        Loinc2HpoAnnotationModel loinc2HPOAnnotation = builder.build();
        addAnnotationAndUpdateGui(loincCode, loinc2HPOAnnotation);
    }

    @FXML
    private void createQnAnnotation(ActionEvent e) {
        if (loincTableView.getSelectionModel().getSelectedItem() == null) {
            PopUps.showInfoMessage("No loinc entry is selected. Try clicking \"Initialize Loinc Table\"",
                    "No Loinc selection Error");
            return;
        }
        LoincEntry loincEntryForAnnotation = loincTableView.getSelectionModel().getSelectedItem();
        LoincId loincCode = loincEntryForAnnotation.getLOINC_Number();
        LoincScale loincScale = LoincScale.string2enum(loincEntryForAnnotation.getScale());
        if (! loincScale.equals(LoincScale.Qn)) {
            String errorMsg = String.format("You are trying to annotate with Qn but the mode of %s is %s",
                    loincCode, loincScale.name());
            PopUps.showInfoMessage(errorMsg,
                    "Inappropriate Loinc Mode");
            return;
        }
        if (optionalResources.getLoincAnnotationMap().containsKey(loincCode)) {
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


        Loinc2HpoAnnotationModel.Builder builder = new Loinc2HpoAnnotationModel.Builder();
        builder.setLoincId(loincCode)
                .setLoincScale(loincScale)
                .setNote(comment);
        builder.setLowValueHpoTerm(low.getId())
                .setNormalHpoTerm(normal.getId(), true)
                .setHighValueHpoTerm(high.getId());
        builder.addAnnotation(InternalCodeSystem.abnormal(), new HpoTerm4TestOutcome(normal.getId()));
        builder.setCreatedBy(settings.getBiocuratorID() == null ? MISSINGVALUE : settings.getBiocuratorID())
                .setCreatedOn(LocalDateTime.now().withNano(0))
                .setVersion(0.1);
        Loinc2HpoAnnotationModel loinc2HPOAnnotation = builder.build();
        addAnnotationAndUpdateGui(loincCode, loinc2HPOAnnotation);
        e.consume();
    }

    private void addAnnotationAndUpdateGui(LoincId loincCode, Loinc2HpoAnnotationModel annotation) {
        optionalResources.getLoincAnnotationMap().put(loincCode, annotation);
        LoincAnnotationCreatedViewFactory factory =
                new LoincAnnotationCreatedViewFactory(optionalResources.getOntology(), annotation);
        boolean confirmed = factory.openDialogWithBoolean();
        LOGGER.error("Confirmed " + confirmed);
        if (! confirmed) {
            PopUps.showInfoMessage("Canceling new annotation", "warning");
            return;
        }
        annotationNoteField.clear();
        loincTableEnableMultiSelection.setSelected(false);
        this.hpoAnnotationTable.getItems().clear();
        refreshLoinc2HpoAnnotationTable();
        if (createAnnotationButton.getText().equals("Save")) {
            createAnnotationButton.setText("Create annotation");
        }
        changeColorLoincTableView();
    }


    //change the color of rows to green after the loinc code has been annotated
    void changeColorLoincTableView() {
        /*
        logger.debug("enter changeColorLoincTableView");
        logger.info("annotated LOINC count: " + appResources.getLoincAnnotationMap().size());
        logger.info("num Loinc Categories: " + appResources.getUserCreatedLoincLists().keySet());
        logger.info("unable_to_annotate: " + appResources.getUserCreatedLoincLists().get("unspecified_specimen").size());
        logger.info("colors: " + appResources.getUserCreatedLoincListsColor().values());
        loincIdTableColumn.setCellFactory(x -> new TableCell<LoincEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                //if(item != null && !empty) {
                if(!empty) {
                    setText(item);
                    try {
                        if(appResources.getLoincAnnotationMap().containsKey(new LoincId(item))) {
                            TableRow<LoincEntry> currentRow = getTableRow();
                            currentRow.setStyle("-fx-background-color: cyan");
                        } else {
                            TableRow<LoincEntry> currentRow = getTableRow();
                            LoincId loincId = new LoincId(item);
                            List<String> inList = appResources.getUserCreatedLoincLists().entrySet()
                                    .stream()
                                    .filter(entry -> entry.getValue().contains(loincId))
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());
                            if (!inList.isEmpty()) {
                                List<Color> colors = inList.stream()
                                    .map(l -> settings.getUserCreatedLoincListsColor().get(l))
                                    .filter(Objects::nonNull)
                                    .map(Color::web)
                                    .collect(Collectors.toList());
                                if (colors.isEmpty()) {
                                    currentRow.setStyle("");
                                } else {
                                    String backgroundColorValue = colors.get(0).toString(); //just use the first color
                                    logger.trace(backgroundColorValue);
                                    logger.trace(String.format("#%s", backgroundColorValue.substring(2,8).toUpperCase()));
                                    currentRow.setStyle("-fx-background-color: " + String.format("#%s", backgroundColorValue.substring(2,8).toUpperCase()));
                                    //Cannot use set background. It DOES NOT work!
                                    //currentRow.setBackground(new Background(new BackgroundFill(colors.get(0), null, null)));
                                }

                            } else {
                                currentRow.setStyle("");
                            }
                        }
                    } catch (MalformedLoincCodeException e) {
                        //do nothing
                        logger.error("should never happen:xdeide");
                    }
                } else {
                    setText(null);
                    getTableRow().setStyle("");
                    //logger.trace("changecolor:44444");
                }
            }

        });

         */
        LOGGER.debug("exit changeColorLoincTableView");
    }




    @FXML
    private void handleDeleteCodedAnnotation(ActionEvent event) {
        /*
        event.consume();
        logger.debug("user wants to delete an annotation");
        logger.debug("tempAdvancedAnnotations size: " + tempAdvancedAnnotations.size());
        AdvancedAnnotationTableComponent selectedToDelete = advancedAnnotationTable.getSelectionModel().getSelectedItem();
        if (selectedToDelete != null) {
            tempAdvancedAnnotations.remove(selectedToDelete);
        }
        logger.debug("tempAdvancedAnnotations size: " + tempAdvancedAnnotations.size());

         */
    }


    @FXML
    public void downloadHPO(ActionEvent e) {
        String dirpath = Platform.getLoinc2HpoDir().getAbsolutePath();
        File f = new File(dirpath);
        if (f == null || !(f.exists() && f.isDirectory())) {
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
        System.err.println("[WARNING] Help dialog not implemented");
        //HelpViewFactory.openHelpDialog();
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
        //return appTempData.isSessionChanged();
        System.err.println("WARNING -- isSessionDataChanged not implemented");
        return true;
    }

    @FXML
    private void handleSave(ActionEvent e) {
        LOGGER.trace("handleSaveSession");
        //Create a session if it is saved for the first time
        if (settings.getAnnotationFile() == null) {
            PopUps.showWarningDialog("Warning", "Error",
                    "Attempt to save files without annotation folder");
            return;
        }

        String dataDir = settings.getAnnotationFile() + File.separator + "Data";
        System.err.println("WARNING -- SAVE NOT IMPLEMENTED");
//
 /*
        Path folderTSVSingle = Paths.get(dataDir + File.separator + Constants.TSVSingleFileFolder);
        if (!Files.exists(folderTSVSingle)) {
            try {
                Files.createDirectory(folderTSVSingle);
            } catch (IOException e1) {
                PopUps.showWarningDialog("Error message",
                        "Failure to create folder" ,
                        String.format("An error occurred when trying to make a directory at %s. Try again!", folderTSVSingle));
                return;
            }
        }

        String annotationTSVSingleFile = folderTSVSingle.toString() + File.separator + Constants.TSVSingleFileName;
        try {
            Loinc2HpoAnnotationModel.to_csv_file(appResources.getLoincAnnotationMap(), annotationTSVSingleFile);
        } catch (IOException e1) {
            PopUps.showWarningDialog("Error message",
                    "Failure to Save Session Data" ,
                    String.format("An error occurred when trying to save data to %s. Try again!", annotationTSVSingleFile));
            return;
        }

        String pathToLoincCategory = dataDir + File.separator + LOINC_CATEGORY_folder;
        if (!new File(pathToLoincCategory).exists()) {
            new File(pathToLoincCategory).mkdir();
        }
        appResources.getUserCreatedLoincLists().entrySet()
                .forEach(p -> {
                    String path = pathToLoincCategory + File.separator + p.getKey() + ".txt";
                    Set<LoincId> loincIds = appResources.getUserCreatedLoincLists().get(p.getKey());
                    StringBuilder builder = new StringBuilder();
                    loincIds.forEach(l -> {
                        builder.append (l);
                        builder.append("\n");
                    });
                    WriteToFile.writeToFile(builder.toString().trim(), path);
                });

        //reset the session change tracker
        appTempData.setSessionChanged(false);
           */
        if (e != null) {
            e.consume();
        }

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
                //window.close();
            } else if (choice.isPresent() && choice.get().equals("No")) {
                javafx.application.Platform.exit();
                System.exit(0);
                //window.close();
            } else {
                //hang on. No action required
            }
        } else {
            javafx.application.Platform.exit();
            System.exit(0);
            //window.close();
        }
    }

    // Annotation table
    private String getHTML() {
        String html = "<html><body>\n" +
                inlineCSS() +
                "<h1>LOINC2HPO Biocuration: Summary</h1>";
        if ("" != null) {
            return html + getLoincAnnotationData() + "</body></html>";
        } else {
            return html + "</body></html>";
        }


    }

    public void updateSummary() {
        runLater(() -> {
            WebView wview = new WebView();
            WebEngine contentWebEngine = wview.getEngine();
            contentWebEngine.loadContent(getHTML());
            this.vbox4wv.getChildren().addAll(wview);
        });
    }

    private String getLoincAnnotationData() {
        StringBuilder sb = new StringBuilder();
        /*
        sb.append(String.format("<li>Number of HPO Terms: %d</li>",appResources.getHpo().countNonObsoleteTerms()));
        sb.append(String.format("<li>Number of annotation LOINC codes: %d</li>",appResources.getLoincAnnotationMap().size()));
          */
        return String.format("<ul>%s</ul>", sb.toString());

    }


    private static String inlineCSS() {
        return "<head><style>\n" +
                "  html { margin: 0; padding: 0; }" +
                "body { font: 100% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 10; padding: 10; }" +
                "p { margin-top: 0;text-align: justify;}" +
                "h2,h3 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;" +
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}" +
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
        Map<LoincId, Loinc2HpoAnnotationModel> testmap = optionalResources.getLoincAnnotationMap();
        runLater(() -> {
            loincAnnotationTableView.getItems().clear();
            loincAnnotationTableView.getItems().addAll(testmap.values());
        });
    }

    @FXML
    private void handleReview(ActionEvent event) {
        /*
        if (appResources.getLoincEntryMap() == null || appResources.getLoincEntryMap().isEmpty()) {
            PopUps.showInfoMessage("The loinc number is not found. Try clicking \"Initialize LOINC Table\"", "Loinc Not Found");
            return;
        }
        if (appResources.getTermidTermMap() == null || appResources.getTermidTermMap().isEmpty()) {
            PopUps.showInfoMessage("Hpo is not imported yet. Try clicking \"Initialize HPO appTempData\" first.", "HPO not imported");
            return;
        }
        Loinc2HpoAnnotationModel selected = loincAnnotationTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            annotateTabController.setLoincIdSelected(selected.getLoincId());
            annotateTabController.showAllAnnotations(event);
        }
    */
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        /*
        if (appResources.getLoincEntryMap() == null || appResources.getLoincEntryMap().isEmpty()) {
            PopUps.showInfoMessage("The loinc number is not found. Try clicking \"Initialize LOINC Table\"", "Loinc Not Found");
            return;
        }
        if (appResources.getTermidTermMap() == null || appResources.getTermidTermMap().isEmpty()) {
            PopUps.showInfoMessage("Hpo is not imported yet. Try clicking \"Initialize HPO appTempData\" first.", "HPO not imported");
            return;
        }

        Loinc2HpoAnnotationModel toEdit = loincAnnotationTableView.getSelectionModel()
                .getSelectedItem();
        if (toEdit != null) {
            mainController.switchTab(MainController.TabPaneTabs.AnnotateTabe);
            annotateTabController.editCurrentAnnotation(toEdit);
        }

         */
        event.consume();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        /*
        boolean confirmation = PopUps.getBooleanFromUser("Are you sure you want to delete the record?", "Confirm deletion request", "Deletion");
        if (confirmation) {
            Loinc2HpoAnnotationModel toDelete = loincAnnotationTableView.getSelectionModel()
                    .getSelectedItem();
            if (toDelete != null) {
                loincAnnotationTableView.getItems().remove(toDelete);
                appResources.getLoincAnnotationMap().remove(toDelete.getLoincId());
                appTempData.setSessionChanged(true);
            }
        }

         */
        event.consume();
    }

    protected void exportAnnotationsAsTSV() {
        /*
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

            if (!f.exists() || overwrite) {
                try {
                    Loinc2HpoAnnotationModel.to_csv_file(appResources.getLoincAnnotationMap(), path);
                } catch (IOException e1) {
                    PopUps.showWarningDialog("Error message",
                            "Failure to Save Session Data" ,
                            String.format("An error occurred when trying to save data to %s. Try again!", path));
                    return;
                }
            }
        }

         */
    }

}
