package controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Controlador principal de la aplicación
 * Patrón: MVC - Controlador
 * Maneja la interacción entre la vista y el modelo
 */
public class ControladorPrincipal {

    // Elementos de la UI (Vista)
    @FXML private Label lblArchivoSeleccionado;
    @FXML private ComboBox<String> cmbNormalizacion;
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private ComboBox<String> cmbTipoEnlace;
    @FXML private TextArea txtResultado;
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

    /**
     * Inicializa el controlador después de cargar el FXML
     */
    @FXML
    public void initialize() {
        // Inicializar ComboBox de Normalización
        cmbNormalizacion.getItems().addAll("Min-Max", "Z-Score", "Logarítmica");
        cmbNormalizacion.setValue("Min-Max");

        // Inicializar ComboBox de Distancia
        cmbDistancia.getItems().addAll("Euclidiana", "Manhattan", "Coseno", "Hamming");
        cmbDistancia.setValue("Euclidiana");

        // Inicializar ComboBox de Tipo de Enlace
        cmbTipoEnlace.getItems().addAll("Promedio", "Mínimo", "Máximo", "Centroide");
        cmbTipoEnlace.setValue("Promedio");

        // Deshabilitar botones hasta cargar CSV
        btnConfigurarPesos.setDisable(true);
        btnSeleccionarVariables.setDisable(true);
        btnEjecutar.setDisable(true);
        btnExportarJSON.setDisable(true);

        // Ocultar barra de progreso
        progressBar.setVisible(false);
        lblEstado.setText("Esperando carga de archivo CSV...");
    }

    /**
     * Maneja el evento de cargar archivo CSV
     */
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

    /**
     * Carga el archivo CSV
     */
    private void cargarArchivo(File archivo) {
        try {
            lblEstado.setText("Cargando archivo CSV...");
            progressBar.setVisible(true);

            cargador = new CargadorCSV();
            cargador.cargar(archivo.getAbsolutePath());

            vectores = cargador.getVectores();

            // Inicializar selector con todas las columnas
            selector = new SelectorColumnas(cargador.getNombresDimensiones());

            // Inicializar ponderador sin pesos (todos 1.0)
            double[] pesos = new double[cargador.getDimensiones()];
            for (int i = 0; i < pesos.length; i++) {
                pesos[i] = 1.0;
            }
            ponderador = new Ponderador(pesos, cargador.getNombresDimensiones());

            // Actualizar UI
            lblArchivoSeleccionado.setText(archivo.getName());
            txtResultado.setText(
                    "✓ Archivo cargado exitosamente\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Películas: " + cargador.getNumeroFilas() + "\n" +
                            "Dimensiones: " + cargador.getDimensiones() + "\n" +
                            "Variables: " + String.join(", ", cargador.getNombresDimensiones())
            );

            // Habilitar botones
            btnConfigurarPesos.setDisable(false);
            btnSeleccionarVariables.setDisable(false);
            btnEjecutar.setDisable(false);

            lblEstado.setText("Listo para ejecutar clustering");
            progressBar.setVisible(false);

        } catch (IOException e) {
            mostrarError("Error al cargar CSV", e.getMessage());
            progressBar.setVisible(false);
        }
    }

    /**
     * Maneja el evento de configurar pesos
     */
    @FXML
    private void onConfigurarPesos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/pantalla-pesos.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Configurar Pesos");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            // --- INICIO DE CAMBIOS ---
            stage.setWidth(450);
            stage.setHeight(600);
            stage.setMinWidth(350);
            stage.setMinHeight(400);
            // --- FIN DE CAMBIOS ---

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

    /**
     * Maneja el evento de seleccionar variables
     */
    @FXML
    private void onSeleccionarVariables() {
        // TODO: Abrir ventana de selección de variables
        mostrarInformacion("Seleccionar Variables",
                "Ventana de selección de variables próximamente...\n" +
                        "Por ahora todas las variables están seleccionadas");
    }

    /**
     * Maneja el evento de ejecutar clustering
     */
    @FXML
    private void onEjecutar() {
        if (vectores == null) {
            mostrarError("Error", "Primero debe cargar un archivo CSV");
            return;
        }

        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                javafx.application.Platform.runLater(() -> {
                    lblEstado.setText("Ejecutando clustering...");
                    progressBar.setVisible(true);
                    btnEjecutar.setDisable(true);
                });

                // Aplicar pesos
                Vector[] vectoresPonderados = ponderador.aplicarPesos(vectores);

                // Normalizar
                FactoryNormalizacion.TipoNormalizacion tipoNorm = obtenerTipoNormalizacion();
                Normalizador normalizador = new Normalizador(tipoNorm);
                Vector[] vectoresNormalizados = normalizador.normalizar(vectoresPonderados);

                // Calcular matriz de distancias
                FactoryDistancia.TipoDistancia tipoDist = obtenerTipoDistancia();
                CalculadorMatrizDistancia calculador = new CalculadorMatrizDistancia();
                calculador.calcular(vectoresNormalizados, tipoDist);

                // Ejecutar clustering
                MotorCluster.TipoEnlace tipoEnlace = obtenerTipoEnlace();
                MotorCluster motor = new MotorCluster(tipoEnlace);
                dendrogramaRaiz = motor.construirDendrograma(vectoresNormalizados, tipoDist);

                // Actualizar UI con resultados
                javafx.application.Platform.runLater(() -> {
                    String resultado = "✓ Clustering completado exitosamente\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Normalización: " + cmbNormalizacion.getValue() + "\n" +
                            "Distancia: " + cmbDistancia.getValue() + "\n" +
                            "Tipo de enlace: " + cmbTipoEnlace.getValue() + "\n" +
                            "Altura del árbol: " + dendrogramaRaiz.altura() + "\n" +
                            "Hojas: " + dendrogramaRaiz.contarHojas() + "\n" +
                            "Fusiones: " + motor.obtenerNumeroFusiones() + "\n" +
                            "\n" +
                            "Dendrograma (primeras líneas):\n" +
                            dendrogramaRaiz.toStringArbol().substring(0, Math.min(500, dendrogramaRaiz.toStringArbol().length())) + "...";

                    txtResultado.setText(resultado);
                    lblEstado.setText("Clustering completado");
                    progressBar.setVisible(false);
                    btnEjecutar.setDisable(false);
                    btnExportarJSON.setDisable(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error en clustering", e.getMessage());
                    lblEstado.setText("Error en clustering");
                    progressBar.setVisible(false);
                    btnEjecutar.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Maneja el evento de exportar JSON
     */
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
            try {
                String json = dendrogramaRaiz.toJSON();
                FileWriter writer = new FileWriter(archivo);
                writer.write(json);
                writer.close();

                mostrarInformacion("Exportación exitosa",
                        "JSON guardado en:\n" + archivo.getAbsolutePath());
                lblEstado.setText("JSON exportado correctamente");

            } catch (IOException e) {
                mostrarError("Error al exportar", e.getMessage());
            }
        }
    }

    // Métodos auxiliares

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
