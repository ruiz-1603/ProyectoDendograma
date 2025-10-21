package controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import modelo.datos.CargadorCSV;
import modelo.datos.SelectorColumnas;
import modelo.estructuras.Vector;
import modelo.estructuras.Nodo;
import modelo.normalizacion.Normalizador;
import modelo.normalizacion.FactoryNormalizacion;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.clustering.MotorCluster;
import modelo.clustering.Ponderador;
import vista.DendrogramaDrawer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ControladorPrincipal {

    // Elementos de la UI (Vista)
    @FXML private Label lblArchivoSeleccionado;
    @FXML private ComboBox<String> cmbNormalizacion;
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private ComboBox<String> cmbTipoEnlace;
    @FXML private Spinner<Integer> spinnerClusters;
    @FXML private TextField txtDistanciaUmbral;
    @FXML private TextArea txtResultado;
    @FXML private Pane paneDendrograma;
    @FXML private Button btnCargarCSV;
    @FXML private Button btnConfigurarPesos;
    @FXML private Button btnSeleccionarVariables;
    @FXML private Button btnEjecutar;
    @FXML private Button btnExportarJSON;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblEstado;

    // Modelo (lógica de negocio)
    private CargadorCSV cargador;
    private SelectorColumnas selector;
    private Ponderador ponderador;
    private Vector[] vectores;
    private Nodo dendrogramaRaiz;
    private File archivoCSV;
    private String reportePrincipalCache = ""; // Cache for the main report

    @FXML
    public void initialize() {
        cmbNormalizacion.getItems().addAll("Min-Max", "Z-Score", "Logarítmica");
        cmbNormalizacion.setValue("Min-Max");

        cmbDistancia.getItems().addAll("Euclidiana", "Manhattan", "Coseno", "Hamming");
        cmbDistancia.setValue("Euclidiana");

        cmbTipoEnlace.getItems().addAll("Promedio", "Mínimo", "Máximo", "Centroide");
        cmbTipoEnlace.setValue("Promedio");

        spinnerClusters.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        spinnerClusters.setDisable(true);
        txtDistanciaUmbral.setDisable(true);

        btnConfigurarPesos.setDisable(true);
        btnSeleccionarVariables.setDisable(true);
        btnEjecutar.setDisable(true);
        btnExportarJSON.setDisable(true);

        progressBar.setVisible(false);
        lblEstado.setText("Esperando carga de archivo CSV...");
    }

    @FXML
    private void onCargarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        Stage stage = (Stage) btnCargarCSV.getScene().getWindow();
        archivoCSV = fileChooser.showOpenDialog(stage);

        if (archivoCSV != null) {
            cargarArchivo(archivoCSV);
        }
    }

    private void cargarArchivo(File archivo) {
        try {
            lblEstado.setText("Cargando archivo CSV...");
            progressBar.setVisible(true);

            cargador = new CargadorCSV();
            cargador.cargar(archivo.getAbsolutePath());

            vectores = cargador.getVectores();

            selector = new SelectorColumnas(cargador.getNombresDimensiones());

            double[] pesos = new double[cargador.getDimensiones()];
            Arrays.fill(pesos, 1.0);
            ponderador = new Ponderador(pesos, cargador.getNombresDimensiones());

            lblArchivoSeleccionado.setText(archivo.getName());
            actualizarTextoResultadoCarga();

            btnConfigurarPesos.setDisable(false);
            btnSeleccionarVariables.setDisable(false);
            btnEjecutar.setDisable(false);
            spinnerClusters.setDisable(false);
            spinnerClusters.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, vectores.length, 1));

            lblEstado.setText("Listo para ejecutar clustering");
            progressBar.setVisible(false);

        } catch (IOException e) {
            mostrarError("Error al cargar CSV", e.getMessage());
            progressBar.setVisible(false);
        }
    }

    @FXML
    private void onConfigurarPesos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/pantalla-pesos.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Configurar Pesos");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setWidth(450);
            stage.setHeight(600);
            stage.setMinWidth(350);
            stage.setMinHeight(400);

            ControladorPesos controller = loader.getController();
            controller.inicializarDatos(ponderador.getNombresDimensiones(), ponderador.getPesos());

            stage.showAndWait();

            if (controller.isGuardado()) {
                double[] nuevosPesos = controller.getNuevosPesos();
                ponderador = new Ponderador(nuevosPesos, cargador.getNombresDimensiones());
                lblEstado.setText("Pesos actualizados correctamente.");
            }

        } catch (IOException e) {
            mostrarError("Error al abrir configuración", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSeleccionarVariables() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/pantalla-variables.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Seleccionar Variables");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setWidth(450);
            stage.setHeight(600);
            stage.setMinWidth(350);
            stage.setMinHeight(400);

            ControladorVariables controller = loader.getController();
            controller.inicializarDatos(selector.getTodasLasColumnas(), selector.getColumnasSeleccionadas());

            stage.showAndWait();

            if (controller.isGuardado()) {
                selector.ignorarTodas();
                selector.seleccionarMultiples(controller.getColumnasSeleccionadas());
                lblEstado.setText("Selección de variables actualizada.");
                actualizarTextoResultadoCarga();
            }

        } catch (IOException e) {
            mostrarError("Error al abrir selección", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEjecutar() {
        if (vectores == null) {
            mostrarError("Error", "Primero debe cargar un archivo CSV");
            return;
        }
        if (!selector.esValido()){
            mostrarError("Error de selección", "Debe seleccionar al menos una variable para el análisis.");
            return;
        }

        final int k = spinnerClusters.getValue();

        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() -> {
                    lblEstado.setText("Ejecutando clustering...");
                    progressBar.setVisible(true);
                    btnEjecutar.setDisable(true);
                    txtDistanciaUmbral.setDisable(true);
                    txtDistanciaUmbral.clear();
                });

                Vector[] vectoresSeleccionados = selector.aplicarSeleccion(vectores);
                Ponderador ponderadorFiltrado = ponderador.filtrarPesos(selector);
                Vector[] vectoresPonderados = ponderadorFiltrado.aplicarPesos(vectoresSeleccionados);

                FactoryNormalizacion.TipoNormalizacion tipoNorm = obtenerTipoNormalizacion();
                Normalizador normalizador = new Normalizador(tipoNorm);
                Vector[] vectoresNormalizados = normalizador.normalizar(vectoresPonderados);

                FactoryDistancia.TipoDistancia tipoDist = obtenerTipoDistancia();
                CalculadorMatrizDistancia calculador = new CalculadorMatrizDistancia();
                calculador.calcular(vectoresNormalizados, tipoDist);

                MotorCluster.TipoEnlace tipoEnlace = obtenerTipoEnlace();
                MotorCluster motor = new MotorCluster(tipoEnlace);
                dendrogramaRaiz = motor.construirDendrograma(vectoresNormalizados, tipoDist);

                javafx.application.Platform.runLater(() -> {
                    StringBuilder resultado = new StringBuilder("✓ Clustering completado exitosamente\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Variables usadas: " + selector.getNumeroSeleccionadas() + " de " + selector.getNumeroTotal() + "\n" +
                            "Normalización: " + cmbNormalizacion.getValue() + "\n" +
                            "Distancia: " + cmbDistancia.getValue() + "\n" +
                            "Tipo de enlace: " + cmbTipoEnlace.getValue() + "\n" +
                            "Altura del árbol: " + dendrogramaRaiz.altura() + "\n" +
                            "Hojas: " + dendrogramaRaiz.contarHojas() + "\n" +
                            "Distancia máx. de fusión: " + String.format("%.4f", dendrogramaRaiz.getDistancia()) + "\n" +
                            "Fusiones: " + motor.obtenerNumeroFusiones());

                    reportePrincipalCache = resultado.toString(); // Cache the main report

                    List<Nodo> clustersParaColorear = new ArrayList<>();

                    if (k > 1 && dendrogramaRaiz != null) {
                        try {
                            clustersParaColorear = dendrogramaRaiz.cortarArbol(k);
                            resultado.append("\n\n").append(generarReporteClusters(clustersParaColorear));
                        } catch (Exception e) {
                            resultado.append("\n\nError al cortar el árbol: ").append(e.getMessage());
                        }
                    }

                    txtResultado.setText(resultado.toString());
                    DendrogramaDrawer.draw(paneDendrograma, dendrogramaRaiz, clustersParaColorear);

                    lblEstado.setText("Clustering completado");
                    progressBar.setVisible(false);
                    btnEjecutar.setDisable(false);
                    btnExportarJSON.setDisable(false);
                    txtDistanciaUmbral.setDisable(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error en clustering", e.getMessage());
                    lblEstado.setText("Error en clustering");
                    progressBar.setVisible(false);
                    btnEjecutar.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    public void onAplicarCorteDistancia() {
        if (dendrogramaRaiz == null) {
            mostrarError("Error", "Primero debe ejecutar el clustering para poder cortar por distancia.");
            return;
        }

        String umbralStr = txtDistanciaUmbral.getText();
        if (umbralStr == null || umbralStr.isBlank()) {
            txtResultado.setText(reportePrincipalCache); // Restore main report
            DendrogramaDrawer.draw(paneDendrograma, dendrogramaRaiz, null); // Redraw with no colors
            return;
        }

        try {
            double umbral = Double.parseDouble(umbralStr.replace(',', '.'));
            if (umbral < 0) {
                mostrarError("Error de validación", "La distancia umbral no puede ser negativa.");
                return;
            }

            List<Nodo> clusters = dendrogramaRaiz.cortarPorDistancia(umbral);
            String reporteCorte = generarReporteClusters(clusters);
            txtResultado.setText(reportePrincipalCache + "\n\n" + reporteCorte);
            DendrogramaDrawer.draw(paneDendrograma, dendrogramaRaiz, clusters);

        } catch (NumberFormatException e) {
            mostrarError("Error de formato", "Por favor, ingrese un número válido para la distancia umbral.");
        } catch (Exception e) {
            mostrarError("Error al cortar", e.getMessage());
            e.printStackTrace();
        }
    }

    private String generarReporteClusters(List<Nodo> clusters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resultado del Corte en ").append(clusters.size()).append(" Clusters\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (int i = 0; i < clusters.size(); i++) {
            Nodo cluster = clusters.get(i);
            sb.append("Cluster ").append(i + 1).append(" (").append(cluster.contarHojas()).append(" miembros):\n");
            List<String> etiquetas = cluster.obtenerEtiquetasHojas();
            sb.append("  ").append(etiquetas.stream().collect(Collectors.joining(", "))).append("\n\n");
        }
        return sb.toString();
    }

    @FXML
    private void onExportarJSON() {
        if (dendrogramaRaiz == null) {
            mostrarError("Error", "Primero debe ejecutar el clustering");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar dendrograma como JSON");
        fileChooser.setInitialFileName("dendrograma.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json")
        );

        Stage stage = (Stage) btnExportarJSON.getScene().getWindow();
        File archivo = fileChooser.showSaveDialog(stage);

        if (archivo != null) {
            try (FileWriter writer = new FileWriter(archivo)) {
                String json = dendrogramaRaiz.toJSON();
                writer.write(json);
                mostrarInformacion("Exportación exitosa",
                        "JSON guardado en:\n" + archivo.getAbsolutePath());
                lblEstado.setText("JSON exportado correctamente");

            } catch (IOException e) {
                mostrarError("Error al exportar", e.getMessage());
            }
        }
    }
    
    private void actualizarTextoResultadoCarga(){
        txtResultado.setText(
            "✓ Archivo cargado exitosamente\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Películas: " + cargador.getNumeroFilas() + "\n" +
            "Dimensiones totales: " + cargador.getDimensiones() + "\n" +
            "Variables seleccionadas: " + selector.getNumeroSeleccionadas() + "\n" +
            "Variables a usar: " + String.join(", ", selector.getColumnasSeleccionadas())
        );
    }

    private FactoryNormalizacion.TipoNormalizacion obtenerTipoNormalizacion() {
        switch (cmbNormalizacion.getValue()) {
            case "Z-Score": return FactoryNormalizacion.TipoNormalizacion.Z_SCORE;
            case "Logarítmica": return FactoryNormalizacion.TipoNormalizacion.LOGARITMICA;
            default: return FactoryNormalizacion.TipoNormalizacion.MIN_MAX;
        }
    }

    private FactoryDistancia.TipoDistancia obtenerTipoDistancia() {
        switch (cmbDistancia.getValue()) {
            case "Manhattan": return FactoryDistancia.TipoDistancia.MANHATTAN;
            case "Coseno": return FactoryDistancia.TipoDistancia.COSENO;
            case "Hamming": return FactoryDistancia.TipoDistancia.HAMMING;
            default: return FactoryDistancia.TipoDistancia.EUCLIDIANA;
        }
    }

    private MotorCluster.TipoEnlace obtenerTipoEnlace() {
        switch (cmbTipoEnlace.getValue()) {
            case "Mínimo": return MotorCluster.TipoEnlace.MINIMO;
            case "Máximo": return MotorCluster.TipoEnlace.MAXIMO;
            case "Centroide": return MotorCluster.TipoEnlace.CENTROIDE;
            default: return MotorCluster.TipoEnlace.PROMEDIO;
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}