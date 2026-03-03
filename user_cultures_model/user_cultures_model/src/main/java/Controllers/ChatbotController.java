package Controllers;

import Api.TacheApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ChatbotController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextArea inputField;

    @FXML
    private ScrollPane scrollPane;

    /** Historique minimal si besoin côté UI. */
    private final StringBuilder conversationHistory = new StringBuilder();

    @FXML
    public void initialize() {
        if (scrollPane != null && messagesContainer != null) {
            scrollPane.vvalueProperty().bind(messagesContainer.heightProperty());
        }
        if (messagesContainer != null) messagesContainer.getStyleClass().add("messages-container");

        // Indiquer que le chatbot est désactivé
        Platform.runLater(() -> addBotMessage("L'assistant AGRIGO est actuellement désactivé dans cette version."));

        // Charger une astuce locale (toujours sans appel externe au service IA)
        loadLocalTip();
    }

    @FXML
    public void handleSend() {
        // Ne pas appeler d'API externe : afficher message inoffensif
        String text = inputField != null ? inputField.getText() : null;
        if (text == null || text.trim().isEmpty()) return;
        addUserMessage(text.trim());
        if (inputField != null) inputField.clear();
        addBotMessage("L'assistant est désactivé. Si vous avez besoin d'aide, contactez l'administrateur.");
    }

    private void loadLocalTip() {
        // Petit fallback local : astuce statique (aucun appel réseau)
        Platform.runLater(() -> addBotMessage("💡 Astuce AGRIGO : pensez à planifier vos interventions après chaque récolte pour suivre le rendement."));
    }

    private void addUserMessage(String message) {
        if (messagesContainer == null) return;
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.getStyleClass().addAll("message", "user");

        Label avatar = new Label("👤");
        avatar.getStyleClass().addAll("avatar", "user");

        HBox container = new HBox();
        container.getChildren().addAll(avatar, msg);
        container.getStyleClass().add("hbox-user");
        HBox.setHgrow(msg, Priority.ALWAYS);

        messagesContainer.getChildren().add(container);
    }

    private Label addBotMessage(String message) {
        if (messagesContainer == null) return new Label();
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.getStyleClass().addAll("message", "bot");

        Label avatar = new Label("💬");
        avatar.getStyleClass().addAll("avatar", "bot");

        HBox container = new HBox();
        container.getChildren().addAll(avatar, msg);
        container.getStyleClass().add("hbox-bot");
        HBox.setHgrow(msg, Priority.ALWAYS);

        messagesContainer.getChildren().add(container);
        return msg;
    }

    // Garder la méthode publique mais neutre si d'autres classes l'appellent
    public void greetCustomerService() {
        Platform.runLater(() -> addBotMessage("Assistant service client désactivé."));
    }
}
