import com.formdev.flatlaf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiladorUI extends JFrame {

    // --- COMPONENTES GLOBALES ---
    private JTextPane txtEntrada; 
    private JTextArea txtResultadoSintactico;
    private JTable tblTokens;
    private DefaultTableModel modeloTablaTokens;

    // Paneles Ocultables
    private JPanel pnlTabla;
    private JScrollPane scrollSintactico;
    private JScrollPane scrollTabla;

    // Splits
    private JSplitPane splitSuperior;
    private JSplitPane splitPrincipal;

    // Botones Toggle
    private JToggleButton btnTokens;
    private JToggleButton btnConsola;

    // Estado lógico
    private boolean consolaVisible = false;
    private boolean tokensVisible = false; 

    // Variables de Color Dinámicas (Cambian según el tema)
    private Color colorFondo;
    private Color colorTexto;
    private Color colorError;
    private Color colorExito;

    // Estilos de Syntax Highlighting
    private SimpleAttributeSet estiloNormal = new SimpleAttributeSet();
    private SimpleAttributeSet estiloPalabraReservada = new SimpleAttributeSet();
    private SimpleAttributeSet estiloCadena = new SimpleAttributeSet();
    private SimpleAttributeSet estiloNumero = new SimpleAttributeSet();
    private SimpleAttributeSet estiloComentario = new SimpleAttributeSet();

    public CompiladorUI() {
        setTitle("Analizador Léxico y Sintáctico - IDE Profesional");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Configuración inicial
        inicializarEstilosBase(); 
        crearMenu();
        inicializarComponentes();
        
        // Aplicamos el tema oscuro por defecto al iniciar
        aplicarTemaGlobal(true);
        
        actualizarPaneles(); 
    }

    private void inicializarEstilosBase() {
        StyleConstants.setBold(estiloPalabraReservada, true);
        StyleConstants.setItalic(estiloComentario, true);
    }

    // --- MÉTODO MAESTRO DE TEMAS ---
    private void aplicarTemaGlobal(boolean modoOscuro) {
        if (modoOscuro) {
            // --- PALETA MODO OSCURO (VS Code Dark) ---
            colorFondo = new Color(30, 30, 30);
            colorTexto = new Color(220, 220, 220);
            colorError = new Color(255, 100, 100); // Rojo pastel (brillante)
            colorExito = new Color(100, 255, 100); // Verde pastel (brillante)
            
            // Colores de Sintaxis
            StyleConstants.setForeground(estiloNormal, colorTexto);
            StyleConstants.setForeground(estiloPalabraReservada, new Color(197, 134, 192)); 
            StyleConstants.setForeground(estiloCadena, new Color(206, 145, 120)); 
            StyleConstants.setForeground(estiloNumero, new Color(181, 206, 168)); 
            StyleConstants.setForeground(estiloComentario, new Color(106, 153, 85)); 

        } else {
            // --- PALETA MODO CLARO (Eclipse / NetBeans) ---
            colorFondo = Color.WHITE;
            colorTexto = Color.BLACK;
            colorError = new Color(200, 0, 0);   // Rojo fuerte (oscuro)
            colorExito = new Color(0, 128, 0);   // Verde fuerte (oscuro)

            // Colores de Sintaxis
            StyleConstants.setForeground(estiloNormal, colorTexto); 
            StyleConstants.setForeground(estiloPalabraReservada, new Color(127, 0, 85)); // Morado Eclipse
            StyleConstants.setForeground(estiloCadena, new Color(42, 0, 255)); // Azul
            StyleConstants.setForeground(estiloNumero, new Color(0, 128, 0)); // Verde
            StyleConstants.setForeground(estiloComentario, new Color(63, 127, 95)); // Gris Verdoso
        }

        // --- APLICAR COLORES A LOS COMPONENTES ---
        
        // 1. Editor de Código
        txtEntrada.setBackground(colorFondo);
        txtEntrada.setCaretColor(modoOscuro ? Color.WHITE : Color.BLACK);
        
        // 2. Consola de Salida
        txtResultadoSintactico.setBackground(colorFondo);
        // Si no hay texto de error/éxito actual, ponemos el normal
        if (txtResultadoSintactico.getText().isEmpty()) {
            txtResultadoSintactico.setForeground(colorTexto);
        }

        // 3. Tabla de Tokens
        tblTokens.setBackground(colorFondo);
        tblTokens.setForeground(colorTexto);
        tblTokens.setGridColor(modoOscuro ? new Color(60, 60, 60) : Color.LIGHT_GRAY);
        // Encabezado de la tabla (Truco para que se vea bien)
        if (scrollTabla != null) {
            scrollTabla.getViewport().setBackground(colorFondo);
        }

        // Repintar sintaxis
        pintarCodigo();
    }

    private void inicializarComponentes() {
        // --- 1.1 Panel de Código ---
        JPanel pnlCodigo = new JPanel(new BorderLayout());
        pnlCodigo.setBorder(BorderFactory.createTitledBorder("Código Fuente"));
        
        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 15));
        txtEntrada.putClientProperty("JComponent.roundRect", true);
        
        ((AbstractDocument) txtEntrada.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                super.insertString(fb, offset, string, attr); pintarCodigo();
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text, attrs); pintarCodigo();
            }
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length); pintarCodigo();
            }
        });

        JScrollPane scrollCodigo = new JScrollPane(txtEntrada);
        try { scrollCodigo.setRowHeaderView(new TextLineNumber(txtEntrada)); } catch (Exception e) {}
        pnlCodigo.add(scrollCodigo, BorderLayout.CENTER);

        // --- 1.2 Panel Tabla ---
        pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));
        String[] columnas = {"Lexema", "Tipo de Token", "Línea"};
        modeloTablaTokens = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTokens = new JTable(modeloTablaTokens);
        tblTokens.setShowGrid(true);
        tblTokens.setFillsViewportHeight(true);
        
        scrollTabla = new JScrollPane(tblTokens); // Guardamos referencia para colorear fondo
        pnlTabla.add(scrollTabla, BorderLayout.CENTER);

        // --- 1.3 Panel Consola ---
        txtResultadoSintactico = new JTextArea();
        txtResultadoSintactico.setEditable(false);
        txtResultadoSintactico.setFont(new Font("Consolas", Font.PLAIN, 13));
        scrollSintactico = new JScrollPane(txtResultadoSintactico);
        scrollSintactico.setBorder(BorderFactory.createTitledBorder("Salida / Errores Sintácticos"));
        scrollSintactico.setMinimumSize(new Dimension(0, 100));

        // --- 2. Splits ---
        splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitSuperior.setLeftComponent(pnlCodigo); 
        splitSuperior.setResizeWeight(1.0); 
        splitSuperior.setContinuousLayout(true);
        splitSuperior.setBorder(null);

        splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPrincipal.setTopComponent(splitSuperior);
        splitPrincipal.setResizeWeight(1.0); 
        splitPrincipal.setContinuousLayout(true);

        add(splitPrincipal, BorderLayout.CENTER);

        // --- 3. Botones ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnTokens = new JToggleButton("📊 Ver Tokens", false);
        btnTokens.addActionListener(e -> {
            tokensVisible = btnTokens.isSelected();
            actualizarPaneles();
        });

        btnConsola = new JToggleButton("📜 Ver Consola", false);
        btnConsola.addActionListener(e -> {
            consolaVisible = btnConsola.isSelected();
            actualizarPaneles();
        });

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarInterfaz());

        JButton btnAnalizar = new JButton("▶ ANALIZAR");
        btnAnalizar.setBackground(new Color(0, 120, 215));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAnalizar.putClientProperty("JButton.buttonType", "roundRect");
        
        btnAnalizar.addActionListener(e -> {
            ejecutarAnalisis();
            if (!tokensVisible) { btnTokens.setSelected(true); tokensVisible = true; }
            if (!consolaVisible) { btnConsola.setSelected(true); consolaVisible = true; }
            actualizarPaneles();
        });

        JButton btnTema = new JButton("🌗");
        btnTema.setToolTipText("Cambiar Tema");
        btnTema.addActionListener(e -> cambiarTema());

        pnlBotones.add(btnTokens);
        pnlBotones.add(Box.createHorizontalStrut(5));
        pnlBotones.add(btnConsola);
        pnlBotones.add(Box.createHorizontalStrut(20));
        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnAnalizar);
        pnlBotones.add(Box.createHorizontalStrut(10));
        pnlBotones.add(btnTema);

        add(pnlBotones, BorderLayout.SOUTH);
    }

    private void pintarCodigo() {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = txtEntrada.getStyledDocument();
                String texto = doc.getText(0, doc.getLength());
                
                doc.setCharacterAttributes(0, texto.length(), estiloNormal, true);

                String regexKeywords = "\\b(CREAR|CADENA|NUM|VECTOR|MATRIZ|SI|SINO|FIN_SI|MOSTRAR|MODIFICAR|ELIMINAR|DETERMINANTE|INVERSA|TRANSPUESTA|ADJUNTA|COFACTORES|RANGO|SEN|COS|TAN|RAIZ|POTENCIA|LOG|LN|EXP|PI|LOG10|CELDA|CEROS|COLUMNA|CRUZ|DIAGONAL|AGREGAR)\\b";
                String regexCadenas = "\"[^\"]*\"";
                String regexNumeros = "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b";
                String regexComentarios = "//.*|/\\*[\\s\\S]*?\\*/";

                aplicarEstiloRegex(doc, texto, regexKeywords, estiloPalabraReservada);
                aplicarEstiloRegex(doc, texto, regexNumeros, estiloNumero);
                aplicarEstiloRegex(doc, texto, regexCadenas, estiloCadena);
                aplicarEstiloRegex(doc, texto, regexComentarios, estiloComentario);
                
            } catch (BadLocationException e) {}
        });
    }

    private void aplicarEstiloRegex(StyledDocument doc, String texto, String regex, AttributeSet estilo) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), estilo, false);
        }
    }

    private void actualizarPaneles() {
        if (tokensVisible) {
            splitSuperior.setRightComponent(pnlTabla);
            splitSuperior.setDividerSize(10);
            if (splitSuperior.getDividerLocation() == 0 || splitSuperior.getDividerLocation() > getWidth() - 50) {
                 splitSuperior.setDividerLocation(0.65); 
            }
        } else {
            splitSuperior.setRightComponent(null);
            splitSuperior.setDividerSize(0);
        }

        if (consolaVisible) {
            splitPrincipal.setBottomComponent(scrollSintactico);
            splitPrincipal.setDividerSize(10);
            if (splitPrincipal.getDividerLocation() == 0 || splitPrincipal.getDividerLocation() > getHeight() - 50) {
                 splitPrincipal.setDividerLocation(0.75);
            }
        } else {
            splitPrincipal.setBottomComponent(null);
            splitPrincipal.setDividerSize(0);
        }
        revalidate();
        repaint();
    }

    private void ejecutarAnalisis() {
        String codigoFuente = txtEntrada.getText();
        if (codigoFuente.trim().isEmpty()) return;

        modeloTablaTokens.setRowCount(0);
        txtResultadoSintactico.setText("");

        try {
            Token[] tokensCrudos = Proyecto_Final_Automatas1.tokenizador(codigoFuente);
            AFD afd = Proyecto_Final_Automatas1.obtenerAFD(); 
            List<Token> tokensAnalizados = afd.aceptar(tokensCrudos);

            for (Token tk : tokensAnalizados) {
                modeloTablaTokens.addRow(new Object[]{tk.getLexema(), tk.getTipo(), tk.getLinea()});
            }

            List<Token> tokensParaParser = new ArrayList<>();
            for (Token tk : tokensAnalizados) {
                if (tk.existeSimbolo()) tokensParaParser.add(tk);
            }

            ParserLL1 parser = new ParserLL1(tokensParaParser);
            parser.inicio();

            StringBuilder reporte = new StringBuilder();
            if (parser.errores.hayErrores()) {
                reporte.append("❌ ERRORES ENCONTRADOS:\n");
                for (ErrorCompilacion err : parser.errores.getErrores()) {
                    reporte.append(String.format("• Línea %d: %s\n", err.getLinea(), err.getDescripcion()));
                }
                // AQUÍ USAMOS LA VARIABLE DINÁMICA DE COLOR
                txtResultadoSintactico.setForeground(colorError); 
            } else {
                reporte.append("✅ COMPILACIÓN EXITOSA.\nNo se encontraron errores sintácticos.");
                // AQUÍ USAMOS LA VARIABLE DINÁMICA DE COLOR
                txtResultadoSintactico.setForeground(colorExito); 
            }
            txtResultadoSintactico.setText(reporte.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            txtResultadoSintactico.setText("Error crítico: " + ex.getMessage());
        }
    }

    private void limpiarInterfaz() {
        txtEntrada.setText("");
        modeloTablaTokens.setRowCount(0);
        txtResultadoSintactico.setText("");
        tokensVisible = false; consolaVisible = false; btnTokens.setSelected(false); btnConsola.setSelected(false);
        actualizarPaneles();
    }
    
    private void cambiarTema() {
        try {
            boolean esOscuro = FlatLaf.isLafDark();
            
            if (esOscuro) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                aplicarTemaGlobal(false); // Cambiar a paleta CLARA
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                aplicarTemaGlobal(true); // Cambiar a paleta OSCURA
            }
            
            SwingUtilities.updateComponentTreeUI(this);
            
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void crearMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemAbrir = new JMenuItem("Abrir...");
        itemAbrir.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivoSeleccionado = fileChooser.getSelectedFile();
                StringBuilder contenido = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(archivoSeleccionado))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        contenido.append(linea).append("\n");
                    }
                    txtEntrada.setText(contenido.toString());
                    txtEntrada.setCaretPosition(0); 
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage());
                }
            }
        });
        menuArchivo.add(itemAbrir);
        menuBar.add(menuArchivo);
        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception ex) {}
        SwingUtilities.invokeLater(() -> new CompiladorUI().setVisible(true));
    }
}