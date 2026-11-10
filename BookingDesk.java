package com.transport;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * "Booking Desk" window - the JavaFX topics of Unit 5, each one marked with  >> UNIT 5 <<
 *
 *   MenuBar, Menu, MenuItem (+ image on MenuItem), Toolbar, RadioButton (ToggleGroup), ComboBox,
 *   TextField, ListView, TreeView, Effects (DropShadow, Glow, Reflection), Transforms (Rotate, Scale)
 *
 * It is also a BookingListener (Unit 3, delegation event model): every seat change in BookingService
 * arrives here as a BookingEvent, possibly from a background thread, so UI updates use Platform.runLater.
 */
public final class BookingDesk {

    /** One line in the activity list (a record = immutable data class). */
    private record Row(long time, String seat, String passenger, String status) {
        static Row of(BookingEvent e) { return new Row(e.getTime(), e.getSeatId(), e.getPassenger(), e.getType().name()); }
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static BookingDesk instance;                         // only one desk window at a time

    private final BookingService service;
    private final Stage stage = new Stage();

    private final ObservableList<Row> rows = FXCollections.observableArrayList();        // Unit 4: collections
    private final FilteredList<Row> filtered = new FilteredList<>(rows, r -> true);       // Unit 5: generics
    private final SortedList<Row> sorted = new SortedList<>(filtered);

    private final ListView<Row> list = new ListView<>(sorted);                            // >> UNIT 5 << ListView
    private final TextField search = new TextField();                                     // >> UNIT 5 << TextField
    private final ToggleGroup statusGroup = new ToggleGroup();                            // >> UNIT 5 << RadioButton group
    private final ComboBox<String> sortBox = new ComboBox<>();                            // >> UNIT 5 << ComboBox
    private final Label summary = new Label();
    private final Label title = new Label("Booking Desk");

    /** Delegation event model: the adapter overrides ONLY what it needs - here all four events. */
    private final BookingListener listener = new BookingAdapter() {
        @Override public void onReserved(BookingEvent e) { add(e); }
        @Override public void onBooked(BookingEvent e) { add(e); }
        @Override public void onReleased(BookingEvent e) { add(e); }
        @Override public void onExpired(BookingEvent e) { add(e); }
        private void add(BookingEvent e) { Platform.runLater(() -> rows.add(Row.of(e))); }   // may be called from any thread
    };

    public static void open(BookingService service, String css) {
        if (instance != null && instance.stage.isShowing()) { instance.stage.toFront(); return; }
        instance = new BookingDesk(service, css);
        instance.stage.show();
    }

    private BookingDesk(BookingService service, String css) {
        this.service = service;
        for (BookingEvent e : service.getHistory()) rows.add(Row.of(e));
        service.addBookingListener(listener);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("bd-root");
        root.setTop(new VBox(buildMenuBar(), buildToolBar()));
        root.setLeft(buildTreeCard());
        root.setCenter(buildCenter());

        Scene scene = new Scene(root, 980, 640);
        scene.getStylesheets().add(css);
        stage.setTitle("Booking Desk");
        stage.setScene(scene);
        stage.setOnHidden(e -> service.removeBookingListener(listener));   // unregister: no leak
        rows.addListener((ListChangeListener<Row>) c -> updateSummary());
        filtered.addListener((ListChangeListener<Row>) c -> updateSummary());
        applySort();
        updateSummary();
    }

