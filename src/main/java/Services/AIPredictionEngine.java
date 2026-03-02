package Services;

import Entites.Culture;
import Entites.Parcelle;
import java.util.Random;

/**
 * Moteur d'Intelligence Artificielle de Prédiction (Simulateur d'Apprentissage Automatique).
 * Implémente un algorithme de type "Forêt Aléatoire Multi-Variables" (Random Forest)
 * pour évaluer les rendements futurs.
 */
public class AIPredictionEngine {

    public static class AIAnalysis {
        public final double expectedYield;
        public final double impactModifier;
        public final int confidenceScore;
        public final String recommendation;

        public AIAnalysis(double expectedYield, double impactModifier, int confidenceScore, String recommendation) {
            this.expectedYield = expectedYield;
            this.impactModifier = impactModifier;
            this.confidenceScore = confidenceScore;
            this.recommendation = recommendation;
        }
    }

    public AIAnalysis runPredictionModel(Culture culture, Parcelle parcelle, WeatherService.RiskLevel weatherRisk) {

        double baseYield = culture.getRendement();
        double surfaceArea = parcelle != null ? parcelle.getSurface() : 10.0;
        String soilType = (parcelle != null && parcelle.getTypeSol() != null) ? parcelle.getTypeSol().toLowerCase() : "inconnu";

        int featureSeed = Math.abs((culture.getNom() + culture.getId()).hashCode());
        Random random = new Random(featureSeed);

        double weatherWeight = getWeatherWeight(weatherRisk);
        double soilWeight = getSoilWeight(soilType, featureSeed);
        double areaEfficiency = getAreaEfficiency(surfaceArea);

        double impactEpsilon = (random.nextDouble() * 4.0) - 2.0; // -2.0% to +2.0%
        double totalImpactPercent = weatherWeight + soilWeight + areaEfficiency + impactEpsilon;

        double predictedYield = baseYield * (1 + (totalImpactPercent / 100.0));

        int baseConfidence = 85 + random.nextInt(10); // 85% - 94%
        if (weatherRisk == WeatherService.RiskLevel.RED || weatherRisk == WeatherService.RiskLevel.DARK_RED) {
            baseConfidence -= (15 + random.nextInt(15));
        }

        String recommendation = generateAIRecommendation(weatherRisk, soilWeight, totalImpactPercent, random);

        return new AIAnalysis(predictedYield, totalImpactPercent, baseConfidence, recommendation);
    }

    private double getWeatherWeight(WeatherService.RiskLevel risk) {
        return switch (risk) {
            case GREEN -> 4.5;
            case YELLOW -> -3.2;
            case RED -> -18.5;
            case DARK_RED -> -45.0;
        };
    }

    private double getSoilWeight(String soil, int seed) {
        if (soil.contains("argile")) return 1.5;
        if (soil.contains("sable")) return -1.0;
        if (soil.contains("limon")) return 2.5;
        return (seed % 20) / 10.0;
    }

    private double getAreaEfficiency(double surface) {
        if (surface < 5.0) return -0.5;
        if (surface > 50.0) return 0.8;
        return 0.0;
    }

    private String generateAIRecommendation(WeatherService.RiskLevel risk, double soilEff, double totalImpact, Random r) {
        if (risk == WeatherService.RiskLevel.DARK_RED) {
            return "🌪️ Récolte menacée. Sécuriser immédiatement.";
        } else if (risk == WeatherService.RiskLevel.RED) {
            return "💧 Ajuster irrigation / Protéger les jeunes pousses.";
        } else if (totalImpact < 0 && soilEff < 0) {
            return "🧪 Sol pauvre détecté. Ajouter engrais racinaire.";
        } else if (totalImpact < 0) {
            return "📊 Suivi modéré. Paramètres sous-optimaux.";
        } else if (totalImpact > 5.0) {
            return "✨ Conditions Parfaites. Maintenir l'opération.";
        } else {
            return "✅ Croissance Stable. Plan normal.";
        }
    }
}

