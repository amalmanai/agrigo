package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AgriGameController {

    @FXML private GridPane grid;
    @FXML private Label dayLabel;
    @FXML private Button plantBtn, waterBtn, harvestBtn, advanceBtn, adviceBtn, puzzleBtn, backBtn, resetBtn;
    @FXML private TextArea logArea;
    @FXML private Label selectedLabel, scoreLabel;

    private static final int ROWS = 3, COLS = 3;
    private final Cell[][] cells = new Cell[ROWS][COLS];
    private int day = 1, score = 0;
    private Cell selected = null;

    /** Retour vers "admin" ou "user" */
    private String returnTo = "user";

    public void setReturnTo(String returnTo){ this.returnTo = returnTo; }

    @FXML
    public void initialize() {
        buildGrid();
        updateDayLabel();
        log("Jeu AGRI prêt. Bonne culture !");

        plantBtn.setOnAction(e -> plant());
        waterBtn.setOnAction(e -> water());
        harvestBtn.setOnAction(e -> harvest());
        advanceBtn.setOnAction(e -> advanceDay());
        adviceBtn.setOnAction(e -> giveAdvice());
        puzzleBtn.setOnAction(e -> openPuzzle());
        backBtn.setOnAction(e -> goBack());
        resetBtn.setOnAction(e -> resetGame());

        updateUI();

        // Apply dark mode after the scene graph is ready
        if (Controlles.DashBoardController.preferredDarkMode) {
            javafx.application.Platform.runLater(this::applyDarkModeStyles);
        }
    }

    private void applyDarkModeStyles() {
        final String DARK_BG = "#1a202c";
        final String DARK_PANEL = "#2d3748";
        final String DARK_BORDER = "#4a5568";
        final String LIGHT_TEXT = "#e2e8f0";
        final String MUTED_TEXT = "#94a3b8";

        // Change the root BorderPane background
        if (plantBtn != null && plantBtn.getScene() != null) {
            javafx.scene.Parent root = plantBtn.getScene().getRoot();
            root.setStyle("-fx-background-color: " + DARK_BG + ";");

            // Walk the scene graph and apply dark overrides
            applyDarkToNode(root, DARK_BG, DARK_PANEL, DARK_BORDER, LIGHT_TEXT, MUTED_TEXT);
        }
    }

    private void applyDarkToNode(javafx.scene.Node node, String bg, String panel, String border, String text, String muted) {
        String style = node.getStyle();
        if (style.contains("-fx-background-color: white") || style.contains("-fx-background-color: #f7faf6") || style.contains("-fx-background-color: #f8faf8")) {
            node.setStyle(style.replace("white", panel).replace("#f7faf6", bg).replace("#f8faf8", bg));
        }
        if (node instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region region = (javafx.scene.layout.Region) node;
            if (region.getStyle().contains("-fx-background-color: white") || (region.getStyle().isEmpty() && region instanceof javafx.scene.layout.VBox)) {
                // white panels -> dark panel
                String s = region.getStyle();
                if (s.contains("white")) {
                    region.setStyle(s.replace("white", panel));
                }
            }
        }
        if (node instanceof javafx.scene.control.Labeled) {
            javafx.scene.control.Labeled lbl = (javafx.scene.control.Labeled) node;
            String s = lbl.getStyle();
            if (!s.contains("-fx-text-fill: white") && !s.contains("-fx-text-fill: #228B22") && !s.contains("-fx-text-fill: #16a34a") && !s.contains("-fx-text-fill: #d97706")) {
                if (s.contains("-fx-text-fill: #1a3318") || s.contains("-fx-text-fill: #6b7280") || s.isEmpty()) {
                    lbl.setStyle((s.isEmpty() ? "" : s + " ") + "-fx-text-fill: " + text + ";");
                }
            }
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyDarkToNode(child, bg, panel, border, text, muted);
            }
        }
    }


    private void selectCell(Cell cell) {
        selected = cell;
        updateSelectedLabel();
        updateUI();
    }

    private void updateSelectedLabel(){
        if(selectedLabel != null){
            if(selected != null){
                selectedLabel.setText(String.format("Parcelle [%d,%d] - %s", selected.row+1, selected.col+1, cellStateText(selected)));
            } else {
                selectedLabel.setText("Aucune parcelle sélectionnée");
            }
        }
    }

    private String cellStateText(Cell cell){
        if(!cell.planted) return "vide";
        String s = "planté (j=" + cell.days + ")";
        if(cell.ready) s = "prêt à récolter";
        if(cell.watered) s += ", arrosé";
        return s;
    }

    private void plant(){
        if(selected==null){ log("Sélectionnez une parcelle."); return; }
        if(selected.planted){ log("Parcelle déjà plantée."); return; }
        selected.planted = true; selected.days=0; selected.watered=false; selected.ready=false;
        log(String.format("Parcelle [%d,%d] plantée.", selected.row+1, selected.col+1));
        updateUI();
    }

    private void water(){
        if(selected==null){ log("Sélectionnez une parcelle."); return; }
        if(!selected.planted){ log("Rien à arroser."); return; }
        selected.watered = true;
        log(String.format("Parcelle [%d,%d] arrosée.", selected.row+1, selected.col+1));
        updateUI();
    }

    private void harvest(){
        if(selected==null){ log("Sélectionnez une parcelle."); return; }
        if(!selected.planted){ log("Rien à récolter."); return; }
        if(!selected.ready){ log("Parcelle pas prête."); return; }
        selected.planted=false; selected.days=0; selected.watered=false; selected.ready=false;
        log(String.format("Récolte réussie sur [%d,%d] !", selected.row+1, selected.col+1));
        score++;
        updateUI();
    }

    private void advanceDay(){
        day++;
        for(int r=0;r<ROWS;r++) for(int c=0;c<COLS;c++){
            Cell cell = cells[r][c];
            if(cell.planted){
                cell.days++;
                if((cell.watered && cell.days>=2) || cell.days>=3) cell.ready=true;
                cell.watered=false;
            }
        }
        log("Journée avancée. Jour " + day + ".");
        updateDayLabel();
        updateUI();
    }

    private void giveAdvice(){
        int planted=0, ready=0, watered=0;
        for(int r=0;r<ROWS;r++) for(int c=0;c<COLS;c++){
            Cell cell=cells[r][c];
            if(cell.planted) planted++;
            if(cell.ready) ready++;
            if(cell.watered) watered++;
        }
        String advice;
        if(ready>0) advice = ready + " parcelle(s) prêtes : récoltez-les.";
        else if(planted==0) advice = "Aucune parcelle plantée : plantez quelques parcelles.";
        else if(watered<planted) advice = "Pensez à arroser vos cultures.";
        else advice = "Vos cultures poussent bien. Avancez d'un jour.";
        log("Conseil IA : " + advice);
    }

    private void openPuzzle(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgriPuzzle.fxml"));
            Parent root = loader.load();
            AgriPuzzleController puzzleController = loader.getController();
            if(puzzleController!=null) puzzleController.setReturnTo(returnTo);

            Stage stage = (Stage)((puzzleBtn!=null && puzzleBtn.getScene()!=null) ? puzzleBtn.getScene().getWindow() : grid.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setTitle("Agri Puzzle");
            stage.show();
        }catch(IOException e){
            log("Impossible d'ouvrir le puzzle.");
            System.err.println("AgriGameController.openPuzzle error: "+e.getMessage());
        }
    }

    private void goBack(){
        try{
            String fxml = (Utils.Session.getCurrentUser() != null
                    && "admin".equalsIgnoreCase(Utils.Session.getCurrentUser().getRole_user()))
                    ? "/Dashboard.fxml"
                    : "/menu.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage)((backBtn!=null && backBtn.getScene()!=null) ? backBtn.getScene().getWindow() : grid.getScene().getWindow());
            if(stage!=null){
                Scene scene = new Scene(root);
                try {
                    java.net.URL css = getClass().getResource("/app.css");
                    if (css != null) scene.getStylesheets().add(css.toExternalForm());
                } catch (Exception ignored) {}
                stage.setScene(scene);
                stage.setTitle("Tableau de bord");
                stage.show();
            } else {
                log("Impossible de trouver la fenêtre pour revenir.");
            }
        }catch(IOException e){
            log("Impossible de revenir à l'écran précédent.");
            System.err.println("AgriGameController.goBack error: "+e.getMessage());
        }
    }

    private void updateDayLabel(){ if(dayLabel!=null) dayLabel.setText("Jour "+day); }

    private void updateUI(){
        if(cells==null) return;
        if(scoreLabel!=null) scoreLabel.setText("Score: "+score);

        for(int r=0;r<ROWS;r++) for(int c=0;c<COLS;c++){
            Cell cell = cells[r][c];
            if(cell==null || cell.button==null) continue;
            Button b = cell.button;

            if(!cell.planted){
                b.setText("🟫\nVide");
                b.setStyle("-fx-background-color:#efefef; -fx-font-size:16px;");
            } else if(cell.ready){
                b.setText("🌾\nRécolte");
                b.setStyle("-fx-background-color:#fbe7a3; -fx-font-size:16px;");
            } else {
                b.setText("🌱\nJ"+cell.days);
                b.setStyle("-fx-background-color:#dff7df; -fx-font-size:16px;");
            }

            if(cell==selected) b.setStyle(b.getStyle()+"-fx-border-color:#228B22; -fx-border-width:3;");
        }

        if(plantBtn!=null) plantBtn.setDisable(selected==null || selected.planted);
        if(waterBtn!=null) waterBtn.setDisable(selected==null || !selected.planted || selected.ready);
        if(harvestBtn!=null) harvestBtn.setDisable(selected==null || !selected.planted || !selected.ready);
    }

    private void log(String msg){ if(logArea!=null) logArea.appendText(msg+"\n"); }

    private void buildGrid(){
        grid.getChildren().clear();
        for(int r=0;r<ROWS;r++) for(int c=0;c<COLS;c++){
            Cell cell = new Cell(r,c);
            cells[r][c] = cell;
            Button b = new Button("🟫");
            b.setPrefSize(140,120); b.setWrapText(true);
            int rr=r, cc=c;
            b.setOnAction(e -> selectCell(cells[rr][cc]));
            cell.button = b;
            grid.add(b,c,r);
        }
    }

    private void resetGame(){
        day=1; score=0; selected=null;
        for(int r=0;r<ROWS;r++) for(int c=0;c<COLS;c++) cells[r][c]=null;
        buildGrid();
        updateDayLabel();
        if(logArea!=null) logArea.clear();
        log("Jeu réinitialisé. Bonne partie !");
        updateUI();
        updateSelectedLabel();
    }

    private static class Cell{
        final int row,col;
        boolean planted=false; int days=0; boolean watered=false; boolean ready=false;
        Button button;
        Cell(int r,int c){ row=r; col=c; }
    }
}