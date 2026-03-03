package Services;

import Entites.Culture;
import Entites.Parcelle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YieldPredictionService {

    public static class PredictionResult {
        private final Culture culture;
        private final Parcelle parcelle;
        private final double rendementBase;
        private final double rendementPrevu;
        private final double impactPercent;
        private final int confidenceScore;
        private final String recommendation;

        public PredictionResult(Culture culture, Parcelle parcelle, double rendementBase, double rendementPrevu, double impactPercent, int confidenceScore, String recommendation) {
            this.culture = culture;
            this.parcelle = parcelle;
            this.rendementBase = rendementBase;
            this.rendementPrevu = rendementPrevu;
            this.impactPercent = impactPercent;
            this.confidenceScore = confidenceScore;
            this.recommendation = recommendation;
        }

        public Culture getCulture() { return culture; }
        public Parcelle getParcelle() { return parcelle; }
        public double getRendementBase() { return rendementBase; }
        public double getRendementPrevu() { return rendementPrevu; }
        public double getImpactPercent() { return impactPercent; }
        public int getConfidenceScore() { return confidenceScore; }
        public String getRecommendation() { return recommendation; }
    }

    public List<PredictionResult> predictYields(List<Culture> cultures, List<Parcelle> parcelles, WeatherService.RiskSummary weatherSummary) {
        List<PredictionResult> results = new ArrayList<>();
        if (cultures == null || cultures.isEmpty()) return results;

        Map<Integer, WeatherService.RiskLevel> riskMap =
                weatherSummary != null ? weatherSummary.getParcelleLevels() : Map.of();

        AIPredictionEngine aiEngine = new AIPredictionEngine();

        for (Culture c : cultures) {
            Parcelle parcelle = findParcelleById(parcelles, c.getIdParcelle());
            WeatherService.RiskLevel level = riskMap.get(c.getIdParcelle());

            if (level == null) {
                int code = Math.abs(((c.getNom() != null ? c.getNom() : "") + c.getId()).hashCode()) % 100;
                if (code < 40) level = WeatherService.RiskLevel.GREEN;
                else if (code < 70) level = WeatherService.RiskLevel.YELLOW;
                else if (code < 90) level = WeatherService.RiskLevel.RED;
                else level = WeatherService.RiskLevel.DARK_RED;
            }

            AIPredictionEngine.AIAnalysis analysis = aiEngine.runPredictionModel(c, parcelle, level);

            double base = c.getRendement();
            double impact = Math.round(analysis.impactModifier * 100.0) / 100.0;
            double prevu = Math.round(analysis.expectedYield * 100.0) / 100.0;

            results.add(new PredictionResult(c, parcelle, base, prevu, impact, analysis.confidenceScore, analysis.recommendation));
        }

        return results;
    }

    private Parcelle findParcelleById(List<Parcelle> parcelles, int id) {
        if (parcelles == null) return null;
        return parcelles.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }
}

