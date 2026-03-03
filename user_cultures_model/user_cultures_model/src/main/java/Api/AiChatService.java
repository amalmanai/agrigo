package Api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Stub neutre pour AiChatService — n'effectue aucun appel réseau.
 * Les méthodes retournent toujours CompletableFuture.completedFuture(null) pour éviter tout appel externe.
 * Conserver ce fichier permet de garder les signatures et d'éviter des erreurs de compilation si d'autres classes importent le service.
 */
public class AiChatService {

    /**
     * Stub : ne contacte pas OpenAI, retourne null pour indiquer l'absence de réponse externe.
     */
    public static CompletableFuture<String> askAgrigoAssistant(String conversationHistorique) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Stub streaming : appelle onDelta jamais et retourne null.
     */
    public static CompletableFuture<String> askAgrigoAssistantStream(String conversationHistorique, Consumer<String> onDelta) {
        // Ne pas invoquer onDelta — stub inoffensif
        return CompletableFuture.completedFuture(null);
    }
}
