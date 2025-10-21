package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ControladorVariables {

    @FXML private VBox vboxVariables;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private List<CheckBox> checkBoxes = new ArrayList<>();
    private List<String> columnasSeleccionadas = new ArrayList<>();
    private boolean guardado = false;

    public void inicializarDatos(String[] todasLasColumnas, String[] columnasYaSeleccionadas) {
        for (String columna : todasLasColumnas) {
            CheckBox checkBox = new CheckBox(columna);
            checkBox.setSelected(false); // Deseleccionar por defecto
            for (String seleccionada : columnasYaSeleccionadas) {
                if (columna.equals(seleccionada)) {
                    checkBox.setSelected(true);
                    break;
                }
            }
            checkBoxes.add(checkBox);
            vboxVariables.getChildren().add(checkBox);
        }
    }

    @FXML
    private void onGuardar() {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                columnasSeleccionadas.add(checkBox.getText());
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
        return columnasSeleccionadas.toArray(new String[0]);
    }

    public boolean isGuardado() {
        return guardado;
    }
}
