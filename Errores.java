import java.util.ArrayList;
import java.util.List;

public class Errores {

    private List<ErrorCompilacion> listaErrores;

    public Errores() {
        listaErrores = new ArrayList<>();
    }

    // --- MÉTODO ACTUALIZADO ---
    public void agregarError(int codigo, int linea, String detalleEspecifico) {
        // 1. Título base desde la tabla
        String titulo = TablaErrores.getMensaje(codigo);
        
        // 2. Concatenación con tu mensaje detallado
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
        System.out.println("--------------------------------------------------");
        System.out.printf("%-10s %-10s %-30s%n", "ERROR", "LINEA", "DESCRIPCIÓN");
        System.out.println("--------------------------------------------------");

        for (ErrorCompilacion e : listaErrores) {
            System.out.printf(
                "%-10d %-10d %-30s%n",
                e.getNumero(),
                e.getLinea(),
                e.getDescripcion()
            );
        }
        System.out.println("--------------------------------------------------");
    }
}