import java.util.List;
import java.util.Set;

public class ParserLL1 {
    Errores errores = new Errores();
    
    private List<Token> tokens;
    private int pos = 0, lineaFinal = -2;
    private boolean panicoUsado = false, lFinal = false;

    public ParserLL1(List<Token> tokens) {
        this.tokens = tokens;
    }

   private static final Set<TipoToken> TOKENS_SEGUROS_DEFAULT = Set.of(
        TipoToken.PUNTO_Y_COMA,
        TipoToken.PR_CREAR,
        TipoToken.PR_SI,
        TipoToken.PR_MOSTRAR,
        TipoToken.PR_MODIFICAR,
        TipoToken.PR_OBTENER,
        TipoToken.EOF
    );

    private void panico() {
        panico(TOKENS_SEGUROS_DEFAULT);
    }

    private void panico(Set<TipoToken> tokensSeguros) {
        // Avanzar hasta encontrar un token seguro
        panicoUsado = true;
       
        if(tokensSeguros.contains(tokenActual().getTipo()) && tokenActual().getTipo() != TipoToken.PUNTO_Y_COMA){
        }   
        else{
            while (tokenActual().getTipo() != TipoToken.EOF &&
                   !tokensSeguros.contains(tokenActual().getTipo())) {
                pos++;
            }
            if (tokenActual().getTipo() == TipoToken.PUNTO_Y_COMA) {
                pos++;
                while (tokenActual().getTipo() != TipoToken.EOF &&
                   !tokensSeguros.contains(tokenActual().getTipo())) {
                pos++;
                }
            }
        }
    }

    private Token tokenActual() {
        if (pos >= tokens.size()) {
            //Guardar la última linea por si es necesario marcar un error:
            lineaFinal = tokens.get(pos-1).getLinea();
            lFinal = true;
            // Retornar un token EOF ficticio para que el analizador lo detecte y termine el analisis.
            return new Token("", -1, TipoToken.EOF,"",true);
        }
        return tokens.get(pos);
    }
    
    private Token tokenAnterior(){
        return tokens.get(pos-1);        
    }

    private void match(TipoToken esperado,String msg) {
        if (panicoUsado) return ; 

        if (tokenActual().getTipo() == esperado) {
            pos++;
        } else {
                errores.agregarError(101, tokens.get(pos - 1).getLinea(),msg );
                panico();
        }
    }
//-----------------------------------------------------------------------------------------------------------
// -------------------------------------INICIO DEL PARSER----------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
inicio() → prCrear() | prModificar    
*/
    public void inicio() {
        while (tokenActual().getTipo() != TipoToken.EOF) {
            switch(tokenActual().getTipo()){
            case PR_CREAR:
                prCrear();               
                System.out.println("Tipo: " + tokenActual().getTipo());

                break;
            case PR_MODIFICAR:
                prModificar();
                break;
            case PR_OBTENER:
                 prObtener();
                 break;
            default:
                errores.agregarError(1,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera una acción inicial como CREAR, MODIFICAR, OBTENER en vez de: " + tokenActual().getLexema()
                );
                panico();
            }
            panicoUsado = false;
        }
        match(TipoToken.EOF,"Algo salió mal.");
    }

    
//---------------------------------------------------------------------------------------------------------------
// -------------------------------------CREACIÓN DE VARIABLES----------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
prCrear() → prCadena() | prNum() | prVector() | prMatriz()
     
*/
    private void prCrear() {

        if (tokenActual().getTipo() != TipoToken.PR_CREAR) {
            errores.agregarError(101,tokenActual().getLinea(),"Se esperaba 'CREAR'");
            panico();
            return;
        }

        match(TipoToken.PR_CREAR,"Se esperaba 'CREAR'");

        // Decidir qué tipo se va a crear
        switch (tokenActual().getTipo()) {

            case PR_CADENA:
                prCadena();
                break;

            case PR_NUM:
                prNum();
                break;

            case PR_VECTOR:
                prVector();
                break;

            case PR_MATRIZ:
                prMatriz();
                break;

            default:
                errores.agregarError(1,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera un tipo de variable a crear, por ejemplo: CADENA, NUM, VECTOR o MATRIZ"
                );
                panico();
        }
    }

//------------------------------------------------------------------------------------------------------
// -------------------------------------CREAR CADENA----------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
prCadena() → IDENTIFICADOR = expresionCadena() ;

expresionCadena() → operandoCadena() restoCadena() ;

restoCadena() → + operandoCadena() restoCadena() | ε

operandoCadena() → CADENA
                | IDENTIFICADOR
                | '(' expresionNumerica() ')'
*/
    private void prCadena() {
        match(TipoToken.PR_CADENA,"Se espera 'CADENA' después de 'CREAR'");          
        match(TipoToken.IDENTIFICADOR,"Se espera el nombre de la CADENA");
        match(TipoToken.ASIGNACION,"Se espera el operador de ASIGNACIÓN '=' después del identificador: '" + tokens.get(pos - 1).getLexema() + "'.");
        expresionCadena();
        
        validacionFin(TipoToken.PARENT_IZQ,TipoToken.PARENT_DER,
        "Sobra un PARÉNTESIS de inicio '('."
        + " En caso de querer comenzar una operación numérica dentro de un paréntesis, es necesario agregar un opeardor de suma antes de abrir un paréntesis",
        "Sobra un PARÉNTESIS de cierre ')' ");
        match(TipoToken.PUNTO_Y_COMA,"Se espera un ';' para dar fin a la instrucción");
    }
    private void expresionCadena() {
        if (panicoUsado) return;
        operandoCadena();
        restoCadena();
    }    
    private void operandoCadena() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case CADENA:
                match(TipoToken.CADENA,
                    "Se esperaba una cadena literal.");
                break;

