package Controlles;

import Entites.Parcelle;
import Services.ParcelleCRUD;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ParcelleController {

    @FXML private Label lblTitle;
    @FXML private Button btnSave;
    @FXML private TextField tfNomP;
    @FXML private TextField tfSurface;
    @FXML private TextField tfAdresse;
    @FXML private ComboBox<String> cbTypeSol;
    @FXML private WebView mapView;

    private Parcelle parcelleToEdit;
    private static final int MAX_NOM_LENGTH = 80;
    private static final double MAX_SURFACE = 1_000_000d;
    private static final String NOM_REGEX = "^[\\p{L}][\\p{L}\\s'\\-]{1,79}$";

    private static final String GEOCODING_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "AGRI-GO-Application";

    private Double selectedLatitude;
    private Double selectedLongitude;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        cbTypeSol.getItems().setAll(
                "Argileux",
                "Sableux",
                "Limoneux",
                "Calcaire",
                "Humifere"
        );
        cbTypeSol.setEditable(true);
        setupMap();
    }

    @FXML
    void saveParcelle(ActionEvent event) {
        String nom = safeTrim(tfNomP.getText());
        String surfaceText = safeTrim(tfSurface.getText());
        String gpsText = buildGpsFromSelection();
        String adresse = safeTrim(tfAdresse.getText());
        String typeSol = safeTrim(cbTypeSol.getValue());

        String validationError = validateParcelleInputs(nom, surfaceText, gpsText, typeSol, adresse);
        if (validationError != null) {
            showError(validationError);
            return;
        }

        try {
            Parcelle p = new Parcelle();
            p.setNom(nom);
            p.setSurface(parseFrenchNumber(surfaceText));
            p.setGps(gpsText.isEmpty() ? null : gpsText);
            p.setTypeSol(typeSol);

            ParcelleCRUD pc = new ParcelleCRUD();
            if (parcelleToEdit != null) {
                p.setId(parcelleToEdit.getId());
                pc.modifier(p);
                JOptionPane.showMessageDialog(null, "Parcelle modifiee !");
                closeWindow();
                return;
            }

            pc.ajouter(p);
            JOptionPane.showMessageDialog(null, "Parcelle ajoutee avec succes !");
            viderChamps(null);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "La surface doit etre un nombre !");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erreur Connexion : Verifiez XAMPP !");
            System.err.println("Erreur SQL: " + e.getMessage());
        }
    }

    private String validateParcelleInputs(String nom, String surfaceText, String gpsText, String typeSol, String adresse) {
        if (nom.isEmpty()) {
            return "Le nom de la parcelle est obligatoire.";
        }
        if (nom.length() > MAX_NOM_LENGTH) {
            return "Le nom de la parcelle ne doit pas depasser 80 caracteres.";
        }
        if (!nom.matches(NOM_REGEX)) {
            return "Nom invalide: utilisez des lettres, espaces, tirets ou apostrophes.";
        }

        if (surfaceText.isEmpty()) {
            return "La surface est obligatoire.";
        }

        double surface;
        try {
            surface = parseFrenchNumber(surfaceText);
        } catch (NumberFormatException ex) {
            return "La surface doit etre un nombre valide (ex: 12.5).";
        }
        if (surface <= 0 || surface > MAX_SURFACE) {
            return "La surface doit etre superieure a 0 et inferieure ou egale a 1000000.";
        }

        if (typeSol.isEmpty()) {
            return "Le type de sol est obligatoire.";
        }
        if (!cbTypeSol.getItems().contains(typeSol)) {
            return "Type de sol invalide. Choisissez une valeur de la liste.";
        }

        if (adresse.isEmpty()) {
            return "L'adresse de la parcelle est obligatoire.";
        }

        if (gpsText.isEmpty()) {
            return "Veuillez localiser la parcelle sur la carte.";
        }
        if (!isValidGps(gpsText)) {
            return "GPS invalide. Format attendu: latitude, longitude (ex: 36.8065, 10.1815).";
        }

        return null;
    }

    private boolean isValidGps(String gpsText) {
        String[] parts = gpsText.split(",");
        if (parts.length != 2) {
            return false;
        }

        try {
            double latitude = parseFrenchNumber(parts[0].trim());
            double longitude = parseFrenchNumber(parts[1].trim());
            return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private double parseFrenchNumber(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @FXML
    void viderChamps(ActionEvent event) {
        tfNomP.clear();
        tfSurface.clear();
        tfAdresse.clear();
        cbTypeSol.getSelectionModel().clearSelection();
        selectedLatitude = null;
        selectedLongitude = null;
        resetMapToDefaultView();
    }

    @FXML
    void backToMenu(ActionEvent event) {
        closeWindow();
    }

    public void setParcelleForEdit(Parcelle parcelle) {
        this.parcelleToEdit = parcelle;
        tfNomP.setText(parcelle.getNom());
        tfSurface.setText(String.valueOf(parcelle.getSurface()));
        tfAdresse.setText(parcelle.getGps() == null ? "" : parcelle.getGps());
        parseExistingGpsAndUpdateMap(parcelle.getGps());
        cbTypeSol.setValue(parcelle.getTypeSol());
        if (lblTitle != null) {
            lblTitle.setText("Modifier Parcelle");
        }
        if (btnSave != null) {
            btnSave.setText("Mettre a jour");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tfNomP.getScene().getWindow();
        stage.close();
    }

    private void setupMap() {
        if (mapView == null) {
            return;
        }
        webEngine = mapView.getEngine();
        URL url = getClass().getResource("/map.html");
        if (url == null) {
            System.err.println("map.html introuvable dans les ressources.");
            return;
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });

        webEngine.load(url.toExternalForm());
    }

    private String buildGpsFromSelection() {
        if (selectedLatitude == null || selectedLongitude == null) {
            return "";
        }
        return String.format("%.6f, %.6f", selectedLatitude, selectedLongitude);
    }

    private void updateMapMarker(double latitude, double longitude) {
        if (webEngine == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                String script = String.format("setMarkerFromJava(%f, %f);", latitude, longitude);
                webEngine.executeScript(script);
            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour de la carte: " + e.getMessage());
            }
        });
    }

    private void resetMapToDefaultView() {
        if (webEngine == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                webEngine.executeScript("resetMapToDefault();");
            } catch (Exception e) {
                System.err.println("Erreur lors de la réinitialisation de la carte: " + e.getMessage());
            }
        });
    }

    private void parseExistingGpsAndUpdateMap(String gpsText) {
        if (gpsText == null || gpsText.isEmpty()) {
            return;
        }
        if (!isValidGps(gpsText)) {
            return;
        }
        String[] parts = gpsText.split(",");
        try {
            double latitude = parseFrenchNumber(parts[0].trim());
            double longitude = parseFrenchNumber(parts[1].trim());
            selectedLatitude = latitude;
            selectedLongitude = longitude;
            updateMapMarker(latitude, longitude);
        } catch (NumberFormatException ignored) {
        }
    }

    @FXML
    void localiserAdresse(ActionEvent event) {
        String adresse = safeTrim(tfAdresse.getText());
        if (adresse.isEmpty()) {
            showError("Veuillez saisir une adresse.");
            return;
        }
        geocodeAddress(adresse);
    }

    private void geocodeAddress(String adresse) {
        new Thread(() -> {
            try {
                String query = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
                String url = GEOCODING_URL + "?q=" + query + "&format=json&limit=1";

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                int status = connection.getResponseCode();
                if (status != 200) {
                    Platform.runLater(() ->
                            showError("Erreur lors de l'appel à l'API de géocodage (code " + status + ").")
                    );
                    connection.disconnect();
                    return;
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                } finally {
                    connection.disconnect();
                }

                String json = response.toString();
                Double lat = extractJsonDouble(json, "lat");
                Double lon = extractJsonDouble(json, "lon");

                if (lat == null || lon == null) {
                    Platform.runLater(() -> showError("Adresse non trouvée."));
                    return;
                }

                selectedLatitude = lat;
                selectedLongitude = lon;

                Platform.runLater(() -> {
                    tfAdresse.setText(adresse);
                    updateMapMarker(lat, lon);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Une erreur est survenue lors du géocodage.")
                );
            }
        }).start();
    }

    private Double extractJsonDouble(String json, String fieldName) {
        int index = json.indexOf("\"" + fieldName + "\"");
        if (index == -1) {
            return null;
        }
        int colonIndex = json.indexOf(":", index);
        if (colonIndex == -1) {
            return null;
        }
        int firstQuote = json.indexOf("\"", colonIndex);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (firstQuote == -1 || secondQuote == -1) {
            return null;
        }
        String value = json.substring(firstQuote + 1, secondQuote);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public class JavaConnector {
        public void onMapClicked(double latitude, double longitude) {
            selectedLatitude = latitude;
            selectedLongitude = longitude;
            Platform.runLater(() ->
                    tfAdresse.setText(String.format("%.6f, %.6f", latitude, longitude))
            );
        }
    }
}

