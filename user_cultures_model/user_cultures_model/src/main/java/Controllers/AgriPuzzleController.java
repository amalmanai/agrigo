package Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgriPuzzleController {

    @FXML private GridPane puzzleGrid;
    @FXML private Button shuffleBtn, resetBtn, solveBtn, backBtn;
    @FXML private Label movesLabel;

    private static final int SIZE = 3;
    private final Button[][] tiles = new Button[SIZE][SIZE];
    private int emptyR = SIZE-1, emptyC = SIZE-1;
    private int moves = 0;
    private final String[] icons = new String[]{"🌾","🌱","🌻","🥕","🍅","🍀","🐝","🚜",""};
    private final Random random = new Random();

    /** Détermine si on revient vers admin ou user */
    private String returnTo = "user";
    public void setReturnTo(String returnTo){ this.returnTo = returnTo; }

    @FXML
    public void initialize() {
        buildGrid();
        if(shuffleBtn!=null) shuffleBtn.setOnAction(e->shuffle());
        if(resetBtn!=null) resetBtn.setOnAction(e->reset());
        if(solveBtn!=null) solveBtn.setOnAction(e->solve());
        if(backBtn!=null) backBtn.setOnAction(e->goBack());
        updateMovesLabel();

        // Dark mode
        if (Controlles.MainGuiAdminController.preferredDarkMode) {
            javafx.application.Platform.runLater(() -> {
                if (puzzleGrid != null && puzzleGrid.getScene() != null) {
                    applyDarkToNode(puzzleGrid.getScene().getRoot());
                }
            });
        }
    }

    private void buildGrid(){
        puzzleGrid.getChildren().clear();
        int index=0;
        for(int r=0;r<SIZE;r++){
            for(int c=0;c<SIZE;c++){
                Button b = new Button();
                b.setPrefSize(120,100);
                b.setStyle("-fx-font-size:28px; -fx-background-color:white; -fx-border-color:#d1d5db;");
                b.setText(icons[index++]);
                final int rr=r,cc=c;
                b.setOnAction(ev->handleTileClick(rr,cc));
                tiles[r][c]=b;
                puzzleGrid.add(b,c,r);
            }
        }
        emptyR = SIZE-1; emptyC = SIZE-1;
        setEmptyTileStyle(tiles[emptyR][emptyC]);
        moves=0; updateMovesLabel();
    }

    private void applyDarkToNode(javafx.scene.Node node) {
        final String DARK_BG = "#1a202c";
        final String DARK_PANEL = "#2d3748";
        final String DARK_TILE = "#3a4a5c";
        final String LIGHT_TEXT = "#e2e8f0";

        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            if (style.contains("-fx-background-color:white") || style.contains("-fx-background-color: white")) {
                node.setStyle(style.replace("-fx-background-color:white", "-fx-background-color:" + DARK_TILE)
                                   .replace("-fx-background-color: white", "-fx-background-color: " + DARK_TILE));
            } else if (style.contains("-fx-background-color: #f7faf6") || style.contains("-fx-background-color:#f7faf6")) {
                node.setStyle(style.replace("#f7faf6", DARK_BG));
            }
        }
        if (node instanceof javafx.scene.control.Labeled) {
            javafx.scene.control.Labeled lbl = (javafx.scene.control.Labeled) node;
            String s = lbl.getStyle() == null ? "" : lbl.getStyle();
            if (!s.contains("-fx-text-fill: white") && !s.contains("-fx-text-fill: #228B22")) {
                lbl.setStyle((s.isEmpty() ? "" : s + " ") + "-fx-text-fill: " + LIGHT_TEXT + ";");
            }
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyDarkToNode(child);
            }
        }
    }

    private void handleTileClick(int r,int c){
        if(isAdjacentToEmpty(r,c)){
            swapWithEmpty(r,c);
            moves++; updateMovesLabel();
            if(isSolved() && movesLabel!=null) movesLabel.setText("Gagné en " + moves + " coups !");
        }
    }

    private boolean isAdjacentToEmpty(int r,int c){
        int dr=Math.abs(r-emptyR), dc=Math.abs(c-emptyC);
        return (dr==1 && dc==0)||(dr==0 && dc==1);
    }

    private void swapWithEmpty(int r,int c){
        Button b = tiles[r][c];
        Button empty = tiles[emptyR][emptyC];
        String t=b.getText(); b.setText(empty.getText()); empty.setText(t);
        updateTileStyle(b); updateTileStyle(empty);
        emptyR=r; emptyC=c;
    }

    private void updateTileStyle(Button b){
        if(b.getText().isEmpty()) setEmptyTileStyle(b);
        else b.setStyle("-fx-font-size:28px; -fx-background-color:white; -fx-border-color:#d1d5db;");
    }

    private void setEmptyTileStyle(Button b){
        b.setStyle("-fx-background-color:#f3f4f6; -fx-border-color:#d1d5db; -fx-font-size:28px;");
    }

    private void shuffle(){
        int movesCount = 30+random.nextInt(40);
        for(int i=0;i<movesCount;i++){
            List<int[]> neighbors=getNeighbors(emptyR,emptyC);
            int[] pick=neighbors.get(random.nextInt(neighbors.size()));
            swapWithEmpty(pick[0],pick[1]);
        }
        moves=0; updateMovesLabel();
    }

    private List<int[]> getNeighbors(int r,int c){
        List<int[]> res=new ArrayList<>();
        if(r>0) res.add(new int[]{r-1,c});
        if(r<SIZE-1) res.add(new int[]{r+1,c});
        if(c>0) res.add(new int[]{r,c-1});
        if(c<SIZE-1) res.add(new int[]{r,c+1});
        return res;
    }

    private void reset(){ buildGrid(); }

    private void solve(){
        Timeline timeline=new Timeline();
        int total=SIZE*SIZE;
        for(int i=0;i<total;i++){
            final int idx=i;
            KeyFrame kf = new KeyFrame(Duration.millis(40*i), e->{
                int r=idx/SIZE, c=idx%SIZE;
                tiles[r][c].setText(icons[idx]);
                tiles[r][c].setStyle("-fx-font-size:28px; -fx-background-color:white; -fx-border-color:#d1d5db;");
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.setOnFinished(e->{ emptyR=SIZE-1; emptyC=SIZE-1; setEmptyTileStyle(tiles[emptyR][emptyC]); moves=0; updateMovesLabel(); });
        timeline.play();
    }

    private boolean isSolved(){
        int idx=0;
        for(int r=0;r<SIZE;r++) for(int c=0;c<SIZE;c++){
            String t=tiles[r][c].getText(); if(t==null)t="";
            if(!t.equals(icons[idx++])) return false;
        }
        return true;
    }

    private void updateMovesLabel(){ if(movesLabel!=null) movesLabel.setText("Coups: "+moves); }

    private void goBack(){
        try{
            String fxml = (Utils.Session.getCurrentUser() != null
                    && "admin".equalsIgnoreCase(Utils.Session.getCurrentUser().getRole_user()))
                    ? "/Dashboard.fxml"
                    : "/menu.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage)((backBtn!=null?backBtn.getScene().getWindow():puzzleGrid.getScene().getWindow()));
            Scene scene = new Scene(root);
            try {
                java.net.URL css = getClass().getResource("/app.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}
            stage.setScene(scene);
            stage.setTitle("Tableau de bord");
            stage.show();
        } catch(IOException e){
            System.err.println("AgriPuzzleController.goBack error: "+e.getMessage());
        }
    }
}