            case IDENTIFICADOR:
                match(TipoToken.IDENTIFICADOR,
                    "Se esperaba un identificador válido.");
                break;

            case PARENT_IZQ:
                match(TipoToken.PARENT_IZQ,
                    "Se esperaba '(' para iniciar una expresión numérica.");

                expresionNumerica();

                match(TipoToken.PARENT_DER,
                    "Se esperaba ')' para cerrar la expresión numérica.");
                break;

            default:
                errores.agregarError(
                    103,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una cadena, un identificador o una expresión numérica"
                    + " entre paréntesis después de '" + tokenAnterior().getLexema() + "'"
                );
                panico();
        }
    }

    private void restoCadena() {
        if (panicoUsado) return;
        if (tokenActual().getTipo() == TipoToken.OP_SUMA) {
            match(TipoToken.OP_SUMA,"Se espera un operador de suma (+).");
            operandoCadena();
            restoCadena();
        }
    }
    
    
//------------------------------------------------------------------------------------------------------
// -------------------------------------CREAR NUM-------------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
prNum() → IDENTIFICADOR = expresionNumerica() ';
    
expresionNumerica() → terminoNumerico() continuacionSumaResta()

continuacionSumaResta() → '+' terminoNumerico() continuacionSumaResta()
                        | '-' terminoNumerico() continuacionSumaResta() | ε

terminoNumerico() → factorNumerico() continuacionMultiplicacionDivision()


continuacionMultiplicacionDivision() → '*' factorNumerico() continuacionMultiplicacionDivision()
                        | '/' factorNumerico() continuacionMultiplicacionDivision() | ε

factorNumerico() → SIGNO operandoNumerico() | operandoNumerico()

SIGNO → '+' | '-'

operandoNumerico() → NUMERO_REAL | IDENTIFICADOR | funcionNumerica() | OBTENER obtenerCelda() |'(' expresionNumerica() ')'

funcionNumerica()
    → PR_RAIZ      unArgumento()
    | PR_POT       dosArgumentos()
    | PR_COS       unArgumento()
    | PR_SEN       unArgumento()
    | PR_TAN       unArgumento()
    | PR_ARCSEN    unArgumento()
    | PR_ARCCOS    unArgumento()
    | PR_ARCTAN    unArgumento()
    | PR_LN        unArgumento
    | PR_LOG       dosArgumentos()
    | PR_LOG10     unArgumento()
    | PR_PI
    | PR_EXP       unArgumento()

unArgumento() → '(' operandoSimple() ')'

dosArgumentos() → '(' operandoSimple() ',' operandoSimple() ')'

