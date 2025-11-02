package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import modelo.estructuras.ListaDoble;

public class ControladorVariables {

    @FXML private VBox vboxVariables;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ListaDoble<CheckBox> checkBoxes = new ListaDoble<>();
    private ListaDoble<String> columnasSeleccionadas = new ListaDoble<>();
    private boolean guardado = false;

    public void inicializarDatos(String[] todasLasColumnas, String[] columnasYaSeleccionadas) {
        for (String columna : todasLasColumnas) {
            CheckBox checkBox = new CheckBox(columna);
            checkBox.setSelected(false);

            for (String seleccionada : columnasYaSeleccionadas) {
                if (columna.equals(seleccionada)) {
                    checkBox.setSelected(true);
                    break;
                }
            }

            checkBoxes.agregar(checkBox);
            vboxVariables.getChildren().add(checkBox);
        }
    }

    @FXML
    private void onGuardar() {
        for (int i = 0; i < checkBoxes.tamanio(); i++) {
            CheckBox checkBox = checkBoxes.obtener(i);
            if (checkBox.isSelected()) {
                columnasSeleccionadas.agregar(checkBox.getText());
            }
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

    public String[] getColumnasSeleccionadas() {
        String[] resultado = new String[columnasSeleccionadas.tamanio()];
        for (int i = 0; i < columnasSeleccionadas.tamanio(); i++) {
            resultado[i] = columnasSeleccionadas.obtener(i);
        }
        return resultado;
    }

    public boolean isGuardado() {
        return guardado;
    }
}