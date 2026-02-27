package Controllers;

import Api.AiChatService;
import Api.TacheApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

public class ChatbotController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextArea inputField;

    @FXML
    private ScrollPane scrollPane;

    /** Historique complet pour des réponses longues type ChatGPT. */
    private final StringBuilder conversationHistory = new StringBuilder();

    @FXML
    public void initialize() {
        if (scrollPane != null && messagesContainer != null) {
            scrollPane.vvalueProperty().bind(messagesContainer.heightProperty());
        }
        if (messagesContainer != null) messagesContainer.getStyleClass().add("messages-container");

        // Charger dynamiquement la feuille de styles quand la scene est prête
        Platform.runLater(() -> {
            try {
                if (messagesContainer != null && messagesContainer.getScene() != null) {
                    String css = getClass().getResource("/css/chatbot.css").toExternalForm();
                    messagesContainer.getScene().getStylesheets().add(css);
                }
            } catch (Exception ignored) {}
        });

        // Charger automatiquement une citation Quotable au démarrage
        loadRandomQuote();
    }

    @FXML
    public void handleSend() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String cleaned = text.trim();
        addUserMessage(cleaned);
        inputField.clear();

        // Ajouter au contexte de conversation
        conversationHistory.append("Utilisateur : ").append(cleaned).append("\n");

        // Ajouter un label de bot vide (nous allons le remplir en streaming)
        final Label botLabel = addBotMessage("L’assistant AGRIGO prépare une réponse...");

        // Appel streaming (OpenAI) ; si pas de clé, la méthode complète avec null -> fallback
        CompletableFuture<String> fut = AiChatService.askAgrigoAssistantStream(conversationHistory.toString(), delta -> {
            // delta fragments peuvent arriver depuis un thread non-JavaFX
            Platform.runLater(() -> {
                // remplacer le texte d'attente par la première partie
                String cur = botLabel.getText();
                if (cur != null && cur.startsWith("L’assistant AGRIGO prépare")) {
                    botLabel.setText("");
                }
                // Append le delta
                botLabel.setText(botLabel.getText() + delta);
            });
        });

        fut.thenAccept(reply -> Platform.runLater(() -> {
            // Si l'API n'a pas répondu (null) -> utiliser le fallback local
            if (reply == null || reply.trim().isEmpty()) {
                String finalReply = buildReply(cleaned.toLowerCase());
                botLabel.setText(finalReply);
                conversationHistory.append("Assistant : ").append(finalReply).append("\n");
            } else {
                // reply contient la version finale ; si le streaming a déjà rempli le label, on peut garder
                botLabel.setText(reply.trim());
                conversationHistory.append("Assistant : ").append(reply.trim()).append("\n");
            }
        }));
    }

    private void loadRandomQuote() {
        TacheApiService.getRandomQuote().thenAccept(result -> {
            Platform.runLater(() -> {
                if (result == null || result.content == null || result.content.isEmpty()) {
                    addBotMessage("💡 Astuce AGRIGO : pensez à enregistrer vos tâches et interventions dès qu’elles sont planifiées, cela évite les oublis sur le terrain.");
                } else {
                    String author = (result.author != null && !result.author.isEmpty())
                            ? " — " + result.author
                            : "";
                    addBotMessage("💬 Citation du jour :\n\"" + result.content + "\"" + author);
                }
            });
        });
    }

    private String buildReply(String question) {
        // Problèmes techniques / erreurs
        if (question.contains("erreur") || question.contains("bug") || question.contains("ne marche pas")) {
            return "D’accord, vous rencontrez un problème technique.\n\n" +
                    "1) Dites-moi sur quel écran AGRIGO vous êtes (connexion, tâches, tableau de bord, etc.).\n" +
                    "2) Copiez le message d’erreur exact ou décrivez ce qui se bloque.\n" +
                    "3) Précisez ce que vous vouliez faire (par ex. ajouter une tâche, modifier un utilisateur…).\n\n" +
                    "Avec ces infos, un agent pourra reproduire et corriger le problème rapidement.";
        }

        // Tâches / main d’œuvre
        if (question.contains("tache") || question.contains("tâche")
                || question.contains("main d’œuvre") || question.contains("main oeuvre")
                || question.contains("ouvrier") || question.contains("ouvriers")) {
            return "Module tâches / main d’œuvre :\n\n" +
                    "• Pour voir les tâches : allez dans « Gestion Tâches » puis filtrez par statut ou par ouvrier.\n" +
                    "• Pour ajouter une tâche : bouton « Ajouter », remplissez la parcelle, la date, l’ouvrier et la description.\n" +
                    "• Pour modifier : sélectionnez la tâche dans la liste puis cliquez sur « Modifier ».\n\n" +
                    "Dites-moi ce que vous voulez faire précisément (ex : créer une nouvelle tâche pour un ouvrier) et je vous détaille les étapes.";
        }

        // Parcelles / cultures
        if (question.contains("parcelle") || question.contains("champ")
                || question.contains("culture") || question.contains("cultures")) {
            return "Gestion des parcelles / cultures :\n\n" +
                    "• Chaque parcelle regroupe une culture, une surface et, si besoin, un planning d’intervention.\n" +
                    "• Les tâches et l’irrigation peuvent ensuite être reliées à une parcelle.\n\n" +
                    "Dites-moi si vous voulez :\n" +
                    "1) Créer une nouvelle parcelle\n" +
                    "2) Consulter les interventions d’une parcelle\n" +
                    "3) Lier des tâches ou de l’irrigation à une parcelle.";
        }

        // Irrigation intelligente
        if (question.contains("irrigation") || question.contains("arrosage")
                || question.contains("eau") || question.contains("humidit")) {
            return "Module irrigation intelligente :\n\n" +
                    "• L’objectif est d’optimiser l’eau selon la culture et l’humidité du sol.\n\n" +
                    "• En général, on configure : la parcelle, le type de culture, les horaires d’arrosage et les seuils (humidité / météo).\n\n" +
                    "Indiquez-moi si vous voulez :\n" +
                    "- Configurer une nouvelle règle d’irrigation\n" +
                    "- Comprendre un indicateur (par ex. humidité, volume d’eau)\n" +
                    "- Résoudre un problème d’arrosage qui ne se lance pas.";
        }

        // Stocks agricoles
        if (question.contains("stock") || question.contains("engrais")
                || question.contains("semence") || question.contains("semis")
                || question.contains("intrant") || question.contains("intrants")) {
            return "Module stocks agricoles :\n\n" +
                    "• Vous pouvez enregistrer vos intrants (engrais, semences, produits) avec quantités et alertes de seuil.\n" +
                    "• À chaque utilisation, le stock est mis à jour pour suivre les entrées/sorties.\n\n" +
                    "Précisez si vous voulez :\n" +
                    "- Ajouter un nouveau produit au stock\n" +
                    "- Mettre à jour une quantité\n" +
                    "- Comprendre un indicateur ou une alerte de stock.";
        }

        // Ventes / récoltes
        if (question.contains("vente") || question.contains("vendre")
                || question.contains("récolte") || question.contains("recolte")
                || question.contains("production")) {
            return "Module ventes / récoltes :\n\n" +
                    "• Enregistrez vos récoltes (culture, quantité, date, parcelle) puis les ventes associées.\n\n" +
                    "• Les tableaux de bord vous montrent les volumes et revenus par culture ou période.\n\n" +
                    "Dites-moi si vous voulez :\n" +
                    "- Enregistrer une nouvelle récolte\n" +
                    "- Ajouter une vente\n" +
                    "- Lire un indicateur de performance (revenu, rendement).";
        }

        // Connexion / compte
        if (question.contains("connexion") || question.contains("connecter")
                || question.contains("mot de passe") || question.contains("login")) {
            return "Compte / connexion AGRIGO :\n\n" +
                    "• Vérifiez votre email et mot de passe sur l’écran de connexion.\n" +
                    "• En cas d’oubli, utilisez le lien « Mot de passe oublié ? » si disponible ou contactez l’administrateur.\n\n" +
                    "Expliquez-moi si vous :\n" +
                    "- Avez une erreur précise à la connexion\n" +
                    "- Avez perdu l’accès à votre compte\n" +
                    "- Voulez modifier vos informations (email, téléphone, rôle, photo).";
        }

        // Réponse par défaut
        return "Merci pour votre message.\n\n" +
                "Je suis l’assistant AGRIGO intégré. Pour bien vous aider :\n" +
                "1) Dites-moi sur quel module vous travaillez (tâches, parcelles, irrigation, stocks, ventes…).\n" +
                "2) Décrivez ce que vous essayez de faire.\n" +
                "3) S’il y a une erreur, copiez-la ici.\n\n" +
                "Je vous proposerai ensuite des étapes simples adaptées à votre cas, et pour les situations complexes un agent humain pourra prendre le relais.";
    }

    private void addUserMessage(String message) {
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
}