operandoSimple() → expresionNumerica()
*/
    private void prNum() {
        match(TipoToken.PR_NUM,
            "Se espera la palabra reservada NUM para crear una variable numérica.");

        match(TipoToken.IDENTIFICADOR,
            "Se espera el identificador del número a crear.");

        match(TipoToken.ASIGNACION,
            "Se espera el operador de asignación '=' después del identificador '"
            + tokenAnterior().getLexema() + "'.");

        expresionNumerica(); 
        validacionFin(TipoToken.PARENT_IZQ,TipoToken.PARENT_DER,
        "Sobra un PARÉNTESIS de inicio '(' al final de la declaración de la EXPRESIÓN NUMÉRICA."
        + " En caso de querer comenzar otra operación es necesario agregar un opeardor aritmético antes de abrir un paréntesis",
        "Sobra un PARÉNTESIS de cierre ')' al final de la declaración de la EXPRESIÓN NUMÉRICA.");
        
        match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' para finalizar la declaración del número.");
    }

    private void expresionNumerica() {
        if (panicoUsado) return;

        terminoNumerico();
        continuacionSumaResta();
    }

    private void continuacionSumaResta() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {

            match(tokenActual().getTipo(),
                "Se esperaba un operador aritmético válido '+' o '-'.");

            terminoNumerico();
            continuacionSumaResta();
        }
    }

    private void terminoNumerico() {
        if (panicoUsado) return;

        factorNumerico();
        continuacionMultiplicacionDivision();
    }

    private void continuacionMultiplicacionDivision() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.OP_MULT ||
            tokenActual().getTipo() == TipoToken.OP_DIV) {

            match(tokenActual().getTipo(),
                "Se esperaba un operador aritmético '*' o '/'.");

            factorNumerico();
            continuacionMultiplicacionDivision();
        }
    }

    private void factorNumerico() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {

            match(tokenActual().getTipo(),
                "Se esperaba un signo '+' o '-' válido.");

            operandoNumerico();
        } else {
            operandoNumerico();
        }
    }


    private void operandoNumerico() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case NUMERO_REAL:
                match(TipoToken.NUMERO_REAL,
                    "Se esperaba un número real válido.");
                break;

            case IDENTIFICADOR:
                match(TipoToken.IDENTIFICADOR,
                    "Se esperaba un identificador numérico válido.");
                break;

            case PARENT_IZQ:
                match(TipoToken.PARENT_IZQ,
                    "Se esperaba '(' para iniciar una expresión numérica.");
                expresionNumerica();
                match(TipoToken.PARENT_DER,
                    "Se esperaba ')' para cerrar la expresión numérica.");
                break;

            case PR_RAIZ:
            case PR_POTENCIA:
            case PR_COS:
            case PR_SEN:
            case PR_TAN:
            case PR_ARCSEN:
            case PR_ARCCOS:
            case PR_ARCTAN:
            case PR_LN:
            case PR_LOG:
            case PR_LOG10:
            case PR_PI:
            case PR_EXP:
                funcionNumerica();
                break;
            case PR_OBTENER:
                match(TipoToken.PR_OBTENER, 
                    "Se espera la palabra reservada 'OBTENER'.");
                obtenerCelda();
                break;

            default:
                errores.agregarError(
                    210,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un número real, un identificador, "
                    + "una función numérica o una expresión entre paréntesi después de : '"
                    + tokenAnterior().getLexema() + "', pero se encontró: " + tokenActual().getLexema()
                );
                panico();
        }
    }

    private void funcionNumerica() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case PR_RAIZ:
                match(TipoToken.PR_RAIZ,"");
                unArgumento("RAIZ(x)");
                break;

            case PR_POTENCIA:
                match(TipoToken.PR_POTENCIA,"");
                dosArgumentos("POTENCIA(base,exponente)");
                break;

            case PR_COS:
                match(TipoToken.PR_COS,"");
                unArgumento("COS(x)");
                break;

            case PR_SEN:
                match(TipoToken.PR_SEN,"");
                unArgumento("SEN(x)");
                break;

            case PR_TAN:
                match(TipoToken.PR_TAN,"");
                unArgumento("TAN(x)");
                break;

            case PR_ARCSEN:
                match(TipoToken.PR_ARCSEN,"");
                unArgumento("ARCSEN(x)");
                break;

            case PR_ARCCOS:
                match(TipoToken.PR_ARCCOS,"");
                unArgumento("ARCCOS(x)");
                break;

            case PR_ARCTAN:
                match(TipoToken.PR_ARCTAN,"");
                unArgumento("ARCTAN(x)");
                break;

            case PR_LN:
                match(TipoToken.PR_LN,"");
                unArgumento("LN(x)");
                break;

            case PR_LOG:
                match(TipoToken.PR_LOG,"");
                dosArgumentos("LOG(base,argumento)");
                break;

            case PR_LOG10:
                match(TipoToken.PR_LOG10,"");
                unArgumento("LOG10(x)");
                break;

            case PR_PI:
                match(TipoToken.PR_PI,
                    "PI es una constante y no recibe argumentos.");
                break;

            case PR_EXP:
                match(TipoToken.PR_EXP,"");
                unArgumento("EXP(exponente)");
                break;

            default:
                errores.agregarError(
                    211,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una función numérica válida."
                );
                panico();
        }
    }

    private void unArgumento(String esperado) {
        match(TipoToken.PARENT_IZQ,
            "Se esperaba el argumento de la función con la forma: " + esperado);

        operandoSimple();
        
        match(TipoToken.PARENT_DER,
            "Se esperaba el argumento de la función con la forma: " + esperado);
    }

    private void dosArgumentos(String esperado) {
        match(TipoToken.PARENT_IZQ,
            "Se esperaban los argumentos de la función con la forma: " + esperado);

        operandoSimple();

        match(TipoToken.COMA,
            "Se esperaban dos argumentos separados por una coma ','. Ejemplo: " + esperado);
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        operandoSimple();

        match(TipoToken.PARENT_DER,
            "Se esperaban los argumentos de la función con la forma: " + esperado);
    }

    private void operandoSimple() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case NUMERO_REAL:
            case IDENTIFICADOR:
            case PR_RAIZ:
            case PR_POTENCIA:
            case PR_COS:
            case PR_SEN:
            case PR_TAN:
            case PR_ARCSEN:
            case PR_ARCCOS:
            case PR_ARCTAN:
            case PR_LN:
            case PR_LOG:
            case PR_LOG10:
            case PR_PI:
            case PR_EXP:
            case PR_OBTENER:
                expresionNumerica();
                break;

            default: 
                if(tokenAnterior().getLexema().equals("(") && tokenActual().getLexema().equals(")")){ 
                    errores.agregarError( 203, lFinal ?  lineaFinal : tokenActual().getLinea(),
                            "El argumento de la función no debe estar vacío." );
                } 
                else{ errores.agregarError( 203, lFinal ?  lineaFinal : tokenActual().getLinea(), 
                        "El argumento de la función debe ser un número real, un identificador o una expresión numérica válida.");
                        }
                panico();
        }
    }


