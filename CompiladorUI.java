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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiladorUI extends JFrame {

    // --- COMPONENTES GLOBALES ---
    private JTextPane txtEntrada; 
    
    // Componentes para la zona inferior (Pestañas)
    private JTabbedPane pestanasInferior;
    private JTextArea txtConsola;
    private JTable tblErroresLexicos;
    private DefaultTableModel modeloErroresLexicos;
    private JTable tblErroresSintacticos;
    private DefaultTableModel modeloErroresSintacticos;

    private JTable tblTokens;
    private DefaultTableModel modeloTablaTokens;

    // Paneles Ocultables
    private JPanel pnlTabla;
    private JScrollPane scrollTabla;

    // Splits
    private JSplitPane splitSuperior;
    private JSplitPane splitPrincipal;

    // Botones
    private JToggleButton btnTokens;
    private JToggleButton btnConsola;
    private JButton btnVerArbol; 

    // Estado lógico
    private boolean consolaVisible = false;
    private boolean tokensVisible = false; 
    private NodoArbol raizActual = null; 

    // Variables de Color Dinámicas
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
        setTitle("Analizador Léxico y Sintáctico - MATRIX CORE");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        inicializarEstilosBase(); 
        crearMenu();
        inicializarComponentes();
        aplicarTemaGlobal(true);
        actualizarPaneles(); 
    }

    private void inicializarEstilosBase() {
        StyleConstants.setBold(estiloPalabraReservada, true);
        StyleConstants.setItalic(estiloComentario, true);
    }

    private void aplicarTemaGlobal(boolean modoOscuro) {
        if (modoOscuro) {
            colorFondo = new Color(30, 30, 30);
            colorTexto = new Color(220, 220, 220);
            colorError = new Color(255, 100, 100); 
            colorExito = new Color(100, 255, 100); 
            
            StyleConstants.setForeground(estiloNormal, colorTexto);
            StyleConstants.setForeground(estiloPalabraReservada, new Color(197, 134, 192)); 
            StyleConstants.setForeground(estiloCadena, new Color(206, 145, 120)); 
            StyleConstants.setForeground(estiloNumero, new Color(181, 206, 168)); 
            StyleConstants.setForeground(estiloComentario, new Color(106, 153, 85)); 
        } else {
            colorFondo = Color.WHITE;
            colorTexto = Color.BLACK;
            colorError = new Color(200, 0, 0);   
            colorExito = new Color(0, 128, 0);   

            StyleConstants.setForeground(estiloNormal, colorTexto); 
            StyleConstants.setForeground(estiloPalabraReservada, new Color(127, 0, 85)); 
            StyleConstants.setForeground(estiloCadena, new Color(42, 0, 255)); 
            StyleConstants.setForeground(estiloNumero, new Color(0, 128, 0)); 
            StyleConstants.setForeground(estiloComentario, new Color(63, 127, 95)); 
        }

        txtEntrada.setBackground(colorFondo);
        txtEntrada.setCaretColor(modoOscuro ? Color.WHITE : Color.BLACK);
        
        txtConsola.setBackground(colorFondo);
        txtConsola.setForeground(colorTexto);
        
        tblTokens.setBackground(colorFondo);
        tblTokens.setForeground(colorTexto);
        tblTokens.setGridColor(modoOscuro ? new Color(60, 60, 60) : Color.LIGHT_GRAY);
        
        // Colores para tablas de errores
        tblErroresLexicos.setBackground(colorFondo);
        tblErroresLexicos.setForeground(colorError); // Rojo para errores léxicos
        
        tblErroresSintacticos.setBackground(colorFondo);
        tblErroresSintacticos.setForeground(new Color(255, 165, 0)); // Naranja/Dorado para sintácticos

        if (scrollTabla != null) scrollTabla.getViewport().setBackground(colorFondo);

        pintarCodigo();
    }

    private void inicializarComponentes() {
        // --- 1. Panel de Código ---
        JPanel pnlCodigo = new JPanel(new BorderLayout());
        pnlCodigo.setBorder(BorderFactory.createTitledBorder("Código Fuente"));
        
        txtEntrada = new JTextPane();
        txtEntrada.setFont(new Font("Consolas", Font.PLAIN, 15));
        
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

        // --- 2. Panel Tabla Tokens ---
        pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));
        String[] columnasTokens = {"Lexema", "Tipo de Token", "Línea"};
        modeloTablaTokens = new DefaultTableModel(columnasTokens, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTokens = new JTable(modeloTablaTokens);
        tblTokens.setShowGrid(true);
        tblTokens.setFillsViewportHeight(true);
        scrollTabla = new JScrollPane(tblTokens); 
        pnlTabla.add(scrollTabla, BorderLayout.CENTER);

        // --- 3. Panel Inferior (Pestañas de Errores y Consola) ---
        pestanasInferior = new JTabbedPane();
        
        // Pestaña 1: Consola General
        txtConsola = new JTextArea();
        txtConsola.setEditable(false);
        txtConsola.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollConsola = new JScrollPane(txtConsola);
        pestanasInferior.addTab("📜 Consola", scrollConsola);

        // Pestaña 2: Tabla Errores Léxicos
        String[] colsErr = {"Código Error", "Línea", "Descripción"};
        modeloErroresLexicos = new DefaultTableModel(colsErr, 0);
        tblErroresLexicos = new JTable(modeloErroresLexicos);
        tblErroresLexicos.setShowGrid(true);
        pestanasInferior.addTab("Errores Léxicos", new JScrollPane(tblErroresLexicos));

        // Pestaña 3: Tabla Errores Sintácticos
        modeloErroresSintacticos = new DefaultTableModel(colsErr, 0);
        tblErroresSintacticos = new JTable(modeloErroresSintacticos);
        tblErroresSintacticos.setShowGrid(true);
        pestanasInferior.addTab("Errores Sintácticos", new JScrollPane(tblErroresSintacticos));
        
        pestanasInferior.setMinimumSize(new Dimension(0, 150));

        // --- Splits ---
        splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitSuperior.setLeftComponent(pnlCodigo); 
        splitSuperior.setResizeWeight(0.7); 
        splitSuperior.setContinuousLayout(true);
        splitSuperior.setBorder(null);

        splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPrincipal.setTopComponent(splitSuperior);
        splitPrincipal.setResizeWeight(0.7); 
        splitPrincipal.setContinuousLayout(true);

        add(splitPrincipal, BorderLayout.CENTER);

        // --- Botones ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnTokens = new JToggleButton("📊 Ver Tokens", false);
        btnTokens.addActionListener(e -> {
            tokensVisible = btnTokens.isSelected();
            actualizarPaneles();
        });

        btnConsola = new JToggleButton("📜 Ver Resultados", false);
        btnConsola.addActionListener(e -> {
            consolaVisible = btnConsola.isSelected();
            actualizarPaneles();
        });
        
        btnVerArbol = new JButton("🌳 Ver Árbol");
        btnVerArbol.setEnabled(false); 
        btnVerArbol.addActionListener(e -> {
            if (raizActual != null) new VentanaArbol(raizActual).setVisible(true);
        });

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarInterfaz());

        JButton btnAnalizar = new JButton("ANALIZAR");
        btnAnalizar.setBackground(new Color(45, 216, 129));
        btnAnalizar.setForeground(Color.WHITE);
        btnAnalizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAnalizar.putClientProperty("JButton.buttonType", "roundRect");
        
        btnAnalizar.addActionListener(e -> {
            ejecutarAnalisis();
            if (!tokensVisible) { btnTokens.setSelected(true); tokensVisible = true; }
            if (!consolaVisible) { btnConsola.setSelected(true); consolaVisible = true; }
            actualizarPaneles();
        });

        pnlBotones.add(btnTokens);
        pnlBotones.add(Box.createHorizontalStrut(5));
        pnlBotones.add(btnConsola);
        pnlBotones.add(Box.createHorizontalStrut(5));
        pnlBotones.add(btnVerArbol);
        pnlBotones.add(Box.createHorizontalStrut(20));
        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnAnalizar);
        pnlBotones.add(Box.createHorizontalStrut(10));

        add(pnlBotones, BorderLayout.SOUTH);
    }

    private void pintarCodigo() {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = txtEntrada.getStyledDocument();
                String texto = doc.getText(0, doc.getLength());
                
                doc.setCharacterAttributes(0, texto.length(), estiloNormal, true);

                String regexKeywords = "(?U)(?i)\\b(CREAR|CADENA|NUM|VECTOR|MATRIZ|SI|SINO|FIN_SI|TAMAÑO|MOSTRAR|MODIFICAR|ELIMINAR|DETERMINANTE|INVERSA|TRANSPUESTA|ADJUNTA|COFACTORES|RANGO|SEN|COS|TAN|RAIZ|POTENCIA|LOG|LN|EXP|PI|LOG10|CELDA|CEROS|COLUMNA|CRUZ|DIAGONAL|AGREGAR|METODO|DE)\\b";
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
            splitPrincipal.setBottomComponent(pestanasInferior); // AHORA USAMOS LAS PESTAÑAS
            splitPrincipal.setDividerSize(10);
            if (splitPrincipal.getDividerLocation() == 0 || splitPrincipal.getDividerLocation() > getHeight() - 50) {
                 splitPrincipal.setDividerLocation(0.70);
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

        // 1. Limpiezas iniciales
        modeloTablaTokens.setRowCount(0);
        modeloErroresLexicos.setRowCount(0);
        modeloErroresSintacticos.setRowCount(0);
        txtConsola.setText("");
        raizActual = null; 
        btnVerArbol.setEnabled(false);

        try {
            // 2. Tokenización
            Token[] tokensCrudos = Proyecto_Final_Automatas1.tokenizador(codigoFuente);
            AFD afd = Proyecto_Final_Automatas1.obtenerAFD(); 
            List<Token> tokensAnalizados = afd.aceptar(tokensCrudos);

            // 3. Llenar tabla de tokens y Tabla de Errores Léxicos
            for (Token tk : tokensAnalizados) {
                modeloTablaTokens.addRow(new Object[]{tk.getLexema(), tk.getTipo(), tk.getLinea()});
            }
            
            // OBTENER ERRORES LÉXICOS DEL AFD
            Errores errLex = afd.getErrores();
            if (errLex.hayErrores()) {
                for (ErrorCompilacion err : errLex.getErrores()) {
                    modeloErroresLexicos.addRow(new Object[]{err.getNumero(), err.getLinea(), err.getDescripcion()});
                }
                pestanasInferior.setSelectedIndex(1); // Enfocar pestaña de errores léxicos
                txtConsola.append("Se encontraron errores léxicos. Revisa la pestaña correspondiente.\n");
                txtConsola.setForeground(colorError);
            }

            // 4. Filtrar tokens válidos para el Parser
           Set<Integer> lineasConError = new HashSet<>();
            for (Token tk : tokensAnalizados) {
                if (!tk.existeSimbolo()) {
                    lineasConError.add(tk.getLinea());
                }
            }

            // Preparar tokens para parser: solo líneas sin errores
            List<Token> tokensParaParser = new ArrayList<>();
            List<Token> tokensErrorLexico = new ArrayList<>();

            for (Token tk : tokensAnalizados) {
                if (lineasConError.contains(tk.getLinea())) { 
                    // Toda la línea se omite del análisis sintáctico
                    tokensErrorLexico.add(tk);
                } else {
                    tokensParaParser.add(tk);
                }
            }
            
            // 5. Análisis Sintáctico
            if(tokensParaParser.size() < 1){
                System.out.println("No hay ningún token válido como para realizar un análisis sintáctico");
            }else{
                ParserLL1 parser = new ParserLL1(tokensParaParser);
                parser.inicio();

                // 6. Llenar Tabla de Errores Sintácticos
                if (parser.errores.hayErrores()) {
                    for (ErrorCompilacion err : parser.errores.getErrores()) {
                        modeloErroresSintacticos.addRow(new Object[]{err.getNumero(), err.getLinea(), err.getDescripcion()});
                    }
                    
                    txtConsola.append("Se encontraron errores sintácticos.\n");
                    if (!errLex.hayErrores()) {
                        // Si no hubo errores léxicos pero sí sintácticos, enfocar sintácticos
                        pestanasInferior.setSelectedIndex(2);
                        txtConsola.setForeground(colorError);
                    }
                } else if (!errLex.hayErrores()) {
                    // Si no hay errores de ningún tipo
                    txtConsola.append("COMPILACIÓN EXITOSA.\nNo se encontraron errores.");
                    txtConsola.setForeground(colorExito); 
                    pestanasInferior.setSelectedIndex(0); // Enfocar consola
                    
                    // Generar Árbol
                    raizActual = parser.getRaiz();
                    if (raizActual != null) {
                        btnVerArbol.setEnabled(true);
                    }
                }
            }
            

        } catch (Exception ex) {
            ex.printStackTrace();
            txtConsola.setText("Error crítico: " + ex.getMessage());
        }
    }

    private void limpiarInterfaz() {
        txtEntrada.setText("");
        modeloTablaTokens.setRowCount(0);
        modeloErroresLexicos.setRowCount(0);
        modeloErroresSintacticos.setRowCount(0);
        txtConsola.setText("");
        tokensVisible = false; consolaVisible = false; 
        btnTokens.setSelected(false); btnConsola.setSelected(false);
        raizActual = null;
        btnVerArbol.setEnabled(false);
        actualizarPaneles();
    }
    
    private void cambiarTema() {
        try {
            boolean esOscuro = FlatLaf.isLafDark();
            if (esOscuro) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                aplicarTemaGlobal(false); 
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                aplicarTemaGlobal(true); 
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