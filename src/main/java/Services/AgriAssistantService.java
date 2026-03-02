package Services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service Assistant Agrole - Mode secours quand OpenAI n'est pas disponible
 */
public class AgriAssistantService {
    
    private final Random random = new Random();
    
    // Base de connaissances agricoles
    private final Map<String, String[]> knowledgeBase;
    
    public AgriAssistantService() {
        knowledgeBase = new HashMap<>();
        initializeKnowledgeBase();
    }
    
    private void initializeKnowledgeBase() {
        // Tomates
        knowledgeBase.put("tomate", new String[]{
            "Les tomates se plantent en Tunisie de février à avril pour une récolte de mai à juillet.",
            "Pour les tomates : espacez les plants de 50cm, arrosez régulièrement mais sans excès, et paillez le sol.",
            "Les tomates aiment le soleil et un sol riche en matière organique. Ajoutez du compost avant la plantation."
        });
        
        // Pucerons
        knowledgeBase.put("puceron", new String[]{
            "Contre les pucerons : pulvérisez une solution de savon noir (1 cuillère pour 1L d'eau) ou de l'eau avec quelques gouttes de liquide vaisselle.",
            "Les pucerons peuvent être éliminés naturellement avec des feuilles de tomate macérées dans l'eau pendant 24h.",
            "Plantez de l'ail ou des œillets d'Inde près de vos cultures pour repousser les pucerons naturellement."
        });
        
        // Arrosage
        knowledgeBase.put("arrosage", new String[]{
            "En été, arrosez vos légumes tôt le matin ou tard le soir pour éviter l'évaporation.",
            "La fréquence d'arrosage dépend du sol : sol argileux tous les 3-4 jours, sol sableux tous les 1-2 jours.",
            "Vérifiez l'humidité du sol en enfonçant votre doigt à 2-3cm de profondeur avant d'arroser."
        });
        
        // Engrais NPK
        knowledgeBase.put("engrais", new String[]{
            "Pour les cultures maraîchères, utilisez un engrais NPK équilibré 15-15-15 au début de la croissance.",
            "L'azote (N) favorise les feuilles, le phosphore (P) les racines et fleurs, le potassium (K) les fruits.",
            "Appliquez l'engrais NPK toutes les 3-4 semaines pendant la période de croissance active."
        });
        
        // Nouveaux sujets agricoles variés
        
        // Pommes de terre
        knowledgeBase.put("pomme de terre", new String[]{
            "Les pommes de terre se plantent en Tunisie de janvier à mars, à 10cm de profondeur et 30cm d'écart.",
            "Buttez les plants de pommes de terre lorsque les feuilles atteignent 20cm pour protéger les tubercules.",
            "Récoltez les pommes de terre 90-120 jours après plantation quand le feuillage jaunit."
        });
        
        // Oliviers
        knowledgeBase.put("olivier", new String[]{
            "Les oliviers en Tunisie se taillent après la récolte (mars-avril) pour éliminer les branches mortes.",
            "Un olivier adulte a besoin de 20-30L d'eau par semaine pendant la saison chaude.",
            "La récolte des olives se fait d'octobre à décembre selon la variété et la région."
        });
        
        // Agrumes
        knowledgeBase.put("agrumes", new String[]{
            "Les agrumes (oranges, citrons) se plantent au printemps avec un espacement de 4-5m entre les arbres.",
            "Protégez les jeunes agrumes du gel en hiver avec un paillage épais au pied.",
            "Fertilisez les agrumes trois fois par an : début printemps, début été et début automne."
        });
        
        // Maladies
        knowledgeBase.put("maladie", new String[]{
            "La prévention des maladies : rotation des cultures, bonne aération, et surveillance régulière.",
            "Pour le mildiou : traitez avec du cuivre (bouillie bordelaise) par temps humide.",
            "L'oïdium se traite avec du soufre pulvérisé dès l'apparition des taches blanches."
        });
        
        // Sol
        knowledgeBase.put("sol", new String[]{
            "Un bon sol agricole doit être meuble, riche en matière organique et bien drainé.",
            "Testez votre sol : pH entre 6 et 7 pour la plupart des légumes.",
            "Amendez le sol avec du compost chaque automne pour maintenir sa fertilité."
        });
        
        // Semences
        knowledgeBase.put("semence", new String[]{
            "Conservez les semences dans un endroit sec, frais et à l'abri de la lumière.",
            "Les semences de légumes restent viables 3-5 ans en moyenne.",
            "Faites des tests de germination avant de semer de vieilles semences."
        });
        
        // Irrigation
        knowledgeBase.put("irrigation", new String[]{
            "L'irrigation goutte à goutte est la plus économe en eau pour les cultures maraîchères.",
            "Calculez vos besoins : 5-8L d'eau par m² par jour pour les légumes en été.",
            "Privilégiez l'irrigation matinale pour réduire les maladies fongiques."
        });
        
        // Culture bio
        knowledgeBase.put("bio", new String[]{
            "En agriculture bio, utilisez le compost, le fumier bien décomposé et les engrais verts.",
            "La lutte intégrée contre les ravageurs combine prévention, observation et traitements naturels.",
            "La rotation des cultures sur 4 ans est essentielle en bio pour préserver la fertilité."
        });
        
        // Serre
        knowledgeBase.put("serre", new String[]{
            "Une serre permet de cultiver toute l'année et de protéger les cultures des intempéries.",
            "Aérez votre serre quotidiennement pour éviter l'excès d'humidité et les maladies.",
            "En hiver, une serre peut réduire les besoins en arrosage de 50%."
        });
        
        // Récolte
        knowledgeBase.put("recolte", new String[]{
            "Récoltez tôt le matin pour une meilleure conservation des légumes.",
            "La plupart des légumes se récoltent avant pleine maturité pour une meilleure conservation.",
            "Stockez les légumes-racines dans un endroit frais, sombre et humide."
        });
        
        // Général
        knowledgeBase.put("general", new String[]{
            "En agriculture tunisienne, adaptez toujours vos pratiques au climat méditerranéen : étés chauds et hivers doux.",
            "La rotation des cultures est essentielle : évitez de planter la même famille au même endroit deux années de suite.",
            "Le paillage (mulching) conserve l'humidité du sol et réduit la croissance des mauvaises herbes."
        });
    }
    