//------------------------------------------------------------------------------------------------------
// -------------------------------------CREAR VECTOR----------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
prVector() → IDENTIFICADOR = expresionVector() ;

expresionVector() → '[' listaElementosVector() ']' | OBTENER obtenerFila() | OBTENER obtenerColumna()

listaElementosVector() → expresionNumerica() ',' expresionNumerica() restoElementosVector()

restoElementosVector() → ',' expresionNumerica() restoElementosVector()
                        | ε

*/

    private void prVector() {
        match(TipoToken.PR_VECTOR, "Se espera 'VECTOR' después de 'CREAR'.");
        match(TipoToken.IDENTIFICADOR, "Se espera el identificador del vector a crear.");
        match(TipoToken.ASIGNACION,
                "Se espera el operador de ASIGNACIÓN '=' después del identificador: '" +
                tokenAnterior().getLexema() + "'.");
        expresionVector();
        match(TipoToken.PUNTO_Y_COMA, "Se espera un ';' para dar fin a la declaración del vector.");
    }

        private void expresionVector() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case CORCHETE_IZQ:
                match(
                    TipoToken.CORCHETE_IZQ,
                    "Se espera '[' para iniciar la definición del vector."
                );

                listaElementosVector();

                match(
                    TipoToken.CORCHETE_DER,
                    "Se espera ']' para cerrar la definición del vector."
                );
                validacionFin(
                    TipoToken.CORCHETE_IZQ,
                    TipoToken.CORCHETE_DER,
                    "Sobra un corchete de inicio '[' en la definición del vector.",
                    "Sobra un corchete de cierre ']' en la definición del vector."
                );
                break;

            case PR_OBTENER:
                match(
                    TipoToken.PR_OBTENER,
                    "Se espera la palabra reservada 'OBTENER'."
                );

                switch (tokenActual().getTipo()) {

                    case PR_FILA:
                        obtenerFila();
                        break;

                    case PR_COLUMNA:
                        obtenerColumna();
                        break;

                    default:
                        errores.agregarError(
                            310,
                            lFinal ? lineaFinal : tokenActual().getLinea(),
                            "Después de 'OBTENER' se esperaba FILA o COLUMNA."
                        );
                        panico();
                        return;
                }
                break;

            default:
                errores.agregarError(
                    309,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una definición de vector: "
                  + "un vector literal [ ... ] o una obtención con OBTENER FILA/COLUMNA."
                );
                panico();
        }
    }


    private void listaElementosVector() {
        if (panicoUsado) return;

        // Primer elemento del vetor
        expresionNumerica();
        if (panicoUsado) return;

        // Validando que el vector sea de al menos dos elementos
        if (tokenActual().getTipo() != TipoToken.COMA) {
            errores.agregarError(
                401,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                "Un vector debe contener al menos dos elementos (n ≥ 2). Debe estar expresado de la siguiente forma: VECTOR[a,b]"
            );
            panico();
            return;
        }

        match(TipoToken.COMA,
                "Se espera ',' para separar los elementos del vector.");
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

        // Segundo elemento (obligatorio)
        expresionNumerica();
        if (panicoUsado) return;

        // Resto opcional
        restoElementosVector();
    }

    private void restoElementosVector() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA,
                    "Se espera ',' para separar los elementos del vector.");
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
            expresionNumerica();
            restoElementosVector();
        }
    }



