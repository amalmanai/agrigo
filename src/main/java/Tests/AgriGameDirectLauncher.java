package Tests;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AgriGameDirectLauncher extends Application {

    private static final int ROWS = 3, COLS = 3;
    private final Cell[][] cells = new Cell[ROWS][COLS];
    private int day = 1;
    private Cell selected = null;

    private Label dayLabel = new Label("Jour 1");
    private Label selectedLabel = new Label("Aucune parcelle sélectionnée");
    private TextArea logArea = new TextArea();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        HBox top = new HBox(8);
        Label title = new Label("🎮 Jeu AGRI (Direct)");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        top.getChildren().addAll(title, dayLabel);
        top.setPadding(new Insets(6));
        root.setTop(top);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Cell cell = new Cell(r, c);
                cells[r][c] = cell;
                Button b = new Button("🟫\nVide");
                b.setPrefSize(120, 90);
                int rr = r, cc = c;
                b.setOnAction(e -> selectCell(cells[rr][cc]));
                cell.button = b;
                grid.add(b, c, r);
            }
        }
        root.setCenter(grid);

        // right controls
        VBox right = new VBox(8);
        right.setPadding(new Insets(12));
        Button plantBtn = new Button("Planter");
        Button waterBtn = new Button("Arroser");
        Button harvestBtn = new Button("Récolter");
        Button advanceBtn = new Button("Avancer d'un jour");
        Button adviceBtn = new Button("Conseil IA");
        Button resetBtn = new Button("Réinitialiser");

        plantBtn.setOnAction(e -> plant());
        waterBtn.setOnAction(e -> water());
        harvestBtn.setOnAction(e -> harvest());
        advanceBtn.setOnAction(e -> advanceDay());
        adviceBtn.setOnAction(e -> giveAdvice());
        resetBtn.setOnAction(e -> resetGame());

        logArea.setEditable(false);
        logArea.setPrefRowCount(10);

        right.getChildren().addAll(new Label("Contrôles"), plantBtn, waterBtn, harvestBtn, advanceBtn, adviceBtn, resetBtn, new Label("Journal"), logArea, selectedLabel);
        root.setRight(right);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("AgriGame Direct");
        stage.show();

        log("Jeu AGRI (mode direct) prêt.");
        updateUI();
    }

    private void selectCell(Cell cell) {
        selected = cell;
        selectedLabel.setText(String.format("Parcelle [%d,%d] - %s", cell.row+1, cell.col+1, cellStateText(cell)));
        updateUI();
    }

    private String cellStateText(Cell cell) {
        if (!cell.planted) return "vide";
        String s = "planté (j=" + cell.days + ")";
        if (cell.ready) s = "prêt à récolter";
        if (cell.watered) s += ", arrosé";
        return s;
    }

    private void plant() {
        if (selected == null) { log("Sélectionnez d'abord une parcelle."); return; }
        if (selected.planted) { log("La parcelle est déjà plantée."); return; }
        selected.planted = true; selected.days = 0; selected.watered = false; selected.ready = false;
        log(String.format("Vous avez planté la parcelle [%d,%d].", selected.row+1, selected.col+1));
        updateUI();
    }

    private void water() {
        if (selected == null) { log("Sélectionnez d'abord une parcelle."); return; }
        if (!selected.planted) { log("Rien à arroser sur une parcelle vide."); return; }
        selected.watered = true;
        log(String.format("Vous avez arrosé la parcelle [%d,%d].", selected.row+1, selected.col+1));
        updateUI();
    }

    private void harvest() {
        if (selected == null) { log("Sélectionnez d'abord une parcelle."); return; }
        if (!selected.planted) { log("Rien à récolter."); return; }
        if (!selected.ready) { log("La parcelle n'est pas encore prête à la récolte."); return; }
        selected.planted = false; selected.days = 0; selected.watered = false; selected.ready = false;
        log(String.format("Récolte réussie sur [%d,%d] — bravo !", selected.row+1, selected.col+1));
        updateUI();
    }

    private void advanceDay() {
        day++;
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            Cell cell = cells[r][c];
            if (cell.planted) {
                cell.days++;
                if (cell.watered && cell.days >= 2) cell.ready = true;
                else if (cell.days >= 3) cell.ready = true;
                cell.watered = false;
            }
        }
        log("Journée avancée. Jour " + day + ".");
        updateUI();
    }

    private void giveAdvice() {
        int planted=0, ready=0, watered=0;
        for (int r=0;r<ROWS;r++) for (int c=0;c<COLS;c++) {
            Cell cell = cells[r][c]; if (cell.planted) planted++; if (cell.ready) ready++; if (cell.watered) watered++; }
        String advice;
        if (ready>0) advice = ready + " parcelle(s) prêtes : récoltez-les.";
        else if (planted==0) advice = "Aucune parcelle plantée.";
        else if (watered < planted) advice = "Arrosez vos cultures.";
        else advice = "Tout va bien.";
        log("Conseil IA : " + advice);
    }

    private void resetGame() {
        day = 1; selected = null; for (int r=0;r<ROWS;r++) for (int c=0;c<COLS;c++) cells[r][c] = new Cell(r,c);
        logArea.clear(); log("Jeu réinitialisé."); updateUI();
    }

    private void updateUI() {
        dayLabel.setText("Jour " + day);
        for (int r=0;r<ROWS;r++) for (int c=0;c<COLS;c++) {
            Cell cell = cells[r][c]; Button b = cell.button;
            if (!cell.planted) { b.setText("🟫\nVide"); b.setStyle("-fx-background-color:#efefef; -fx-font-size:16px;"); }
            else if (cell.ready) { b.setText("🌾\nRécolte"); b.setStyle("-fx-background-color:#fbe7a3; -fx-font-size:16px;"); }
            else { b.setText("🌱\nJ" + cell.days); b.setStyle("-fx-background-color:#dff7df; -fx-font-size:16px;"); }
            if (cell == selected) b.setStyle(b.getStyle() + " -fx-border-color:#228B22; -fx-border-width:3;");
        }
        if (selected == null) selectedLabel.setText("Aucune parcelle sélectionnée");
        else selectedLabel.setText(String.format("Parcelle [%d,%d] - %s", selected.row+1, selected.col+1, cellStateText(selected)));
    }

    private void log(String msg) { logArea.appendText(msg + "\n"); }

    private static class Cell {
        final int row, col; boolean planted=false; int days=0; boolean watered=false; boolean ready=false; Button button;
        Cell(int r, int c) { row = r; col = c; button = new Button("🟫\nVide"); button.setPrefSize(120,90); }
    }

    public static void main(String[] args) { launch(args); }
}

