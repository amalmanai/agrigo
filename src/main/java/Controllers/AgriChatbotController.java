package Controllers;

import Services.AgriAssistantService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgriChatbotController {

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton, clearButton, backButton;
    @FXML private Label statusLabel;
    @FXML private Button btnQuestion1, btnQuestion2, btnQuestion3, btnQuestion4, btnQuestion5, btnQuestion6;

    private AgriAssistantService agriService;
    private boolean isProcessing = false;

    /** Détermine si on revient vers admin ou user */
    private String returnTo = "user"; // "admin" ou "user"

    public void setReturnTo(String returnTo) {
        if(returnTo != null) this.returnTo = returnTo;
    }

    @FXML
    public void initialize() {
        agriService = new AgriAssistantService();

        if(statusLabel != null){
            statusLabel.setText("🟢 En ligne (Mode Local)");
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
        }

        addBotMessage("🌱 Bonjour ! Je suis votre assistant agricole AgriGo. Je peux vous aider avec :\n\n" +
                "• Conseils de plantation et récolte\n" +
                "• Traitement des maladies et ravageurs\n" +
                "• Fréquence d'arrosage\n" +
                "• Types d'engrais NPK\n" +
                "• Calendriers agricoles tunisiens\n\n" +
                "Comment puis-je vous aider aujourd'hui ?");

        // Actions des boutons
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
        clearButton.setOnAction(e -> clearChat());
        backButton.setOnAction(e -> goBack());

        btnQuestion1.setOnAction(e -> sendQuickMessage("Quand est le meilleur moment pour planter les tomates en Tunisie ?"));
        btnQuestion2.setOnAction(e -> sendQuickMessage("Comment traiter naturellement les pucerons sur mes plants ?"));
        btnQuestion3.setOnAction(e -> sendQuickMessage("À quelle fréquence devrais-je arroser mes légumes en été ?"));
        btnQuestion4.setOnAction(e -> sendQuickMessage("Quel type d'engrais NPK recommandez-vous pour les cultures maraîchères ?"));
        btnQuestion5.setOnAction(e -> sendQuickMessage("Comment planter et cultiver les pommes de terre ?"));
        btnQuestion6.setOnAction(e -> sendQuickMessage("Quels sont les principes de l'agriculture biologique ?"));

        // Scroll automatique
        if(chatMessagesContainer != null && chatScrollPane != null){
            chatMessagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> chatScrollPane.setVvalue(1.0));
        }

        // Apply dark mode after scene is ready
        if (Controlles.DashBoardController.preferredDarkMode) {
            Platform.runLater(() -> {
                if (sendButton != null && sendButton.getScene() != null) {
                    applyDarkToNode(sendButton.getScene().getRoot());
                }
            });
        }
    }

    private void applyDarkToNode(javafx.scene.Node node) {
        final String DARK_BG = "#1a202c";
        final String DARK_PANEL = "#2d3748";
        final String LIGHT_TEXT = "#e2e8f0";

        String style = node.getStyle();
        if (style != null && !style.isEmpty()) {
            if (style.contains("-fx-background-color: white") || style.contains("-fx-background-color: #f8faf8")) {
                node.setStyle(style.replace("white", DARK_PANEL).replace("#f8faf8", DARK_BG));
            } else if (style.contains("-fx-background-color: #f3f4f6")) {
                node.setStyle(style.replace("#f3f4f6", "#374151"));
            }
        }
        if (node instanceof javafx.scene.control.Labeled) {
            javafx.scene.control.Labeled lbl = (javafx.scene.control.Labeled) node;
            String s = lbl.getStyle() == null ? "" : lbl.getStyle();
            if (!s.contains("-fx-text-fill: white") && !s.contains("-fx-text-fill: #16a34a")
                    && !s.contains("-fx-text-fill: #1a5c1a") && !s.contains("-fx-text-fill: #228B22")) {
                lbl.setStyle((s.isEmpty() ? "" : s + " ") + "-fx-text-fill: " + LIGHT_TEXT + ";");
            }
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyDarkToNode(child);
            }
        }
    }

    private void sendMessage() {
        if(isProcessing || messageField == null) return;

        String userMessage = messageField.getText().trim();
        if(userMessage.isEmpty()) return;

        addUserMessage(userMessage);
        messageField.clear();

        isProcessing = true;
        if(sendButton != null) sendButton.setDisable(true);
        if(statusLabel != null){
            statusLabel.setText("🟡 Traitement...");
            statusLabel.setStyle("-fx-text-fill: #f59e0b;");
        }

        new Thread(() -> {
            try {
                String botResponse = agriService.askAgriculturalQuestion(userMessage);
                Platform.runLater(() -> {
                    addBotMessage(botResponse);
                    isProcessing = false;
                    if(sendButton != null) sendButton.setDisable(false);
                    updateStatusOnline();
                });
            } catch(Exception e){
                Platform.runLater(() -> {
                    addBotMessage("Désolé, une erreur s'est produite. Veuillez réessayer.");
                    isProcessing = false;
                    if(sendButton != null) sendButton.setDisable(false);
                    updateStatusOnline();
                });
                System.err.println("Erreur lors du traitement du message: " + e.getMessage());
            }
        }).start();
    }

    private void updateStatusOnline(){
        if(statusLabel != null){
            statusLabel.setText("🟢 En ligne (Mode Local)");
            statusLabel.setStyle("-fx-text-fill: #16a34a;");
        }
    }

    private void sendQuickMessage(String message){
        if(messageField != null){
            messageField.setText(message);
            sendMessage();
        }
    }

    private void addUserMessage(String message){
        if(chatMessagesContainer != null){
            chatMessagesContainer.getChildren().add(createUserMessageBubble(message));
        }
    }

    private void addBotMessage(String message){
        if(chatMessagesContainer != null){
            chatMessagesContainer.getChildren().add(createBotMessageBubble(message));
        }
    }

    private HBox createUserMessageBubble(String message){
        HBox hbox = new HBox();
        hbox.setStyle("-fx-alignment: center-right; -fx-padding: 5 0;");
        VBox bubble = new VBox();
        bubble.setStyle("-fx-background-color: #228B22; -fx-background-radius: 15 15 5 15; -fx-padding: 12 16; -fx-max-width: 400;");
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11;");
        bubble.getChildren().addAll(msgLabel, timeLabel);
        hbox.getChildren().add(bubble);
        return hbox;
    }

    private HBox createBotMessageBubble(String message){
        HBox hbox = new HBox();
        hbox.setStyle("-fx-alignment: center-left; -fx-padding: 5 0;");
        VBox bubble = new VBox();
        bubble.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 15 15 15 5; -fx-padding: 12 16; -fx-max-width: 400;");
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 14;");
        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11;");
        bubble.getChildren().addAll(msgLabel, timeLabel);
        hbox.getChildren().add(bubble);
        return hbox;
    }

    private String getCurrentTime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void clearChat(){
        if(chatMessagesContainer != null){
            chatMessagesContainer.getChildren().clear();
            addBotMessage("Conversation effacée. Comment puis-je vous aider ?");
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

            Stage stage = null;
            if(backButton != null && backButton.getScene()!=null && backButton.getScene().getWindow() instanceof Stage){
                stage = (Stage) backButton.getScene().getWindow();
            } else if(chatScrollPane != null && chatScrollPane.getScene()!=null && chatScrollPane.getScene().getWindow() instanceof Stage){
                stage = (Stage) chatScrollPane.getScene().getWindow();
            }

            if(stage != null){
                Scene scene = new Scene(root);
                try {
                    java.net.URL css = getClass().getResource("/app.css");
                    if (css != null) scene.getStylesheets().add(css.toExternalForm());
                } catch (Exception ignored) {}
                stage.setScene(scene);
                stage.setTitle("AgriGo");
                stage.show();
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible de trouver la fenêtre principale.").showAndWait();
            }
        } catch(IOException e){
            System.err.println("Erreur lors du retour: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, "Impossible de revenir à l'écran principal.").showAndWait();
        }
    }

    /** Nettoyage du service */
    public void cleanup(){
        if(agriService != null) agriService.close();
    }
}