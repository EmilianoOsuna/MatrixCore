import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AFD {
    
    private Set<String> estados;                     // Conjunto de estados
    private Set<Character> alfabeto;                 // Alfabeto de entrada
    private Map<String, Map<Character, String>> transiciones; // Función de transición
    private String estadoInicial;                    // Estado inicial
    private Set<String> estadosAceptacion;           // Estados de aceptación

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

        // Método principal para procesar una lista de tokens
        public List<Token> aceptar(Token[] tokens) {
            List<Token> resultados = new ArrayList<>();

            for (Token tk : tokens) {

                String estadoActual = estadoInicial;
                boolean error = false;

                for (char simbolo : tk.getLexema().toCharArray()) {

                    if (!alfabeto.contains(simbolo)) {
                        // Símbolo no reconocido
                        resultados.add(
                            new Token(
                                tk.getLexema(),
                                tk.getLinea(),
                                TipoToken.DESCONOCIDO,
                                estadoActual,
                                false
                            )
                        );
                        error = true;
                        break;
                    }

                    Map<Character, String> transicionesEstado = transiciones.get(estadoActual);

                    if (transicionesEstado == null || !transicionesEstado.containsKey(simbolo)) {
                        // No hay transición válida
                        resultados.add(
                            new Token(
                                tk.getLexema(),
                                tk.getLinea(),
                                determinarTipoLexema(tk.getLexema()),
                                estadoActual,
                                true
                            )
                        );
                        error = true;
                        break;
                    }

                    estadoActual = transicionesEstado.get(simbolo);
                }

                // Análisis terminó normalmente
                if (!error) {
                    resultados.add(
                        new Token(
                            tk.getLexema(),
                            tk.getLinea(),
                            tipoPR(estadoActual),
                            estadoActual,
                            true
                        )
                    );
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

        /* ===============================
           OPERADORES DE COMPARACIÓN
           =============================== */
        switch (lexema) {
            case "==":
                return TipoToken.OP_IGUAL;
            case "!=":
                return TipoToken.OP_DIFERENTE;
            case "<":
                return TipoToken.OP_MENOR;
            case ">":
                return TipoToken.OP_MAYOR;
            case "<=":
                return TipoToken.OP_MENOR_IGUAL;
            case ">=":
                return TipoToken.OP_MAYOR_IGUAL;
        }

        /* ===============================
           OPERADORES ARITMÉTICOS
           =============================== */
        switch (lexema) {
            case "+":
                return TipoToken.OP_SUMA;
            case "-":
                return TipoToken.OP_RESTA;
            case "*":
                return TipoToken.OP_MULT;
            case "/":
                return TipoToken.OP_DIV;
        }

        /* ===============================
           DELIMITADORES
           =============================== */
        if (lexema.equals("(")) return TipoToken.PARENT_IZQ;
        if (lexema.equals(")")) return TipoToken.PARENT_DER;
        if (lexema.equals("[")) return TipoToken.CORCHETE_IZQ;
        if (lexema.equals("]")) return TipoToken.CORCHETE_DER;
        if (lexema.equals(",")) return TipoToken.COMA;
        if (lexema.equals(";")) return TipoToken.PUNTO_Y_COMA;
        if (lexema.equals("_")) return TipoToken.GUION_BAJO;
        if (lexema.equals("=")) return TipoToken.ASIGNACION;

        /* ===============================
           CADENA ENTRE COMILLAS
           =============================== */
        if (lexema.matches("^\"[^\"]*\"$")) {
            return TipoToken.CADENA;
        }

        /* ===============================
           NÚMERO (real / entero / científico)
           =============================== */
        if (lexema.matches("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$")) {
            return TipoToken.NUMERO_REAL;
        }

        /* ===============================
           IDENTIFICADOR
           =============================== */
        if (lexema.matches("^[A-Za-zÑñ][A-Za-z0-9Ññ_]*$")) {
            return TipoToken.IDENTIFICADOR;
        }

        return TipoToken.DESCONOCIDO;
    }

}
