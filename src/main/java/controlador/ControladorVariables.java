package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modelo.datos.VariableConfig;
import modelo.estructuras.ListaDoble;

public class ControladorVariables {

    @FXML private VBox vboxVariables;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ListaDoble<VariableConfig> configs = new ListaDoble<>();
    private ListaDoble<HBox> filasUI = new ListaDoble<>();
    private boolean guardado = false;

    private final ObservableList<String> normalizacionOptions = FXCollections.observableArrayList("Ninguno", "Min-Max", "Z-Score", "Logaritmica");

    private String determinarTipoDato(String nombreColumnaFinal) {
        String[] COLUMNAS_CATEGORICAS_ORIGINALES = {"original_language", "status"};

        for (String original : COLUMNAS_CATEGORICAS_ORIGINALES) {
            if (nombreColumnaFinal.startsWith(original + "_")) {
                return "Categórico";
            }
        }
        return "Numérico";
    }

    public void inicializarDatos(String[] todasLasColumnas, ListaDoble<VariableConfig> configsExistentes) {
        this.configs.limpiar();
        this.filasUI.limpiar();
        this.vboxVariables.getChildren().clear();

        Label masterLabel = new Label("Aplicar a todo (Numérico):");
        ComboBox<String> masterNormalizacionBox = new ComboBox<>(normalizacionOptions);
        masterNormalizacionBox.setPromptText("Seleccionar...");
        masterNormalizacionBox.getStylesheets().add(getClass().getResource("/org/example/style/comboBox.css").toExternalForm());
        masterNormalizacionBox.setStyle("-fx-background-color: #3D3A3A; -fx-border-color: #ffffff;");

        masterNormalizacionBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                for (int i = 0; i < configs.tamanio(); i++) {
                    VariableConfig config = configs.obtener(i);
                    if ("Numérico".equals(config.getTipoDato())) {
                        HBox fila = filasUI.obtener(i);
                        @SuppressWarnings("unchecked")
                        ComboBox<String> normalizacionBox = (ComboBox<String>) fila.getChildren().get(4);
                        normalizacionBox.setValue(newVal);
                    }
                }
            }
        });

        HBox masterBox = new HBox(10, masterLabel, masterNormalizacionBox);
        masterBox.setAlignment(Pos.CENTER_LEFT);
        masterBox.setPadding(new Insets(0, 0, 10, 0));

        vboxVariables.getChildren().add(masterBox);
        vboxVariables.getChildren().add(new Separator());

        if (configsExistentes == null || configsExistentes.tamanio() == 0) {
            for (String columna : todasLasColumnas) {
                String tipoDato = determinarTipoDato(columna);
                boolean seleccionadaPorDefecto = true; // Seleccionar todo por defecto
                this.configs.agregar(new VariableConfig(columna, seleccionadaPorDefecto, tipoDato, "Ninguno"));
            }
        } else {
            this.configs = configsExistentes;
        }

        for (int i = 0; i < configs.tamanio(); i++) {
            VariableConfig config = configs.obtener(i);
            HBox fila = crearFilaUI(config);
            filasUI.agregar(fila);
            vboxVariables.getChildren().add(fila);
        }
    }

    private HBox crearFilaUI(VariableConfig config) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(config.isSeleccionada());

        Label labelNombre = new Label(config.getNombre());
        labelNombre.setPrefWidth(120);
        labelNombre.setWrapText(true);

        Label labelTipo = new Label(config.getTipoDato());
        labelTipo.setPrefWidth(80);

        ComboBox<String> normalizacionBox = new ComboBox<>(normalizacionOptions);
        normalizacionBox.setValue(config.getMetodoNormalizacion());
        normalizacionBox.getStylesheets().add(getClass().getResource("/org/example/style/comboBox.css").toExternalForm());
        normalizacionBox.setStyle("-fx-background-color: #3D3A3A; -fx-border-color: #ffffff;");

        if (!"Numérico".equals(config.getTipoDato())) {
            normalizacionBox.setDisable(true);
        }

        HBox growBox = new HBox();
        HBox.setHgrow(growBox, Priority.ALWAYS);

        hbox.getChildren().addAll(checkBox, labelNombre, growBox, labelTipo, normalizacionBox);
        return hbox;
    }

    @FXML
    private void onGuardar() {
        for (int i = 0; i < filasUI.tamanio(); i++) {
            HBox fila = filasUI.obtener(i);
            VariableConfig config = configs.obtener(i);

            CheckBox checkBox = (CheckBox) fila.getChildren().get(0);
            ComboBox<String> normalizacionBox = (ComboBox<String>) fila.getChildren().get(4);

            config.setSeleccionada(checkBox.isSelected());
            config.setMetodoNormalizacion(normalizacionBox.getValue());
        }
        guardado = true;
        cerrarVentana();
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public ListaDoble<VariableConfig> getConfigs() {
        return configs;
    }

    public boolean isGuardado() {
        return guardado;
    }
}
