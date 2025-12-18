import java.util.ArrayList;
import java.util.List;

public class Errores {

    private List<ErrorCompilacion> listaErrores;

    public Errores() {
        listaErrores = new ArrayList<>();
    }

    // --- AGREGAR ERROR ---
    public void agregarError(int codigo, int linea, String detalleEspecifico) {
        //Título base desde la tabla
        String titulo = TablaErrores.getMensaje(codigo);
        
        // Concatenación con el mensaje detallado
        String descripcionFinal;
        if (detalleEspecifico != null && !detalleEspecifico.isEmpty()) {
            descripcionFinal = titulo + " " + detalleEspecifico;
        } else {
            descripcionFinal = titulo;
        }

        listaErrores.add(new ErrorCompilacion(codigo, linea, descripcionFinal));
    }

    public boolean hayErrores() {
        return !listaErrores.isEmpty();
    }

    public List<ErrorCompilacion> getErrores() {
        return listaErrores;
    }

    public void imprimirTabla() {
        for (ErrorCompilacion e : listaErrores) {
            System.out.printf(
                "%-10d %-10d %-30s%n",
                e.getNumero(),
                e.getLinea(),
                e.getDescripcion()
            );
        }
    }
}