import javax.swing.*;
import java.awt.*;

public class VentanaArbol extends JFrame {

    public VentanaArbol(NodoArbol raiz) {
        setTitle("Árbol de Derivación Sintáctico");
        setSize(800, 600);
        setLocationRelativeTo(null); // Centrar en pantalla
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cerrar esta ventana, no la app

        // Instanciamos tu panel de dibujo
        PanelArbol panelDibujo = new PanelArbol(raiz);
        
        // Lo metemos en un ScrollPane por si el árbol es gigante
        JScrollPane scroll = new JScrollPane(panelDibujo);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);

        add(scroll);
    }
}