//------------------------------------------------------------------------------------------------------
// -------------------------------------CREAR MATRIZ----------------------------------------------------    
/*
────────────────────────────────────────────────────────────
CREACIÓN DE MATRICES
────────────────────────────────────────────────────────────

prMatriz()
    → MATRIZ definicionMatriz()

definicionMatriz()
    → matrizConTipo()
    | matrizNormal()

────────────────────────────────────────────────────────────
MATRIZ CON TIPO (IDENTIDAD, CEROS, etc.)
────────────────────────────────────────────────────────────

matrizConTipo()
    → tipoMatriz() tamañoMatriz() IDENTIFICADOR ';'

tipoMatriz()
    → IDENTIDAD
    | DIAGONAL
    | CEROS
    | UNOS

────────────────────────────────────────────────────────────
MATRIZ NORMAL
────────────────────────────────────────────────────────────

matrizNormal()
    → tamañoMatriz() IDENTIFICADOR finMatriz()

finMatriz()
    → '=' expresionMatriz() ';'

────────────────────────────────────────────────────────────
EXPRESIÓN DE MATRIZ
────────────────────────────────────────────────────────────

expresionMatriz()
    → terminoMatriz() continuacionSumaRestaMatriz()
    | '[' listaFilas() ']'

────────────────────────────────────────────────────────────
OPERACIONES ENTRE MATRICES
(SOLO IDENTIFICADORES)
────────────────────────────────────────────────────────────

terminoMatriz()
    → IDENTIFICADOR continuacionMultEscalar()

continuacionMultEscalar()
    → '*' expresionNumerica() continuacionMultEscalar()
    | '/' expresionNumerica() continuacionMultEscalar()
    | ε

continuacionSumaRestaMatriz()
    → '+' terminoMatriz() continuacionSumaRestaMatriz()
    | '-' terminoMatriz() continuacionSumaRestaMatriz()
    | ε

────────────────────────────────────────────────────────────
MATRIZ LITERAL
────────────────────────────────────────────────────────────

listaFilas()
    → filaMatriz() ',' filaMatriz() restoFilas()

restoFilas()
    → ',' filaMatriz() restoFilas()
    | ε

filaMatriz()
    → '[' listaElementosFila() ']'

listaElementosFila()
    → expresionNumerica() ',' expresionNumerica() restoElementosFila()

restoElementosFila()
    → ',' expresionNumerica() restoElementosFila()
    | ε

────────────────────────────────────────────────────────────
TAMAÑO
────────────────────────────────────────────────────────────

tamañoMatriz()
    → TAMAÑO '(' expresionNumerica() ',' expresionNumerica() ')'

*/   

    private void prMatriz() {
        match(TipoToken.PR_MATRIZ, "Se espera la palabra reservada 'MATRIZ' después de 'CREAR'.");
        definicionMatriz();
    }

    /* definicionMatriz → matrizConTipo | matrizNormal */
    private void definicionMatriz() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {
            case PR_IDENTIDAD:
            case PR_DIAGONAL:
            case PR_CEROS:
            case PR_UNOS:
                matrizConTipo();
                break;

            case PR_TAMAÑO:
                matrizNormal();
                break;

            default:
                errores.agregarError(
                    400,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un tipo de matriz (IDENTIDAD, DIAGONAL, CEROS, UNOS) "
                    + "o una definición normal con TAMAÑO(filas,columnas)."
                );
                panico();
        }
    }

    /* matrizConTipo → tipoMatriz tamañoMatriz IDENTIFICADOR ';' */
    private void matrizConTipo() {
        if (panicoUsado) return;
        tipoMatriz();
        tamañoMatriz();
        match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz.");
        if (tokenActual().getTipo() != TipoToken.PUNTO_Y_COMA){
            if (panicoUsado) return;
            errores.agregarError(
                    401,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "En una matriz definida por tipo y tamaño se espera un ';' después de el identificador para dar fin a su declaración."
                );
            panico();
            return;
        }
        match(TipoToken.PUNTO_Y_COMA, "Se espera ';' al final de la declaración de la matriz.");
    }

    /* tipoMatriz → IDENTIDAD | DIAGONAL | CEROS | UNOS */
    private void tipoMatriz() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {
            case PR_IDENTIDAD:
            case PR_DIAGONAL:
            case PR_CEROS:
            case PR_UNOS:
                match(tokenActual().getTipo(), "Se espera un tipo válido de matriz.");
                break;

            default:
                errores.agregarError(
                    401,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Tipo de matriz inválido. Se esperaba IDENTIDAD, DIAGONAL, CEROS o UNOS."
                );
                panico();
        }
    }

    /* matrizNormal → tamañoMatriz IDENTIFICADOR finMatriz */
    private void matrizNormal() {
        if (panicoUsado) return;
        tamañoMatriz();
        match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz.");
        finMatriz();
        
    }

    private void finMatriz() {
        if (panicoUsado) return;
        match(TipoToken.ASIGNACION, "Se espera '=' para asignar los valores de la matriz.");
        expresionMatriz();
        match(TipoToken.PUNTO_Y_COMA, "Se espera ';' al final de la definición de la matriz.");
    }

    private void tamañoMatriz() {
        if (panicoUsado) return;
        match(TipoToken.PR_TAMAÑO, "Se espera la palabra reservada 'TAMAÑO', después de: '" + tokenAnterior().getLexema() + "'");
        match(TipoToken.PARENT_IZQ, "Se espera '(' después de TAMAÑO.");
        expresionNumerica();

        match(TipoToken.COMA, "Se espera ',' para separar el tammaño de las filas y columnas.");
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        expresionNumerica();

        match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la definición del tamaño.");
    }

    /* expresionMatriz → '[' listaFilas ']' */
    private void expresionMatriz() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {

            case CORCHETE_IZQ:
                // Matriz literal
                match(TipoToken.CORCHETE_IZQ, "Se espera '[' para iniciar la matriz.");
                listaFilas();
                match(TipoToken.CORCHETE_DER, "Se espera ']' para cerrar la matriz.");
                validacionFin(TipoToken.CORCHETE_IZQ,TipoToken.CORCHETE_DER,
                "Sobra un CORCHETE de inicio '[' al final de la declaración de la MATRIZ.",
                "Sobra un CORCHETE de cierre ']' al final de la declaración de la MATRIZ.");
                break;

            case IDENTIFICADOR:
                // Operaciones entre matrices
                expresionOperacionMatriz();
                break;

            default:
                errores.agregarError(
                    450,
                    tokenActual().getLinea(),
                    "Se esperaba una matriz literal '[...]' o una operación entre matrices."
                );
                panico();
        }
    }


    private void listaFilas() {
        if (panicoUsado) return;
        filaMatriz();
        if(tokenActual().getTipo() != TipoToken.COMA){
            if(panicoUsado) return;
            errores.agregarError(
                    401,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera que se declare una matriz con al menos dos filas, ya que si solo cuenta con una fila, entonces sería un vector."
                );
            panico();
        }
        match(TipoToken.COMA, "Se espera ',' para separar las filas de la matriz.");
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        
        filaMatriz();
        
        restoFilas();
    }

    private void restoFilas() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA, "Se espera ',' para separar filas.");
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

            filaMatriz();
            restoFilas();
        }
    }

    private void filaMatriz() {
        if (panicoUsado) return;
        match(TipoToken.CORCHETE_IZQ, "Se espera '[' para iniciar una fila de la matriz.");
        listaElementosFila();
        match(TipoToken.CORCHETE_DER, "Se espera ']' para cerrar la fila de la matriz.");
    }

    private void listaElementosFila() {
        if (panicoUsado) return;
        expresionNumerica();
        if(tokenActual().getTipo() != TipoToken.COMA){
            if(panicoUsado) return;
            errores.agregarError(
                    401,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera que se declare una matriz con al menos dos columnas, ya que si solo cuenta con una columna, entonces sería un vector."
                );
            panico();
        }
        match(TipoToken.COMA, "Se espera ',' para separar los elementos de la fila.");
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        
        expresionNumerica();
        restoElementosFila();
    }

    private void restoElementosFila() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            match(TipoToken.COMA, "Se espera ',' para separar los elementos de la fila.");
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

            expresionNumerica();
            restoElementosFila();
        }
    }
    
    
    /* expresionOperacionMatriz → terminoMatriz continuacionSumaRestaMatriz */
    private void expresionOperacionMatriz() {
        if (panicoUsado) return;

        terminoMatriz();
        continuacionSumaRestaMatriz();
    }
    
    /* terminoMatriz → IDENTIFICADOR continuacionMultDivMatriz */
    private void terminoMatriz() {
        if (panicoUsado) return;

        match(
            TipoToken.IDENTIFICADOR,
            "Se esperaba el identificador de una matriz."
        );

        continuacionMultDivMatriz();
    }
    /* continuacionSumaRestaMatriz → ('+'|'-') terminoMatriz continuacionSumaRestaMatriz | ε */
    private void continuacionSumaRestaMatriz() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {

            match(
                tokenActual().getTipo(),
                "Se esperaba un operador '+' o '-' para operar matrices."
            );

            match(
                TipoToken.IDENTIFICADOR,
                "La suma o resta de matrices solo se permite entre matrices."
            );

            continuacionSumaRestaMatriz();
        }
    }
    
    /* continuacionMultDivMatriz → ('*'|'/') expresionNumerica | ε */
    private void continuacionMultDivMatriz() {
        if (panicoUsado) return;

        if (tokenActual().getTipo() == TipoToken.OP_MULT ||
            tokenActual().getTipo() == TipoToken.OP_DIV) {

            match(
                tokenActual().getTipo(),
                "Se esperaba '*' o '/' para operar la matriz con un escalar."
            );

            expresionNumerica(); // SOLO escalar
        }
    }