    // ------------------------------------------------------------------ >> UNIT 5 << MenuBar / Menu / MenuItem
    private MenuBar buildMenuBar() {
        MenuItem export = new MenuItem("Export activity…", icon(Color.web("#2f6fdb")));      // image on a MenuItem
        export.setOnAction(e -> exportActivity());
        MenuItem close = new MenuItem("Close window", icon(Color.web("#d64545")));
        close.setOnAction(e -> stage.close());
        Menu file = new Menu("File", null, export, new SeparatorMenuItem(), close);

        MenuItem refresh = new MenuItem("Refresh", icon(Color.web("#2e9e44")));
        refresh.setAccelerator(new KeyCodeCombination(KeyCode.F5));                          // keyboard shortcut
        refresh.setOnAction(e -> refresh());
        MenuItem clear = new MenuItem("Clear list", icon(Color.web("#8c978b")));
        clear.setOnAction(e -> rows.clear());
        MenuItem stress = new MenuItem("Run 10-user stress test", icon(Color.web("#e0a100")));
        stress.setOnAction(e -> runStress());
        Menu booking = new Menu("Booking", null, refresh, clear, new SeparatorMenuItem(), stress);

        CheckMenuItem glow = new CheckMenuItem("Glow effect on title");
        glow.setSelected(true);
        glow.selectedProperty().addListener((o, a, on) -> title.setEffect(on ? titleEffect() : null));
        Menu view = new Menu("View", null, glow);

        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "Waygo Booking Desk\nLive activity of the seat-reservation engine.").showAndWait());
        Menu help = new Menu("Help", null, about);
        return new MenuBar(file, booking, view, help);
    }

    /** Draws a 16x16 round icon pixel by pixel (so no image files are needed). */
    private static ImageView icon(Color color) {
        WritableImage img = new WritableImage(16, 16);
        PixelWriter pw = img.getPixelWriter();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++) {
                double d = Math.hypot(x - 7.5, y - 7.5);
                pw.setColor(x, y, d < 7 ? color : Color.TRANSPARENT);
            }
        return new ImageView(img);
    }

    // ------------------------------------------------------------------ >> UNIT 5 << Toolbar + Effects + Transforms
    private ToolBar buildToolBar() {
        ImageView spin = icon(Color.web("#2e9e44"));
        Button refresh = new Button("Refresh", spin);
        refresh.setOnAction(e -> {
            RotateTransition rt = new RotateTransition(Duration.millis(500), spin);        // Transform: rotation
            rt.setByAngle(360);
            rt.play();
            refresh();
        });
        Button stress = new Button("Stress test", icon(Color.web("#e0a100")));
        stress.setOnAction(e -> runStress());
        Button clear = new Button("Clear", icon(Color.web("#8c978b")));
        clear.setOnAction(e -> rows.clear());
        for (Button b : new Button[]{refresh, stress, clear}) hoverEffect(b);

        Label live = new Label("LIVE");                                                    // "stamp" badge
        live.getStyleClass().add("bd-badge");
        live.getTransforms().addAll(new Rotate(-8, 0, 0), new Scale(1.1, 1.1));            // Transforms: Rotate + Scale
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        return new ToolBar(refresh, stress, clear, new Separator(), grow, live);
    }

    /** Effect on hover: Glow + a small scale-up. */
    private static void hoverEffect(Node n) {
        n.setOnMouseEntered(e -> { n.setEffect(new Glow(0.6)); n.setScaleX(1.06); n.setScaleY(1.06); });
        n.setOnMouseExited(e -> { n.setEffect(null); n.setScaleX(1); n.setScaleY(1); });
    }

    private static Reflection titleEffect() {
        Reflection r = new Reflection();                                                   // Effect: reflection under the text
        r.setFraction(0.5);
        r.setInput(new DropShadow(6, Color.rgb(16, 43, 44, 0.25)));                        // Effect chaining: shadow -> reflection
        return r;
    }

    // ------------------------------------------------------------------ >> UNIT 5 << TreeView
    private Node buildTreeCard() {
        TreeItem<String> root = new TreeItem<>("Waygo network");
        root.getChildren().addAll(
                route("Route A: Central Station → Airport", "Central Station", "City Mall", "Airport T1", "Airport"),
                route("Route B: City Center → University", "City Center", "Library Square", "University"),
                route("Route C: Harbor → Business Park", "Harbor", "Fish Market", "Tech Park", "Business Park"));
        root.setExpanded(true);
        TreeView<String> tree = new TreeView<>(root);
        tree.getSelectionModel().selectedItemProperty().addListener((o, a, item) -> {
            if (item != null) updateSummary(item.getValue());
        });
        VBox box = new VBox(10, sectionTitle("Network"), tree);
        VBox.setVgrow(tree, Priority.ALWAYS);
        box.getStyleClass().add("bd-card");
        box.setPadding(new Insets(16));
        box.setPrefWidth(260);
        BorderPane.setMargin(box, new Insets(16, 0, 16, 16));
        return box;
    }

    private static TreeItem<String> route(String name, String... stops) {
        TreeItem<String> r = new TreeItem<>(name);
        for (String s : stops) r.getChildren().add(new TreeItem<>(s));
        return r;
    }

    // ------------------------------------------------------------------ center: filters + ListView
    private Node buildCenter() {
        title.getStyleClass().add("bd-title");
        title.setEffect(titleEffect());

        search.setPromptText("Search seat or passenger…");
        search.getStyleClass().add("wg-input");
        search.setPrefHeight(34);
        search.textProperty().addListener((o, a, b) -> applyFilter());
        search.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) search.clear(); });   // KeyEvent handling

        HBox radios = new HBox(12);
        radios.setAlignment(Pos.CENTER_LEFT);
        for (String name : new String[]{"All", "Reserved", "Booked", "Released"}) {
            RadioButton rb = new RadioButton(name);                                           // >> UNIT 5 << RadioButton
            rb.setToggleGroup(statusGroup);
            rb.setUserData(name);
            if (name.equals("All")) rb.setSelected(true);
            radios.getChildren().add(rb);
        }
        statusGroup.selectedToggleProperty().addListener((o, a, b) -> applyFilter());

        sortBox.setItems(FXCollections.observableArrayList("Newest first", "Oldest first", "Seat number"));
        sortBox.setValue("Newest first");
        sortBox.setOnAction(e -> applySort());

        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Row r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setText(null); setStyle(""); return; }
                String t = TIME.format(LocalTime.ofInstant(Instant.ofEpochMilli(r.time()), ZoneId.systemDefault()));
                setText(String.format("%s   %-4s %-14s %s", t, r.seat(), r.passenger() == null ? "-" : r.passenger(), r.status()));
                setStyle("-fx-font-family: 'Consolas','Monospaced'; -fx-text-fill: " + colorOf(r.status()) + ";");
            }
        });
        list.setPlaceholder(new Label("No activity yet - reserve a seat in the main window."));
        VBox.setVgrow(list, Priority.ALWAYS);

        summary.getStyleClass().addAll("s12", "c-muted");
        HBox filters = new HBox(14, search, radios, sortBox);
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        VBox card = new VBox(12, title, filters, list, summary);
        card.getStyleClass().add("bd-card");
        card.setPadding(new Insets(16));
        card.setEffect(new DropShadow(12, Color.rgb(16, 43, 44, 0.10)));                    // Effect: card shadow
        BorderPane.setMargin(card, new Insets(16));
        return card;
    }

    private static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().addAll("s14", "w6", "c-ink");
        return l;
    }

    private static String colorOf(String status) {
        return switch (status) {
            case "RESERVED" -> "#8a5a00";
            case "BOOKED" -> "#1b7a3a";
            case "EXPIRED" -> "#b3261e";
            default -> "#6b7973";
        };
    }

    // ------------------------------------------------------------------ filtering, sorting, actions
    private void applyFilter() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        String which = statusGroup.getSelectedToggle() == null ? "All" : (String) statusGroup.getSelectedToggle().getUserData();
        Predicate<Row> byStatus = r -> switch (which) {
            case "Reserved" -> r.status().equals("RESERVED");
            case "Booked" -> r.status().equals("BOOKED");
            case "Released" -> r.status().equals("RELEASED") || r.status().equals("EXPIRED");
            default -> true;
        };
        Predicate<Row> bySearch = r -> q.isEmpty() || r.seat().toLowerCase().contains(q)
                || (r.passenger() != null && r.passenger().toLowerCase().contains(q));
        filtered.setPredicate(byStatus.and(bySearch));                                      // combine predicates
    }

    private void applySort() {
        sorted.setComparator(switch (sortBox.getValue()) {
            case "Oldest first" -> Comparator.comparingLong(Row::time);
            case "Seat number" -> Comparator.comparingInt(r -> Integer.parseInt(r.seat().substring(1)));
            default -> Comparator.comparingLong(Row::time).reversed();
        });
    }

    private void refresh() {
        rows.clear();
        for (BookingEvent e : service.getHistory()) rows.add(Row.of(e));
    }

    private void updateSummary() { updateSummary(null); }

    private void updateSummary(String selectedNode) {
        long reserved = rows.stream().filter(r -> r.status().equals("RESERVED")).count();
        long booked = rows.stream().filter(r -> r.status().equals("BOOKED")).count();
        long freed = rows.stream().filter(r -> r.status().equals("RELEASED") || r.status().equals("EXPIRED")).count();
        summary.setText(filtered.size() + " shown of " + rows.size() + " events   |   reserved " + reserved
                + "   booked " + booked + "   released/expired " + freed
                + (selectedNode != null ? "   |   selected: " + selectedNode : ""));
    }

    /** Writes the visible list to a file with a byte stream (Unit 3: FileOutputStream). */
    private void exportActivity() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("activity.txt");
        File f = fc.showSaveDialog(stage);
        if (f == null) return;
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(f))) {
            for (Row r : sorted) {
                String line = r.time() + "," + r.seat() + "," + r.passenger() + "," + r.status() + "\n";
                out.write(line.getBytes(StandardCharsets.UTF_8));
            }
            new Alert(Alert.AlertType.INFORMATION, "Saved " + sorted.size() + " rows to " + f.getName()).showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Could not save: " + ex.getMessage()).showAndWait();
        }
    }

    private void runStress() {
        service.findAvailableSeatId().ifPresent(seat -> {
            Thread t = new Thread(() -> service.runContentionTest(seat, 10), "desk-stress");
            t.setDaemon(true);
            t.start();
        });
    }
}
