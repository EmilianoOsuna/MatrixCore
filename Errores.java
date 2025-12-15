import java.util.ArrayList;
import java.util.List;

public class Errores {

    private List<ErrorCompilacion> listaErrores;

    public Errores() {
        listaErrores = new ArrayList<>();
    }

    public void agregarError(int numero, int linea, String descripcion) {
        listaErrores.add(new ErrorCompilacion(numero, linea, descripcion));
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