//------------------------------------------------------------------------------------------------------
// ---------------------------------MODIFICAR MATRIZ----------------------------------------------------    
/*
prModificar() → IDENTIFICADOR accionMatriz() ';'

accionMatriz() → modificarCelda()
               | agregarFilaColumna()
               | eliminarFilaColumna()
               | reemplazarFilaColumna()

modificarCelda() → CELDA '(' expresionNumerica() ',' expresionNumerica() ')' '=' expresionNumerica()
        
agregarFilaColumna() → AGREGAR tipoFilaColumna expresionVector()

tipoFilaColumna → FILA
                 | COLUMNA

eliminarFilaColumna() → ELIMINAR tipoFilaColumna expresionNumerica()

reemplazarFilaColumna() → tipoFilaColumna expresionNumerica() '=' expresionVector()
*/   

    private void prModificar() {
        match(TipoToken.PR_MODIFICAR, 
            "Se espera la palabra reservada 'MODIFICAR'.");
        match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz a modificar.");
        accionMatriz();
    }

    /* accionMatriz → cambiarCelda | agregar | eliminar | reemplazar */
    private void accionMatriz() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {
            case PR_CELDA:
                cambiarCelda();
                break;

            case PR_AGREGAR:
                agregarFilaColumna();
                break;

            case PR_ELIMINAR:
                eliminarFilaColumna();
                break;

            case PR_FILA:
            case PR_COLUMNA:
                reemplazarFilaColumna();
                break;

            default:
                errores.agregarError(
                    500,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una acción válida sobre la matriz: "
                    + "CELDA(i,j), AGREGAR FILA/COLUMNA, ELIMINAR FILA/COLUMNA "
                    + "o reemplazo de FILA/COLUMNA."
                );
                panico();
        }
    }

    /* ===============================
       CAMBIAR UNA CELDA
       =============================== */