    /**
     * Génère une réponse agricole basée sur la question de l'utilisateur
     */
    public String askAgriculturalQuestion(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Bonjour ! Je suis votre assistant agricole. Comment puis-je vous aider aujourd'hui ?";
        }
        
        String messageLower = userMessage.toLowerCase();
        
        // Rechercher dans la base de connaissances avec plus de flexibilité
        for (Map.Entry<String, String[]> entry : knowledgeBase.entrySet()) {
            String key = entry.getKey();
            if (messageLower.contains(key)) {
                String[] responses = entry.getValue();
                return responses[random.nextInt(responses.length)];
            }
        }
        
        // Réponses génériques selon le type de question avec plus de variété
        if (messageLower.contains("bonjour") || messageLower.contains("salut") || messageLower.contains("hello")) {
            String[] greetings = {
                "Bonjour ! Je suis votre assistant agricole AgriGo. Je peux vous aider avec les cultures, maladies, arrosage, engrais, et calendriers de plantation. Quelle est votre question ?",
                "Salut agriculteur ! Je suis là pour vous conseiller. N'hésitez pas à me poser vos questions sur l'agriculture tunisienne !",
                "Bonjour ! Passionné d'agriculture ? Je suis votre expert pour toutes vos questions sur les cultures, sols et techniques agricoles."
            };
            return greetings[random.nextInt(greetings.length)];
        }
        
        if (messageLower.contains("plant") || messageLower.contains("sem") || messageLower.contains("culture")) {
            String[] plantingTips = {
                "Pour la plantation en Tunisie, privilégiez le printemps (février-avril) pour la plupart des légumes. Adaptez toujours au climat local.",
                "La réussite d'une plantation dépend du sol, de la saison et de la variété. Quelles cultures souhaitez-vous planter ?",
                "En agriculture tunisienne, la période de plantation varie : légumes-feuilles en automne, légumes-fruits au printemps."
            };
            return plantingTips[random.nextInt(plantingTips.length)];
        }
        
        if (messageLower.contains("maladie") || messageLower.contains("mal") || messageLower.contains("traitement")) {
            String[] diseaseTips = {
                "La prévention est clé en agriculture : rotation des cultures, bon drainage, et surveillance régulière. Quel type de maladie ?",
                "Pour les maladies, identifiez d'abord le symptôme : taches, jaunissement, flétrissement... Ensuite traitez spécifiquement.",
                "En bio, la prévention des maladies passe par la biodiversité et la santé du sol. Quelles plantes sont concernées ?"
            };
            return diseaseTips[random.nextInt(diseaseTips.length)];
        }
        
