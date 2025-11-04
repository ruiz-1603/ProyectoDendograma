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
import modelo.datos.TransformadorDatos;
import modelo.datos.VariableConfig;
import modelo.distancias.CalculadorMatrizDistancia;
import modelo.distancias.FactoryDistancia;
import modelo.estructuras.Dendrograma;
import modelo.estructuras.ListaDoble;
import modelo.estructuras.Nodo;
import modelo.estructuras.Vector;
import modelo.clustering.MotorCluster;
import modelo.clustering.Ponderador;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ControladorPrincipal {

    @FXML private Label lblArchivoSeleccionado;
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private ComboBox<String> cmbTipoEnlace;
    @FXML private Spinner<Integer> spinnerClusters;
    
    @FXML private Button btnCargarCSV;
    @FXML private Button btnConfigurarPesos;
    @FXML private Button btnSeleccionarVariables;
    @FXML private Button btnEjecutar;
    
    @FXML private Label lblEstado;

    private CargadorCSV cargador;
    private SelectorColumnas selector;
    private Ponderador ponderador;
    private Vector[] vectores;
    private Nodo dendrogramaRaiz;
    private Dendrograma dendrograma;
    private File archivoCSV;
    private ListaDoble<VariableConfig> configs;
    

    @FXML
    public void initialize() {
        cmbDistancia.getItems().addAll("Euclidiana", "Manhattan", "Coseno", "Hamming");
        cmbDistancia.setValue("Euclidiana");

        cmbTipoEnlace.getItems().addAll("Promedio", "Mínimo", "Máximo", "Centroide");
        cmbTipoEnlace.setValue("Promedio");

        spinnerClusters.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        spinnerClusters.setDisable(true);

        btnConfigurarPesos.setDisable(true);
        btnSeleccionarVariables.setDisable(true);
        btnEjecutar.setDisable(true);

        lblEstado.setText("Esperando carga de archivo CSV...");

        dendrograma = new Dendrograma();
    }

    @FXML
    public void onCargarCSV() {
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

            cargador = new CargadorCSV();
            cargador.cargar(archivo.getAbsolutePath(), 0);

            vectores = cargador.getVectores();
            selector = new SelectorColumnas(cargador.getNombresDimensiones());
            
            // La configuración ahora se basa en los nombres de dimensiones finales
            this.configs = null; 

            double[] pesos = new double[cargador.getDimensiones()];
            for (int i = 0; i < pesos.length; i++) {
                pesos[i] = 1.0;
            }
            ponderador = new Ponderador(pesos, cargador.getNombresDimensiones());

            lblArchivoSeleccionado.setText(archivo.getName());

            btnConfigurarPesos.setDisable(false);
            btnSeleccionarVariables.setDisable(false);
            btnEjecutar.setDisable(false);
            spinnerClusters.setDisable(false);
            spinnerClusters.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, vectores.length, 1));

            lblEstado.setText("Listo para ejecutar clustering");

        } catch (IOException e) {
            mostrarError("Error al cargar CSV", e.getMessage());
        }
    }

    @FXML
    private void onConfigurarPesos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/pantalla-pesos.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/vista/pantalla-variables.fxml"));
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
            // Pasamos los nombres finales de las dimensiones
            controller.inicializarDatos(cargador.getNombresDimensiones(), this.configs);

            stage.showAndWait();

            if (controller.isGuardado()) {
                this.configs = controller.getConfigs();

                // La lógica de traducción ya no es necesaria, los nombres en configs son los finales
                ListaDoble<String> finalNamesToSelect = new ListaDoble<>();
                for (int i = 0; i < configs.tamanio(); i++) {
                    VariableConfig config = configs.obtener(i);
                    if (config.isSeleccionada()) {
                        finalNamesToSelect.agregar(config.getNombre());
                    }
                }

                String[] finalNamesArray = new String[finalNamesToSelect.tamanio()];
                for (int i = 0; i < finalNamesToSelect.tamanio(); i++) {
                    finalNamesArray[i] = finalNamesToSelect.obtener(i);
                }

                selector.ignorarTodas();
                selector.seleccionarMultiples(finalNamesArray);
                lblEstado.setText("Selección de variables actualizada.");
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
                    lblEstado.setText("Ejecutando clustering y generando JSON...");
                    btnEjecutar.setDisable(true);
                });

                Vector[] vectoresSeleccionados = selector.aplicarSeleccion(vectores);
                Ponderador ponderadorFiltrado = ponderador.filtrarPesos(selector);
                Vector[] vectoresPonderados = ponderadorFiltrado.aplicarPesos(vectoresSeleccionados);

                // --- Nueva Lógica de Normalización ---
                String[] nombresColumnasSeleccionadas = selector.getColumnasSeleccionadas();
                TransformadorDatos transformador = new TransformadorDatos(this.configs, nombresColumnasSeleccionadas);
                Vector[] vectoresNormalizados = transformador.normalizarPorVariable(vectoresPonderados);
                // --- Fin Nueva Lógica ---

                FactoryDistancia.TipoDistancia tipoDist = obtenerTipoDistancia();
                CalculadorMatrizDistancia calculador = new CalculadorMatrizDistancia();
                calculador.calcular(vectoresNormalizados, tipoDist);

                MotorCluster.TipoEnlace tipoEnlace = obtenerTipoEnlace();
                MotorCluster motor = new MotorCluster(tipoEnlace);
                dendrogramaRaiz = motor.construirDendrograma(vectoresNormalizados, tipoDist);

                if (dendrogramaRaiz != null) {
                    try (FileWriter writer = new FileWriter("dendrograma.json")) {
                        String json = dendrograma.toJSON(dendrogramaRaiz);
                        writer.write(json);
                        javafx.application.Platform.runLater(() -> {
                            mostrarInformacion("Exportación automática", "JSON guardado en la raíz del proyecto como 'dendrograma.json'");
                            lblEstado.setText("Clustering completado. JSON exportado.");
                        });
                    } catch (IOException e) {
                        javafx.application.Platform.runLater(() -> mostrarError("Error al exportar JSON", e.getMessage()));
                    }
                }

                javafx.application.Platform.runLater(() -> btnEjecutar.setDisable(false));

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mostrarError("Error en clustering", e.getMessage());
                    lblEstado.setText("Error en clustering");
                    btnEjecutar.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
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