/* CELDA '(' fila ',' columna ')' '=' expresionNumerica ';' */
    private void cambiarCelda() {
        if (panicoUsado) return;

        match(TipoToken.PR_CELDA, 
            "Se espera la palabra reservada 'CELDA'.");
        match(TipoToken.PARENT_IZQ, 
            "Se espera '(' después de CELDA.");

        expresionNumerica();

        match(TipoToken.COMA, 
            "Se espera ',' para separar fila y columna.");
        validarDobleComa("La separación de la celda debe tener solo una coma ','.");

        expresionNumerica();

        match(TipoToken.PARENT_DER, 
            "Se espera ')' para cerrar la celda.");
        match(TipoToken.ASIGNACION, 
            "Se espera '=' para asignar el nuevo valor a la celda.");

        expresionNumerica();

        match(TipoToken.PUNTO_Y_COMA, 
            "Se espera ';' al final de la modificación de la celda.");
    }

    /* ===============================
       AGREGAR FILA / COLUMNA
       =============================== */
    /* AGREGAR (FILA | COLUMNA) expresionVector ';' */
    private void agregarFilaColumna() {
        if (panicoUsado) return;

        match(TipoToken.PR_AGREGAR, 
            "Se espera la palabra reservada 'AGREGAR'.");

        if (tokenActual().getTipo() != TipoToken.PR_FILA &&
            tokenActual().getTipo() != TipoToken.PR_COLUMNA) {

            errores.agregarError(
                501,
                tokenActual().getLinea(),
                "Después de AGREGAR se espera FILA o COLUMNA."
            );
            panico();
            return;
        }

        match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA.");

        expresionVector();

        match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final de la instrucción AGREGAR.");
    }

    /* ===============================
       ELIMINAR FILA / COLUMNA
       =============================== */
    /* ELIMINAR (FILA | COLUMNA) expresionNumerica ';' */
    private void eliminarFilaColumna() {
        if (panicoUsado) return;

        match(TipoToken.PR_ELIMINAR,
            "Se espera la palabra reservada 'ELIMINAR'.");

        if (tokenActual().getTipo() != TipoToken.PR_FILA &&
            tokenActual().getTipo() != TipoToken.PR_COLUMNA) {

            errores.agregarError(
                502,
                tokenActual().getLinea(),
                "Después de ELIMINAR se espera FILA o COLUMNA."
            );
            panico();
            return;
        }

        match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA.");

        expresionNumerica();

        match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final de la instrucción ELIMINAR.");
    }

    /* ===============================
       REEMPLAZAR FILA / COLUMNA
       =============================== */
    /* (FILA | COLUMNA) expresionNumerica '=' expresionVector ';' */
    private void reemplazarFilaColumna() {
        if (panicoUsado) return;

        match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA.");

        expresionNumerica();

        match(TipoToken.ASIGNACION,
            "Se espera '=' para asignar la nueva fila o columna.");

        expresionVector();

        match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final del reemplazo.");
    }
    
    
