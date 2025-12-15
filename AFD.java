import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AFD {
    
    private Set<String> estados;
    private Set<Character> alfabeto;
    private Map<String, Map<Character, String>> transiciones;
    private String estadoInicial;
    private Set<String> estadosAceptacion;
    
    // NUEVA INSTANCIA PARA ERRORES LÉXICOS
    private Errores erroresLexicos = new Errores();

    public AFD(Set<String> estados,
               Set<Character> alfabeto,
               Map<String, Map<Character, String>> transiciones,
               String estadoInicial,
               Set<String> estadosAceptacion) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
    }
    
    // Getter para obtener los errores desde la UI
    public Errores getErrores() {
        return erroresLexicos;
    }

    public List<Token> aceptar(Token[] tokens) {
        // Limpiamos los errores de la ejecución anterior
        erroresLexicos = new Errores(); 
        
        List<Token> resultados = new ArrayList<>();

        for (Token tk : tokens) {
            String lexemaOriginal = tk.getLexema();
            String lexemaUpper = lexemaOriginal.toUpperCase();

            String estadoActual = estadoInicial;
            boolean error = false;

            // Análisis caracter por caracter
            for (char simbolo : lexemaUpper.toCharArray()) {
                // CASO 1: Símbolo no pertenece al alfabeto
                if (!alfabeto.contains(simbolo)) {
                    resultados.add(new Token(
                            lexemaOriginal,
                            tk.getLinea(),
                            TipoToken.DESCONOCIDO,
                            estadoActual,
                            false
                    ));
                    
                    // REGISTRAR ERROR LÉXICO
                    erroresLexicos.agregarError(
                        100, 
                        tk.getLinea(), 
                        "Error Léxico: Símbolo '" + simbolo + "' no definido en el alfabeto."
                    );
                    
                    error = true;
                    break;
                }

                Map<Character, String> transicionesEstado = transiciones.get(estadoActual);
                
                // CASO 2: Transición inválida (rompe la cadena)
                if (transicionesEstado == null || !transicionesEstado.containsKey(simbolo)) {
                    resultados.add(new Token(
                            lexemaOriginal,
                            tk.getLinea(),
                            determinarTipoLexema(lexemaOriginal),
                            estadoActual,
                            true
                    ));
                    
                    // Verificamos si realmente es un error o solo terminó el token.
                    // Si el token resultante es DESCONOCIDO, es un error léxico real.
                    TipoToken tipoDetectado = determinarTipoLexema(lexemaOriginal);
                    if (tipoDetectado == TipoToken.DESCONOCIDO) {
                         erroresLexicos.agregarError(
                            101, 
                            tk.getLinea(), 
                            "Error Léxico: Palabra desconocida o mal formada '" + lexemaOriginal + "'."
                        );
                    }
                    
                    error = true;
                    break;
                }

                estadoActual = transicionesEstado.get(simbolo);
            }

            if (!error) {
                resultados.add(new Token(
                        lexemaOriginal,
                        tk.getLinea(),
                        tipoPR(estadoActual),
                        estadoActual,
                        true
                ));
            }
        }

        return resultados;
    }

    public static TipoToken tipoPR (String estadoActual){
        try {
            return TipoToken.valueOf("PR_" + estadoActual);
        } catch (IllegalArgumentException e) {
            return determinarTipoLexema(estadoActual);
        }
    }

    public static TipoToken determinarTipoLexema(String lexema) {
        // ... (Mismo código de switches y regex que tenías antes) ...
        /* ===============================
           OPERADORES DE COMPARACIÓN
           =============================== */
        switch (lexema) {
            case "==": return TipoToken.OP_IGUAL;
            case "!=": return TipoToken.OP_DIFERENTE;
            case "<":  return TipoToken.OP_MENOR;
            case ">":  return TipoToken.OP_MAYOR;
            case "<=": return TipoToken.OP_MENOR_IGUAL;
            case ">=": return TipoToken.OP_MAYOR_IGUAL;
        }

        /* OPERADORES ARITMÉTICOS */
        switch (lexema) {
            case "+": return TipoToken.OP_SUMA;
            case "-": return TipoToken.OP_RESTA;
            case "*": return TipoToken.OP_MULT;
            case "/": return TipoToken.OP_DIV;
        }

        /* DELIMITADORES */
        if (lexema.equals("(")) return TipoToken.PARENT_IZQ;
        if (lexema.equals(")")) return TipoToken.PARENT_DER;
        if (lexema.equals("[")) return TipoToken.CORCHETE_IZQ;
        if (lexema.equals("]")) return TipoToken.CORCHETE_DER;
        if (lexema.equals(",")) return TipoToken.COMA;
        if (lexema.equals(";")) return TipoToken.PUNTO_Y_COMA;
        if (lexema.equals("_")) return TipoToken.GUION_BAJO;
        if (lexema.equals("=")) return TipoToken.ASIGNACION;

        /* LITERALES */
        if (lexema.matches("^\"[^\"]*\"$")) return TipoToken.CADENA;
        if (lexema.matches("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$")) return TipoToken.NUMERO_REAL;
        if (lexema.matches("^[A-Za-zÑñ][A-Za-z0-9Ññ_]*$")) return TipoToken.IDENTIFICADOR;

        return TipoToken.DESCONOCIDO;
    }
}