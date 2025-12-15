public class ErrorCompilacion {

    private int numero;
    private int linea;
    private String descripcion;

    public ErrorCompilacion(int numero, int linea, String descripcion) {
        this.numero = numero;
        this.linea = linea;
        this.descripcion = descripcion;
    }

    public int getNumero() {
        return numero;
    }

    public int getLinea() {
        return linea;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
