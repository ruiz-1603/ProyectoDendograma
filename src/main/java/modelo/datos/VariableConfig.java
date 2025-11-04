package modelo.datos;

public class VariableConfig {
    private String nombre;
    private boolean seleccionada;
    private String tipoDato;
    private String metodoNormalizacion;

    public VariableConfig(String nombre, boolean seleccionada, String tipoDato, String metodoNormalizacion) {
        this.nombre = nombre;
        this.seleccionada = seleccionada;
        this.tipoDato = tipoDato;
        this.metodoNormalizacion = metodoNormalizacion;
    }

    // Getters
    public String getNombre() { return nombre; }
    public boolean isSeleccionada() { return seleccionada; }
    public String getTipoDato() { return tipoDato; }
    public String getMetodoNormalizacion() { return metodoNormalizacion; }

    // Setters
    public void setSeleccionada(boolean seleccionada) { this.seleccionada = seleccionada; }
    public void setTipoDato(String tipoDato) { this.tipoDato = tipoDato; }
    public void setMetodoNormalizacion(String metodoNormalizacion) { this.metodoNormalizacion = metodoNormalizacion; }
}
