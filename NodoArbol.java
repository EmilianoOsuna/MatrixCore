import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    private String valor;
    private List<NodoArbol> hijos;

    public NodoArbol(String valor) {
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(NodoArbol hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }

    public String getValor() {
        return valor;
    }

    public List<NodoArbol> getHijos() {
        return hijos;
    }
}