//------------------------------------------------------------------------------------------------------
// ---------------------------------OBTENER-------------------------------------------------------------    
/*
GRAMÁTICA UTILIZADA:
prObtener() → OBTENER operacionObtener() ';'

operacionObtener() → obtenerCelda()
                   | obtenerFila()
                   | obtenerColumna()

obtenerCelda() → CELDA '(' expresionNumerica() ',' expresionNumerica() ')' DE IDENTIFICADOR

obtenerFila() → FILA expresionNumerica() DE IDENTIFICADOR

obtenerColumna() → COLUMNA expresionNumerica() DE IDENTIFICADOR
*/

    private void prObtener() {
        match(TipoToken.PR_OBTENER, 
            "Se espera la palabra reservada 'OBTENER'.");
        definicionObtener();
    }

    private void definicionObtener() {
        if (panicoUsado) return;

        switch (tokenActual().getTipo()) {
            case PR_CELDA:
                obtenerCelda();
                match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER CELDA.");
                break;

            case PR_FILA:
                obtenerFila();
                match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER FILA.");
                break;

            case PR_COLUMNA:
                obtenerColumna();
                match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER COLUMNA.");
                break;

            default:
                errores.agregarError(
                    500,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba CELDA, FILA o COLUMNA después de 'OBTENER'."
                );
                panico();
        }
    }

    private void obtenerCelda() {
        if (panicoUsado) return;

        match(TipoToken.PR_CELDA, 
            "Se espera la palabra reservada 'CELDA'.");
        match(TipoToken.PARENT_IZQ, 
            "Se espera '(' después de CELDA.");

        expresionNumerica();

        match(TipoToken.COMA, 
            "Se espera ',' para separar fila y columna.");
        validarDobleComa(
            "La separación de la fila y columna debe estar dada por una sola coma ','."
        );

        expresionNumerica();

        match(TipoToken.PARENT_DER, 
            "Se espera ')' para cerrar la definición de la celda.");

        match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después de CELDA(fila,columna).");

        match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la celda.");

    }

    private void obtenerFila() {
        if (panicoUsado) return;

        match(TipoToken.PR_FILA, 
            "Se espera la palabra reservada 'FILA'.");

        expresionNumerica();

        match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después del número de fila.");

        match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la fila.");
    }

    private void obtenerColumna() {
        if (panicoUsado) return;

        match(TipoToken.PR_COLUMNA, 
            "Se espera la palabra reservada 'COLUMNA'.");

        expresionNumerica();

        match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después del número de columna.");

        match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la columna.");
    }  
//------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------- 
    
    
    
    
    
    
    
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////////

    private void validarDobleComa(String msg) {
        if (panicoUsado) return;
        if (tokenActual().getTipo() == TipoToken.COMA) {
            errores.agregarError(
                401,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                msg
            );
            panico();
        }
    }
    
    private void validacionFin(TipoToken tk1, TipoToken tk2, String msg1, String msg2) {
        if (panicoUsado) return;
        if (tokenActual().getTipo() == tk1) {
            errores.agregarError(
                210,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                msg1
            );
            panico();
            return;
        }

        if (tokenActual().getTipo() == tk2) {
            errores.agregarError(
                210,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                msg2
            );
            panico();
        }
    }
    
}

