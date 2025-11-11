package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import modelo.estructuras.ListaDoble;

public class ControladorPesos {

    @FXML private GridPane gridPesos;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private ListaDoble<TextField> camposDeTexto = new ListaDoble<>();
    private double[] nuevosPesos;
    private boolean guardado = false;

    @FXML
    public void initialize() {
    }

    public void inicializarDatos(String[] nombresDimensiones, double[] pesosActuales) {
        for (int i = 0; i < nombresDimensiones.length; i++) {
            Label nombreLabel = new Label(nombresDimensiones[i]);
            TextField pesoField = new TextField(String.valueOf(pesosActuales[i]));

            // Guardar referencia al campo de texto
            camposDeTexto.agregar(pesoField);

            gridPesos.add(nombreLabel, 0, i);
            gridPesos.add(pesoField, 1, i);
        }
    }

    @FXML
    private void onGuardar() {
        nuevosPesos = new double[camposDeTexto.tamanio()];
        try {
            for (int i = 0; i < camposDeTexto.tamanio(); i++) {
                double peso = Double.parseDouble(camposDeTexto.obtener(i).getText());
                if (peso < 0) {
                    // Opcional: Mostrar error al usuario
                    System.err.println("Error: El peso no puede ser negativo.");
                    return;
                }
                nuevosPesos[i] = peso;
            }
            guardado = true;
            cerrarVentana();
        } catch (NumberFormatException e) {
            System.err.println("Error: Ingrese un número válido para el peso.");
        }
    }

    @FXML
    private void onCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public double[] getNuevosPesos() {
        return nuevosPesos;
    }

    public boolean isGuardado() {
        return guardado;
    }
}