        if (messageLower.contains("récolte") || messageLower.contains("recolt") || messageLower.contains("recolte")) {
            String[] harvestTips = {
                "Récoltez tôt le matin pour une meilleure conservation. La plupart des légumes se récoltent à maturité mais avant qu'ils ne soient trop mûrs.",
                "Le timing de récolte est crucial : trop tôt = manque de saveur, trop tard = perte de conservation. Quel légume ?",
                "En Tunisie, adaptez la récolte au climat : évitez les heures chaudes et préférez le matin."
            };
            return harvestTips[random.nextInt(harvestTips.length)];
        }
        
        if (messageLower.contains("eau") || messageLower.contains("irrig") || messageLower.contains("hydrat")) {
            String[] waterTips = {
                "L'eau est précieuse en agriculture tunisienne. Privilégiez l'irrigation goutte à goutte et arrosez tôt le matin.",
                "Les besoins en eau varient : 5-8L/m²/jour pour les légumes en été, moins au printemps et automne.",
                "L'irrigation doit être adaptée au sol : sableux = arrosages fréquents, argileux = arrosages espacés mais copieux."
            };
            return waterTips[random.nextInt(waterTips.length)];
        }
        
        if (messageLower.contains("engrais") || messageLower.contains("fertil") || messageLower.contains("npk")) {
            String[] fertilizerTips = {
                "Les engrais doivent être adaptés aux besoins des plantes : azote pour les feuilles, phosphore pour les racines, potassium pour les fruits.",
                "En agriculture bio, privilégiez le compost, le fumier et les engrais verts. En conventionnel, les NPK équilibrés.",
                "La fertilisation dépend du sol : faites une analyse de sol avant d'appliquer des engrais chimiques."
            };
            return fertilizerTips[random.nextInt(fertilizerTips.length)];
        }
        
        if (messageLower.contains("climat") || messageLower.contains("température") || messageLower.contains("saison")) {
            String[] climateTips = {
                "Le climat tunisien est méditerranéen : étés chauds et secs, hivers doux. Adaptez vos cultures à ces conditions.",
                "En été, protégez vos cultures du soleil ardent avec ombrage et paillage. En hiver, certaines cultures nécessitent une protection.",
                "Le changement climatique affecte l'agriculture : diversifiez vos cultures et adaptez les calendriers de plantation."
            };
            return climateTips[random.nextInt(climateTips.length)];
        }
        
        if (messageLower.contains("bio") || messageLower.contains("organique") || messageLower.contains("naturel")) {
            String[] organicTips = {
                "L'agriculture bio privilégie la prévention, la biodiversité et la fertilité naturelle du sol. Pas de produits chimiques de synthèse.",
                "En bio, utilisez le compost, les engrais verts, la rotation des cultures et les auxiliaires (insectes utiles).",
                "La conversion au bio demande 3 ans, mais offre des produits plus sains et préserve l'environnement."
            };
            return organicTips[random.nextInt(organicTips.length)];
        }
        
        // Réponses par défaut plus variées
        String[] defaultResponses = {
            "C'est une excellente question ! Pour des conseils plus précis, pourriez-vous me préciser le type de culture ou la période de l'année ?",
            "En agriculture tunisienne, chaque région a ses spécificités. Quelle est votre localisation pour des conseils adaptés ?",
            "Je vous recommande de consulter un agronome local pour des conseils personnalisés à votre terrain et votre climat.",
            "Pensez à adapter les conseils généraux à votre sol spécifique et aux conditions météo de votre région.",
            "L'agriculture est un domaine vaste ! Pourriez-vous me dire si vous êtes intéressé par les légumes, arbres fruitiers, ou céréales ?",
            "Excellent sujet agricole ! Selon votre expérience, êtes-vous débutant ou agriculteur confirmé ? Cela m'aidera à mieux vous conseiller.",
            "La réussite en agriculture dépend de nombreux facteurs : sol, climat, variétés, et techniques. Sur quel aspect souhaitez-vous vous concentrer ?"
        };
        
        return defaultResponses[random.nextInt(defaultResponses.length)];
    }
    
    /**
     * Teste si le service fonctionne
     */
    public boolean testConnection() {
        return true; // Toujours disponible en mode local
    }
    
    /**
     * Ferme le service
     */
    public void close() {
        // Rien à fermer
    }
}
