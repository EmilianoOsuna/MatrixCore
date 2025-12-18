import java.util.List;
import java.util.Set;

public class ParserLL1 {
    Errores errores = new Errores();
    
    private List<Token> tokens;
    private int pos = 0, lineaFinal = -2;
    private boolean panicoUsado = false, lFinal = false;

    // VARIABLE PARA EL ÁRBOL
    private NodoArbol raizArbol;

    public ParserLL1(List<Token> tokens) {
        this.tokens = tokens;
    }

    // GETTER PARA LA UI
    public NodoArbol getRaiz() {
        return raizArbol;
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

    private NodoArbol match(TipoToken esperado, String msg) {
        if (panicoUsado) return null; 

        if (tokenActual().getTipo() == esperado) {
            NodoArbol hoja = new NodoArbol(tokenActual().getLexema());
            pos++;
            return hoja;
        } else {
                errores.agregarError(TablaErrores.ERROR_SINTACTICO, tokens.get(pos - 1).getLinea(),msg );
                panico();
                return null;
        }
    }
//-----------------------------------------------------------------------------------------------------------
// -------------------------------------INICIO DEL PARSER----------------------------------------------------
/*
GRAMÁTICA UTILIZADA:
inicio() → prCrear() | prModificar() | prObtener()  | prMostrar()  | condicional()
*/
    public void inicio() {
        raizArbol = new NodoArbol("INICIO");
        while (tokenActual().getTipo() != TipoToken.EOF) {
            switch(tokenActual().getTipo()){
            case PR_CREAR:
                raizArbol.agregarHijo(prCrear());               
                break;
            case PR_MODIFICAR:
                raizArbol.agregarHijo(prModificar());
                break;
            case PR_OBTENER:
                 raizArbol.agregarHijo(prObtener());
                 break;
            case PR_MOSTRAR:
                raizArbol.agregarHijo(prMostrar());
                break;
            case PR_SI:
                raizArbol.agregarHijo(condicional());
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_INICIAL,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera una acción inicial como CREAR, MODIFICAR, OBTENER en vez de: " + tokenActual().getLexema()
                );
                panico();
            }
            panicoUsado = false;
        }
        match(TipoToken.EOF,"Algo salió mal.");
    }

    // Esto es para evitar hacer el ciclo cuando hacemos condicionales:
    public NodoArbol inicioSinWhile() {
            NodoArbol nodo = null;
            switch(tokenActual().getTipo()){
            case PR_CREAR:
                nodo = prCrear();               
                break;
            case PR_MODIFICAR:
                nodo = prModificar();
                break;
            case PR_OBTENER:
                 nodo = prObtener();
                 break;
            case PR_MOSTRAR:
                nodo = prMostrar();
                break;
            case PR_SI:
                nodo = condicional();
                break;
            default:
                errores.agregarError(TablaErrores.ERROR_INICIAL,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera una acción inicial como CREAR, MODIFICAR, OBTENER en vez de: " + tokenActual().getLexema()
                );
                panico();
            }
            panicoUsado = false;
            return nodo;
        }

// CREACIÓN DE VARIABLES
/*
GRAMÁTICA UTILIZADA:
prCrear() → prCadena() | prNum() | prVector() | prMatriz() 
*/
    private NodoArbol prCrear() {

        if (tokenActual().getTipo() != TipoToken.PR_CREAR) {
            errores.agregarError(TablaErrores.ERROR_SINTACTICO,tokenActual().getLinea(),"Se esperaba 'CREAR'");
            panico();
            return null;
        }
        
        NodoArbol nodo = new NodoArbol("prCrear");
        nodo.agregarHijo(match(TipoToken.PR_CREAR,"Se esperaba 'CREAR'"));

        // Decidir qué tipo se va a crear
        switch (tokenActual().getTipo()) {

            case PR_CADENA:
                nodo.agregarHijo(prCadena());
                break;

            case PR_NUM:
                nodo.agregarHijo(prNum());
                break;

            case PR_VECTOR:
                nodo.agregarHijo(prVector());
                break;

            case PR_MATRIZ:
                nodo.agregarHijo(prMatriz());
                break;

            default:
                errores.agregarError(TablaErrores.ERROR_INICIAL,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera un tipo de variable a crear, por ejemplo: CADENA, NUM, VECTOR o MATRIZ"
                );
                panico();
        }
        return nodo;
    }


// -CREAR CADENA-
/*
GRAMÁTICA UTILIZADA:
prCadena() → IDENTIFICADOR = expresionCadena() ;
expresionCadena() → operandoCadena() restoCadena() ;
restoCadena() → + operandoCadena() restoCadena() | ε
operandoCadena() → CADENA
                | IDENTIFICADOR
                | '(' expresionNumerica() ')'
*/
    private NodoArbol prCadena() {
        NodoArbol nodo = new NodoArbol("prCadena");
        nodo.agregarHijo(match(TipoToken.PR_CADENA,"Se espera 'CADENA' después de 'CREAR'"));          
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR,"Se espera el nombre de la CADENA"));
        nodo.agregarHijo(match(TipoToken.ASIGNACION,"Se espera el operador de ASIGNACIÓN '=' después del identificador: '" + tokens.get(pos - 1).getLexema() + "'."));
        nodo.agregarHijo(expresionCadena());
        
        validacionFin(TipoToken.PARENT_IZQ,TipoToken.PARENT_DER,
        "Sobra un PARÉNTESIS de inicio '('."
        + " En caso de querer comenzar una operación numérica dentro de un paréntesis, es necesario agregar un opeardor de suma antes de abrir un paréntesis",
        "Sobra un PARÉNTESIS de cierre ')' ");
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA,"Se espera un ';' para dar fin a la instrucción"));
        return nodo;
    }
    private NodoArbol expresionCadena() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresionCadena");
        nodo.agregarHijo(operandoCadena());
        nodo.agregarHijo(restoCadena());
        return nodo;
    }    
    private NodoArbol operandoCadena() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("operandoCadena");

        switch (tokenActual().getTipo()) {

            case CADENA:
                nodo.agregarHijo(match(TipoToken.CADENA,
                    "Se esperaba una cadena literal."));
                break;

            case IDENTIFICADOR:
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR,
                    "Se esperaba un identificador válido."));
                break;

            case PARENT_IZQ:
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ,
                    "Se esperaba '(' para iniciar una expresión numérica."));

                nodo.agregarHijo(expresionNumerica());

                nodo.agregarHijo(match(TipoToken.PARENT_DER,
                    "Se esperaba ')' para cerrar la expresión numérica."));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_CADENA_OPERANDO,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una cadena, un identificador o una expresión numérica"
                    + " entre paréntesis después de '" + tokenAnterior().getLexema() + "'"
                );
                panico();
        }
        return nodo;
    }

    private NodoArbol restoCadena() {
        if (panicoUsado) return null;
        if (tokenActual().getTipo() == TipoToken.OP_SUMA) {
            NodoArbol nodo = new NodoArbol("restoCadena");
            nodo.agregarHijo(match(TipoToken.OP_SUMA,"Se espera un operador de suma (+)."));
            nodo.agregarHijo(operandoCadena());
            nodo.agregarHijo(restoCadena());
            return nodo;
        }
        return null;
    }
    
    
// ----CREAR NUM
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
    private NodoArbol prNum() {
        NodoArbol nodo = new NodoArbol("prNum");
        nodo.agregarHijo(match(TipoToken.PR_NUM,
            "Se espera la palabra reservada NUM para crear una variable numérica."));

        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR,
            "Se espera el identificador del número a crear."));

        nodo.agregarHijo(match(TipoToken.ASIGNACION,
            "Se espera el operador de asignación '=' después del identificador '"
            + tokenAnterior().getLexema() + "'."));

        nodo.agregarHijo(expresionNumerica()); 
        validacionFin(TipoToken.PARENT_IZQ,TipoToken.PARENT_DER,
        "Sobra un PARÉNTESIS de inicio '(' al final de la declaración de la EXPRESIÓN NUMÉRICA."
        + " En caso de querer comenzar otra operación es necesario agregar un opeardor aritmético antes de abrir un paréntesis",
        "Sobra un PARÉNTESIS de cierre ')' al final de la declaración de la EXPRESIÓN NUMÉRICA.");
        
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' para finalizar la declaración del número."));
        return nodo;
    }

    private NodoArbol expresionNumerica() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresionNumerica");

        nodo.agregarHijo(terminoNumerico());
        nodo.agregarHijo(continuacionSumaResta());
        return nodo;
    }

    private NodoArbol continuacionSumaResta() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {
            
            NodoArbol nodo = new NodoArbol("continuacionSumaResta");

            nodo.agregarHijo(match(tokenActual().getTipo(),
                "Se esperaba un operador aritmético válido '+' o '-'."));

            nodo.agregarHijo(terminoNumerico());
            nodo.agregarHijo(continuacionSumaResta());
            return nodo;
        }
        return null;
    }

    private NodoArbol terminoNumerico() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("terminoNumerico");

        nodo.agregarHijo(factorNumerico());
        nodo.agregarHijo(continuacionMultiplicacionDivision());
        return nodo;
    }

    private NodoArbol continuacionMultiplicacionDivision() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.OP_MULT ||
            tokenActual().getTipo() == TipoToken.OP_DIV) {
            
            NodoArbol nodo = new NodoArbol("continuacionMultiplicacionDivision");

            nodo.agregarHijo(match(tokenActual().getTipo(),
                "Se esperaba un operador aritmético '*' o '/'."));

            nodo.agregarHijo(factorNumerico());
            nodo.agregarHijo(continuacionMultiplicacionDivision());
            return nodo;
        }
        return null;
    }

    private NodoArbol factorNumerico() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("factorNumerico");

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {

            nodo.agregarHijo(match(tokenActual().getTipo(),
                "Se esperaba un signo '+' o '-' válido."));

            nodo.agregarHijo(operandoNumerico());
        } else {
            nodo.agregarHijo(operandoNumerico());
        }
        return nodo;
    }


    private NodoArbol operandoNumerico() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("operandoNumerico");

        switch (tokenActual().getTipo()) {

            case NUMERO_REAL:
                nodo.agregarHijo(match(TipoToken.NUMERO_REAL,
                    "Se esperaba un número real válido."));
                break;

            case IDENTIFICADOR:
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR,
                    "Se esperaba un identificador numérico válido."));
                break;

            case PARENT_IZQ:
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ,
                    "Se esperaba '(' para iniciar una expresión numérica."));
                nodo.agregarHijo(expresionNumerica());
                nodo.agregarHijo(match(TipoToken.PARENT_DER,
                    "Se esperaba ')' para cerrar la expresión numérica."));
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
                nodo.agregarHijo(funcionNumerica());
                break;
            case PR_OBTENER:
                nodo.agregarHijo(match(TipoToken.PR_OBTENER, 
                    "Se espera la palabra reservada 'OBTENER'."));
                nodo.agregarHijo(obtenerCelda());
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_TIPO_DATOS,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un número real, un identificador, "
                    + "una función numérica o una expresión entre paréntesi después de : '"
                    + tokenAnterior().getLexema() + "', pero se encontró: " + tokenActual().getLexema()
                );
                panico();
        }
        return nodo;
    }

    private NodoArbol funcionNumerica() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("funcionNumerica");

        switch (tokenActual().getTipo()) {

            case PR_RAIZ:
                nodo.agregarHijo(match(TipoToken.PR_RAIZ,""));
                nodo.agregarHijo(unArgumento("RAIZ(x)"));
                break;

            case PR_POTENCIA:
                nodo.agregarHijo(match(TipoToken.PR_POTENCIA,""));
                nodo.agregarHijo(dosArgumentos("POTENCIA(base,exponente)"));
                break;

            case PR_COS:
                nodo.agregarHijo(match(TipoToken.PR_COS,""));
                nodo.agregarHijo(unArgumento("COS(x)"));
                break;

            case PR_SEN:
                nodo.agregarHijo(match(TipoToken.PR_SEN,""));
                nodo.agregarHijo(unArgumento("SEN(x)"));
                break;

            case PR_TAN:
                nodo.agregarHijo(match(TipoToken.PR_TAN,""));
                nodo.agregarHijo(unArgumento("TAN(x)"));
                break;

            case PR_ARCSEN:
                nodo.agregarHijo(match(TipoToken.PR_ARCSEN,""));
                nodo.agregarHijo(unArgumento("ARCSEN(x)"));
                break;

            case PR_ARCCOS:
                nodo.agregarHijo(match(TipoToken.PR_ARCCOS,""));
                nodo.agregarHijo(unArgumento("ARCCOS(x)"));
                break;

            case PR_ARCTAN:
                nodo.agregarHijo(match(TipoToken.PR_ARCTAN,""));
                nodo.agregarHijo(unArgumento("ARCTAN(x)"));
                break;

            case PR_LN:
                nodo.agregarHijo(match(TipoToken.PR_LN,""));
                nodo.agregarHijo(unArgumento("LN(x)"));
                break;

            case PR_LOG:
                nodo.agregarHijo(match(TipoToken.PR_LOG,""));
                nodo.agregarHijo(dosArgumentos("LOG(base,argumento)"));
                break;

            case PR_LOG10:
                nodo.agregarHijo(match(TipoToken.PR_LOG10,""));
                nodo.agregarHijo(unArgumento("LOG10(x)"));
                break;

            case PR_PI:
                nodo.agregarHijo(match(TipoToken.PR_PI,
                    "PI es una constante y no recibe argumentos."));
                break;

            case PR_EXP:
                nodo.agregarHijo(match(TipoToken.PR_EXP,""));
                nodo.agregarHijo(unArgumento("EXP(exponente)"));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_FUNCION_INVALIDA,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una función numérica válida."
                );
                panico();
        }
        return nodo;
    }

    private NodoArbol unArgumento(String esperado) {
        NodoArbol nodo = new NodoArbol("unArgumento");
        nodo.agregarHijo(match(TipoToken.PARENT_IZQ,
            "Se esperaba el argumento de la función con la forma: " + esperado));

        nodo.agregarHijo(operandoSimple());
        
        nodo.agregarHijo(match(TipoToken.PARENT_DER,
            "Se esperaba el argumento de la función con la forma: " + esperado));
        return nodo;
    }

    private NodoArbol dosArgumentos(String esperado) {
        NodoArbol nodo = new NodoArbol("dosArgumentos");
        nodo.agregarHijo(match(TipoToken.PARENT_IZQ,
            "Se esperaban los argumentos de la función con la forma: " + esperado));

        nodo.agregarHijo(operandoSimple());

        nodo.agregarHijo(match(TipoToken.COMA,
            "Se esperaban dos argumentos separados por una coma ','. Ejemplo: " + esperado));
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        nodo.agregarHijo(operandoSimple());

        nodo.agregarHijo(match(TipoToken.PARENT_DER,
            "Se esperaban los argumentos de la función con la forma: " + esperado));
        return nodo;
    }

    private NodoArbol operandoSimple() {
        if (panicoUsado) return null;

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
                return expresionNumerica();

            default: 
                if(tokenAnterior().getLexema().equals("(") && tokenActual().getLexema().equals(")")){ 
                    errores.agregarError( TablaErrores.ERROR_ARGUMENTO_VACIO, lFinal ?  lineaFinal : tokenActual().getLinea(),
                            "El argumento de la función no debe estar vacío." );
                } 
                else{ errores.agregarError( TablaErrores.ERROR_ARGUMENTO_VACIO, lFinal ?  lineaFinal : tokenActual().getLinea(), 
                        "El argumento de la función debe ser un número real, un identificador o una expresión numérica válida.");
                        }
                panico();
                return null;
        }
    }


// CREAR VECTOR
/*
GRAMÁTICA UTILIZADA:
prVector() → IDENTIFICADOR = expresionVector() ;

expresionVector() → '[' listaElementosVector() ']' | OBTENER obtenerFila() | OBTENER obtenerColumna()

listaElementosVector() → expresionNumerica() ',' expresionNumerica() restoElementosVector()

restoElementosVector() → ',' expresionNumerica() restoElementosVector()
                        | ε

*/

    private NodoArbol prVector() {
        NodoArbol nodo = new NodoArbol("prVector");
        nodo.agregarHijo(match(TipoToken.PR_VECTOR, "Se espera 'VECTOR' después de 'CREAR'."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador del vector a crear."));
        nodo.agregarHijo(match(TipoToken.ASIGNACION,
                "Se espera el operador de ASIGNACIÓN '=' después del identificador: '" +
                tokenAnterior().getLexema() + "'."));
        nodo.agregarHijo(expresionVector());
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, "Se espera un ';' para dar fin a la declaración del vector."));
        return nodo;
    }

        private NodoArbol expresionVector() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresionVector");

        switch (tokenActual().getTipo()) {

            case CORCHETE_IZQ:
                nodo.agregarHijo(match(
                    TipoToken.CORCHETE_IZQ,
                    "Se espera '[' para iniciar la definición del vector."
                ));

                nodo.agregarHijo(listaElementosVector());

                nodo.agregarHijo(match(
                    TipoToken.CORCHETE_DER,
                    "Se espera ']' para cerrar la definición del vector."
                ));
                validacionFin(
                    TipoToken.CORCHETE_IZQ,
                    TipoToken.CORCHETE_DER,
                    "Sobra un corchete de inicio '[' en la definición del vector.",
                    "Sobra un corchete de cierre ']' en la definición del vector."
                );
                break;

            case PR_OBTENER:
                nodo.agregarHijo(match(
                    TipoToken.PR_OBTENER,
                    "Se espera la palabra reservada 'OBTENER'."
                ));

                switch (tokenActual().getTipo()) {

                    case PR_FILA:
                        nodo.agregarHijo(obtenerFila());
                        break;

                    case PR_COLUMNA:
                        nodo.agregarHijo(obtenerColumna());
                        break;

                    default:
                        errores.agregarError(
                            TablaErrores.ERROR_OBTENER_OPCION,
                            lFinal ? lineaFinal : tokenActual().getLinea(),
                            "Después de 'OBTENER' se espera FILA o COLUMNA."
                        );
                        panico();
                        return null;
                }
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_VECTOR_DEF,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una definición de vector: "
                  + "un vector literal [ ... ] o una obtención con OBTENER FILA/COLUMNA."
                );
                panico();
        }
        return nodo;
    }


    private NodoArbol listaElementosVector() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("listaElementosVector");

        // Primer elemento del vetor
        nodo.agregarHijo(expresionNumerica());
        if (panicoUsado) return null;

        // Validando que el vector sea de al menos dos elementos
        if (tokenActual().getTipo() != TipoToken.COMA) {
            errores.agregarError(
                TablaErrores.ERROR_MATRIZ_DIMENSION,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                "Un vector debe contener al menos dos elementos (n ≥ 2). Debe estar expresado de la siguiente forma: VECTOR[a,b]"
            );
            panico();
            return null;
        }

        nodo.agregarHijo(match(TipoToken.COMA,
                "Se espera ',' para separar los elementos del vector."));
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

        // Segundo elemento (obligatorio)
        nodo.agregarHijo(expresionNumerica());
        if (panicoUsado) return null;

        // Resto opcional
        nodo.agregarHijo(restoElementosVector());
        return nodo;
    }

    private NodoArbol restoElementosVector() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            NodoArbol nodo = new NodoArbol("restoElementosVector");
            nodo.agregarHijo(match(TipoToken.COMA,
                    "Se espera ',' para separar los elementos del vector."));
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
            nodo.agregarHijo(expresionNumerica());
            nodo.agregarHijo(restoElementosVector());
            return nodo;
        }
        return null;
    }

/*
Gramática para las operaciones especiales necesarias en la creación de matrices.
operacionEspecial()
    → PR_TRANSPUESTA '(' IDENTIFICADOR ')' 
    | PR_INVERSA     '(' IDENTIFICADOR ')'
    | PR_ADJUNTA     '(' IDENTIFICADOR ')'
    | PR_COFACTORES  '(' IDENTIFICADOR ')'
    | PR_GAUSS       '(' IDENTIFICADOR ')'
    | PR_GAUSSJ      '(' IDENTIFICADOR ')'
*/
    private NodoArbol operacionEspecial() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("operacionEspecial");

        switch (tokenActual().getTipo()) {
            case PR_TRANSPUESTA:
                nodo.agregarHijo(match(TipoToken.PR_TRANSPUESTA, "Se espera la palabra reservada 'TRANSPUESTA'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'TRANSPUESTA'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'TRANSPUESTA'."));
                break;

            case PR_INVERSA:
                nodo.agregarHijo(match(TipoToken.PR_INVERSA, "Se espera la palabra reservada 'INVERSA'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'INVERSA'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'INVERSA'."));
                break;

            case PR_ADJUNTA:
                nodo.agregarHijo(match(TipoToken.PR_ADJUNTA, "Se espera la palabra reservada 'ADJUNTA'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'ADJUNTA'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'ADJUNTA'."));
                break;

            case PR_COFACTORES:
                nodo.agregarHijo(match(TipoToken.PR_COFACTORES, "Se espera la palabra reservada 'COFACTORES'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'COFACTORES'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'COFACTORES'."));
                break;

            case PR_GAUSS:
                nodo.agregarHijo(match(TipoToken.PR_GAUSS, "Se espera la palabra reservada 'GAUSS'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'GAUSS'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'GAUSS'."));
                break;

            case PR_GAUSSJ:
                nodo.agregarHijo(match(TipoToken.PR_GAUSSJ, "Se espera la palabra reservada 'GAUSSJ'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'GAUSSJ'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz a operar."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la operación 'GAUSSJ'."));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_OPERACION_ESPECIAL,
                    tokenActual().getLinea(),
                    "Se esperaba una operación especial de matriz como TRANSPUESTA, INVERSA, ADJUNTA, COFACTORES, GAUSS o GAUSSJ."
                );
                panico();
                break;
        }
        return nodo;
    }


// CREAR MATRIZ   
/*

prMatriz()
    → MATRIZ definicionMatriz()

definicionMatriz()
    → matrizConTipo()
    | matrizNormal()

matrizConTipo()
    → tipoMatriz() tamañoMatriz() IDENTIFICADOR ';'

tipoMatriz()
    → IDENTIDAD
    | DIAGONAL
    | CEROS
    | UNOS


matrizNormal()
    → tamañoMatriz() IDENTIFICADOR finMatriz()

finMatriz()
    → '=' expresionMatriz() ';'

expresionMatriz()
    → terminoMatriz() continuacionSumaRestaMatriz()
    | '[' listaFilas() ']' | operacionEspecialMatriz()


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

tamañoMatriz()
    → TAMAÑO '(' expresionNumerica() ',' expresionNumerica() ')'

*/   

    private NodoArbol prMatriz() {
        NodoArbol nodo = new NodoArbol("prMatriz");
        nodo.agregarHijo(match(TipoToken.PR_MATRIZ, "Se espera la palabra reservada 'MATRIZ' después de 'CREAR'."));
        nodo.agregarHijo(definicionMatriz());
        return nodo;
    }

    /* definicionMatriz → matrizConTipo | matrizNormal */
    private NodoArbol definicionMatriz() {
        if (panicoUsado) return null;

        switch (tokenActual().getTipo()) {
            case PR_IDENTIDAD:
            case PR_DIAGONAL:
            case PR_CEROS:
            case PR_UNOS:
                return matrizConTipo();

            case PR_TAMAÑO:
                return matrizNormal();

            default:
                errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_DEF,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba un tipo de matriz (IDENTIDAD, DIAGONAL, CEROS, UNOS) "
                    + "o una definición normal con TAMAÑO(filas,columnas)."
                );
                panico();
                return null;
        }
    }

    /* matrizConTipo → tipoMatriz tamañoMatriz IDENTIFICADOR ';' */
    private NodoArbol matrizConTipo() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("matrizConTipo");
        nodo.agregarHijo(tipoMatriz());
        nodo.agregarHijo(tamañoMatriz());
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz."));
        if (tokenActual().getTipo() != TipoToken.PUNTO_Y_COMA){
            if (panicoUsado) return null;
            errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_DIMENSION,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "En una matriz definida por tipo y tamaño se espera un ';' después de el identificador para dar fin a su declaración."
                );
            panico();
            return null;
        }
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, "Se espera ';' al final de la declaración de la matriz."));
        return nodo;
    }

    /* tipoMatriz → IDENTIDAD | DIAGONAL | CEROS | UNOS */
    private NodoArbol tipoMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("tipoMatriz");

        switch (tokenActual().getTipo()) {
            case PR_IDENTIDAD:
            case PR_DIAGONAL:
            case PR_CEROS:
            case PR_UNOS:
                nodo.agregarHijo(match(tokenActual().getTipo(), "Se espera un tipo válido de matriz."));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_DIMENSION,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Tipo de matriz inválido. Se esperaba IDENTIDAD, DIAGONAL, CEROS o UNOS."
                );
                panico();
        }
        return nodo;
    }

    /* matrizNormal → tamañoMatriz IDENTIFICADOR finMatriz */
    private NodoArbol matrizNormal() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("matrizNormal");
        nodo.agregarHijo(tamañoMatriz());
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz."));
        nodo.agregarHijo(finMatriz());
        return nodo;
    }

    private NodoArbol finMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("finMatriz");
        nodo.agregarHijo(match(TipoToken.ASIGNACION, "Se espera '=' para asignar los valores de la matriz."));
        nodo.agregarHijo(expresionMatriz());
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, "Se espera ';' al final de la definición de la matriz."));
        return nodo;
    }

    private NodoArbol tamañoMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("tamañoMatriz");
        nodo.agregarHijo(match(TipoToken.PR_TAMAÑO, "Se espera la palabra reservada 'TAMAÑO', después de: '" + tokenAnterior().getLexema() + "'"));
        nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de TAMAÑO."));
        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar el tammaño de las filas y columnas."));
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la definición del tamaño."));
        return nodo;
    }

    /* expresionMatriz → '[' listaFilas ']' */
    private NodoArbol expresionMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresionMatriz");

        switch (tokenActual().getTipo()) {

            case CORCHETE_IZQ:
                // Matriz literal
                nodo.agregarHijo(match(TipoToken.CORCHETE_IZQ, "Se espera '[' para iniciar la matriz."));
                nodo.agregarHijo(listaFilas());
                nodo.agregarHijo(match(TipoToken.CORCHETE_DER, "Se espera ']' para cerrar la matriz."));
                validacionFin(TipoToken.CORCHETE_IZQ,TipoToken.CORCHETE_DER,
                "Sobra un CORCHETE de inicio '[' al final de la declaración de la MATRIZ.",
                "Sobra un CORCHETE de cierre ']' al final de la declaración de la MATRIZ.");
                break;

            case IDENTIFICADOR:
                nodo.agregarHijo(expresionOperacionMatriz());
                break;
            case PR_TRANSPUESTA:
            case PR_INVERSA:
            case PR_ADJUNTA:
            case PR_COFACTORES:
            case PR_GAUSS:
            case PR_GAUSSJ:
                nodo.agregarHijo(operacionEspecial());
                break;
            
            default:
                errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_OPERACION,
                    tokenActual().getLinea(),
                    "Se esperaba una matriz literal '[...]' o una operación entre matrices."
                );
                panico();
        }
        return nodo;
    }


    private NodoArbol listaFilas() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("listaFilas");
        nodo.agregarHijo(filaMatriz());
        if(tokenActual().getTipo() != TipoToken.COMA){
            if(panicoUsado) return null;
            errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_DIMENSION,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera que se declare una matriz con al menos dos filas, ya que si solo cuenta con una fila, entonces sería un vector."
                );
            panico();
        }
        nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar las filas de la matriz."));
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        
        nodo.agregarHijo(filaMatriz());
        
        nodo.agregarHijo(restoFilas());
        return nodo;
    }

    private NodoArbol restoFilas() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            NodoArbol nodo = new NodoArbol("restoFilas");
            nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar filas."));
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

            nodo.agregarHijo(filaMatriz());
            nodo.agregarHijo(restoFilas());
            return nodo;
        }
        return null;
    }

    private NodoArbol filaMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("filaMatriz");
        nodo.agregarHijo(match(TipoToken.CORCHETE_IZQ, "Se espera '[' para iniciar una fila de la matriz."));
        nodo.agregarHijo(listaElementosFila());
        nodo.agregarHijo(match(TipoToken.CORCHETE_DER, "Se espera ']' para cerrar la fila de la matriz."));
        return nodo;
    }

    private NodoArbol listaElementosFila() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("listaElementosFila");
        nodo.agregarHijo(expresionNumerica());
        if(tokenActual().getTipo() != TipoToken.COMA){
            if(panicoUsado) return null;
            errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_DIMENSION,
                    lFinal ?  lineaFinal : tokenActual().getLinea(),
                    "Se espera que se declare una matriz con al menos dos columnas, ya que si solo cuenta con una columna, entonces sería un vector."
                );
            panico();
        }
        nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar los elementos de la fila."));
        validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");
        
        nodo.agregarHijo(expresionNumerica());
        nodo.agregarHijo(restoElementosFila());
        return nodo;
    }

    private NodoArbol restoElementosFila() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.COMA) {
            NodoArbol nodo = new NodoArbol("restoElementosFila");
            nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar los elementos de la fila."));
            validarDobleComa("La separación de los elementos debe estar dada por solo una coma ','.");

            nodo.agregarHijo(expresionNumerica());
            nodo.agregarHijo(restoElementosFila());
            return nodo;
        }
        return null;
    }
    
    
    /* expresionOperacionMatriz → terminoMatriz continuacionSumaRestaMatriz */
    private NodoArbol expresionOperacionMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("expresionOperacionMatriz");

        nodo.agregarHijo(terminoMatriz());
        nodo.agregarHijo(continuacionSumaRestaMatriz());
        return nodo;
    }
    
    /* terminoMatriz → IDENTIFICADOR continuacionMultDivMatriz */
    private NodoArbol terminoMatriz() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("terminoMatriz");

        nodo.agregarHijo(match(
            TipoToken.IDENTIFICADOR,
            "Se esperaba el identificador de una matriz."
        ));

        nodo.agregarHijo(continuacionMultDivMatriz());
        return nodo;
    }
    /* continuacionSumaRestaMatriz → ('+'|'-') terminoMatriz continuacionSumaRestaMatriz | ε */
    private NodoArbol continuacionSumaRestaMatriz() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.OP_SUMA ||
            tokenActual().getTipo() == TipoToken.OP_RESTA) {
            
            NodoArbol nodo = new NodoArbol("continuacionSumaRestaMatriz");

            nodo.agregarHijo(match(
                tokenActual().getTipo(),
                "Se esperaba un operador '+' o '-' para operar matrices."
            ));

            nodo.agregarHijo(match(
                TipoToken.IDENTIFICADOR,
                "La suma o resta de matrices solo se permite entre matrices."
            ));

            nodo.agregarHijo(continuacionSumaRestaMatriz());
            return nodo;
        }
        return null;
    }
    
    /* continuacionMultDivMatriz → ('*'|'/') expresionNumerica | ε */
    private NodoArbol continuacionMultDivMatriz() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.OP_MULT ||
            tokenActual().getTipo() == TipoToken.OP_DIV) {
            
            NodoArbol nodo = new NodoArbol("continuacionMultDivMatriz");

            nodo.agregarHijo(match(
                tokenActual().getTipo(),
                "Se esperaba '*' o '/' para operar la matriz con un escalar."
            ));

            nodo.agregarHijo(expresionNumerica()); // SOLO escalar
            return nodo;
        }
        return null;
    }
    
// PROCEDIMIENTOS NUMÉRICOS CON MATRICES
/*
propiedadNumerica()
    → RANGO '(' IDENTIFICADOR ')'
    | DETERMINANTE '(' IDENTIFICADOR ')' 
    | DETERMINANTE '(' IDENTIFICADOR ')' METODO metodoDeterminante()

metodoDeterminante()
    → COFACTORES
    | GAUSS
*/
    private NodoArbol propiedadNumerica() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("propiedadNumerica");

        switch (tokenActual().getTipo()) {

            case PR_RANGO:
                nodo.agregarHijo(match(TipoToken.PR_RANGO, "Se esperaba la palabra reservada 'RANGO'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'RANGO'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz sobre la cual se calcula el rango."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la llamada a 'RANGO'."));
                break;

            case PR_DETERMINANTE:
                nodo.agregarHijo(match(TipoToken.PR_DETERMINANTE, "Se esperaba la palabra reservada 'DETERMINANTE'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'DETERMINANTE'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el identificador de la matriz sobre la cual se calcula el determinante."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar la llamada a 'DETERMINANTE'."));

                if (tokenActual().getTipo() == TipoToken.PR_METODO) {
                    nodo.agregarHijo(match(TipoToken.PR_METODO, "Se esperaba la palabra reservada 'METODO' para especificar el método de determinante."));
                    switch (tokenActual().getTipo()) {
                        case PR_COFACTORES:
                            nodo.agregarHijo(match(TipoToken.PR_COFACTORES, "Se esperaba el método 'COFACTORES' para calcular el determinante."));
                            break;
                        case PR_GAUSS:
                            nodo.agregarHijo(match(TipoToken.PR_GAUSS, "Se esperaba el método 'GAUSS' para calcular el determinante."));
                            break;
                        default:
                            errores.agregarError(
                                TablaErrores.ERROR_METODO_DETERMINANTE,
                                tokenActual().getLinea(),
                                "Método de determinante inválido. Se esperaba COFACTORES, GAUSS o SARRUS."
                            );
                            panico();
                            return null;
                    }
                }
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_PROPIEDAD_NUMERICA,
                    tokenActual().getLinea(),
                    "Se esperaba una propiedad numérica de matriz: RANGO o DETERMINANTE."
                );
                panico();
        }
        return nodo;
    }
// OPERACIONES CON VECTORES
/* operacionVector() → PR_PPUNTO '(' IDENTIFICADOR ',' IDENTIFICADOR ')' 
                    | PR_CRUZ   '(' IDENTIFICADOR ',' IDENTIFICADOR ')' 
                    | PR_NORMALIZAR '(' IDENTIFICADOR ')' 
                    | PR_MAGNITUD '(' IDENTIFICADOR ')' */
    private NodoArbol operacionVector() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("operacionVector");

        switch (tokenActual().getTipo()) {

            case PR_PPUNTO:
                nodo.agregarHijo(match(TipoToken.PR_PPUNTO, "Se esperaba la palabra reservada 'PPUNTO'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'PPUNTO'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el primer vector en PPUNTO."));
                nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar los vectores en PPUNTO."));
                validarDobleComa("La separación de los vectores debe darse con solo una coma ','.");
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el segundo vector en PPUNTO."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar PPUNTO."));
                break;

            case PR_CRUZ:
                nodo.agregarHijo(match(TipoToken.PR_CRUZ, "Se esperaba la palabra reservada 'CRUZ'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'CRUZ'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el primer vector en CRUZ."));
                nodo.agregarHijo(match(TipoToken.COMA, "Se espera ',' para separar los vectores en CRUZ."));
                validarDobleComa("La separación de los vectores debe darse con solo una coma ','.");
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera el segundo vector en CRUZ."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar CRUZ."));
                break;

            case PR_NORMALIZAR:
                nodo.agregarHijo(match(TipoToken.PR_NORMALIZAR, "Se esperaba la palabra reservada 'NORMALIZAR'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'NORMALIZAR'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un vector válido para NORMALIZAR."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar NORMALIZAR."));
                break;

            case PR_MAGNITUD:
                nodo.agregarHijo(match(TipoToken.PR_MAGNITUD, "Se esperaba la palabra reservada 'MAGNITUD'."));
                nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se espera '(' después de 'MAGNITUD'."));
                nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, "Se espera un vector válido para MAGNITUD."));
                nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se espera ')' para cerrar MAGNITUD."));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_ACCION_INVALIDA,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una operación válida de vector (PPUNTO, CRUZ, NORMALIZAR, MAGNITUD), pero se encontró: '" 
                    + tokenActual().getLexema() + "'."
                );
                panico();
        }
        return nodo;
    }
// MÉTODO MOSTRAR
/*
GRAMÁTICA UTILIZADA:
prMostrar() → expresionCadena() | accion() ';'
    accion() → 
            PROCEDIMIENTO  propiedadNumerica()
            | PROCEDIMIENTO operacionEspecial()
            | PROCEDIMIENTO operacionVector()
            | propiedadNumerica()
            | operacionEspecial()
            | operacionVector()
*/
    private NodoArbol prMostrar() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("prMostrar");

        nodo.agregarHijo(match(TipoToken.PR_MOSTRAR, "Se espera la palabra reservada 'MOSTRAR' para iniciar una instrucción de visualización."));

        switch (tokenActual().getTipo()) {
            case CADENA:
            case IDENTIFICADOR:
            case PARENT_IZQ:
                nodo.agregarHijo(expresionCadena());
                break;

            case PR_PROCEDIMIENTO:
            case PR_RANGO:
            case PR_DETERMINANTE:
            case PR_TRANSPUESTA:
            case PR_INVERSA:
            case PR_ADJUNTA:
            case PR_COFACTORES:
            case PR_GAUSS:
            case PR_GAUSSJ:
            case PR_PPUNTO:
            case PR_CRUZ:
            case PR_NORMALIZAR:
            case PR_MAGNITUD:
                nodo.agregarHijo(accion());
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_MATRIZ_OPERACION,
                    tokenActual().getLinea(),
                    "Se esperaba una cadena literal, un identificador, una expresión entre paréntesis, o una acción válida (propiedad numérica, operación especial o operación vectorial)."
                );
                panico();
                return null;
        }
        
        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, "Se espera ';' al final de la instrucción MOSTRAR."));
        return nodo;
    }


    private NodoArbol accion() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("accion");

        boolean procedimiento = false;

        if (tokenActual().getTipo() == TipoToken.PR_PROCEDIMIENTO) {
            procedimiento = true;
            nodo.agregarHijo(match(TipoToken.PR_PROCEDIMIENTO, "Se esperaba la palabra reservada 'PROCEDIMIENTO' antes de la acción."));
        }

        switch (tokenActual().getTipo()) {
            case PR_RANGO:
            case PR_DETERMINANTE:
                nodo.agregarHijo(propiedadNumerica());
                break;

            case PR_TRANSPUESTA:
            case PR_INVERSA:
            case PR_ADJUNTA:
            case PR_COFACTORES:
            case PR_GAUSS:
            case PR_GAUSSJ:
                nodo.agregarHijo(operacionEspecial());
                break;

            case PR_PPUNTO:
            case PR_CRUZ:
            case PR_NORMALIZAR:
            case PR_MAGNITUD:
                nodo.agregarHijo(operacionVector());
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_ACCION_INVALIDA,
                    tokenActual().getLinea(),
                    "Se esperaba una propiedad numérica (RANGO, DETERMINANTE), una operación especial de matriz (TRANSPUESTA, INVERSA, ADJUNTA, COFACTORES, GAUSS, GAUSSJ), o "
                            + "una operación vectorial después" 
                    + (procedimiento ? " de 'PROCEDIMIENTO'." : "de MOSTRAR. Otra opción también sería ingresar una cadena después de MOSTRAR.")
                );
                panico();
                return null;
        }
        return nodo;
    }

    
    
// MODIFICAR MATRIZ  
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

    private NodoArbol prModificar() {
        NodoArbol nodo = new NodoArbol("prModificar");
        nodo.agregarHijo(match(TipoToken.PR_MODIFICAR, 
            "Se espera la palabra reservada 'MODIFICAR'."));
        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz a modificar."));
        nodo.agregarHijo(accionMatriz());
        return nodo;
    }

    /* accionMatriz → cambiarCelda | agregar | eliminar | reemplazar */
    private NodoArbol accionMatriz() {
        if (panicoUsado) return null;

        switch (tokenActual().getTipo()) {
            case PR_CELDA:
                return cambiarCelda();

            case PR_AGREGAR:
                return agregarFilaColumna();

            case PR_ELIMINAR:
                return eliminarFilaColumna();

            case PR_FILA:
            case PR_COLUMNA:
                return reemplazarFilaColumna();

            default:
                errores.agregarError(
                    TablaErrores.ERROR_ACCION_INVALIDA,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba una acción válida sobre la matriz: "
                    + "CELDA(i,j), AGREGAR FILA/COLUMNA, ELIMINAR FILA/COLUMNA "
                    + "o reemplazo de FILA/COLUMNA."
                );
                panico();
                return null;
        }
    }

//CAMBIAR UNA CELDA
//CELDA '(' fila ',' columna ')' '=' expresionNumerica ';' 
    private NodoArbol cambiarCelda() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("cambiarCelda");

        nodo.agregarHijo(match(TipoToken.PR_CELDA, 
            "Se espera la palabra reservada 'CELDA'."));
        nodo.agregarHijo(match(TipoToken.PARENT_IZQ, 
            "Se espera '(' después de CELDA."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.COMA, 
            "Se espera ',' para separar fila y columna."));
        validarDobleComa("La separación de la celda debe tener solo una coma ','.");

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PARENT_DER, 
            "Se espera ')' para cerrar la celda."));
        nodo.agregarHijo(match(TipoToken.ASIGNACION, 
            "Se espera '=' para asignar el nuevo valor a la celda."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, 
            "Se espera ';' al final de la modificación de la celda."));
        return nodo;
    }


//AGREGAR FILA / COLUMNA
//AGREGAR (FILA | COLUMNA) expresionVector ';' */
    private NodoArbol agregarFilaColumna() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("agregarFilaColumna");

        nodo.agregarHijo(match(TipoToken.PR_AGREGAR, 
            "Se espera la palabra reservada 'AGREGAR'."));

        if (tokenActual().getTipo() != TipoToken.PR_FILA &&
            tokenActual().getTipo() != TipoToken.PR_COLUMNA) {

            errores.agregarError(
                TablaErrores.ERROR_AGREGAR_INVALIDO,
                tokenActual().getLinea(),
                "Después de AGREGAR se espera FILA o COLUMNA."
            );
            panico();
            return null;
        }

        nodo.agregarHijo(match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA."));

        nodo.agregarHijo(expresionVector());

        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final de la instrucción AGREGAR."));
        return nodo;
    }

//ELIMINAR FILA / COLUMNA
//ELIMINAR (FILA | COLUMNA) expresionNumerica ';' 
    private NodoArbol eliminarFilaColumna() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("eliminarFilaColumna");

        nodo.agregarHijo(match(TipoToken.PR_ELIMINAR,
            "Se espera la palabra reservada 'ELIMINAR'."));

        if (tokenActual().getTipo() != TipoToken.PR_FILA &&
            tokenActual().getTipo() != TipoToken.PR_COLUMNA) {

            errores.agregarError(
                TablaErrores.ERROR_ELIMINAR_INVALIDO,
                tokenActual().getLinea(),
                "Después de ELIMINAR se espera FILA o COLUMNA."
            );
            panico();
            return null;
        }

        nodo.agregarHijo(match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final de la instrucción ELIMINAR."));
        return nodo;
    }

//REEMPLAZAR FILA / COLUMNA
//(FILA | COLUMNA) expresionNumerica '=' expresionVector ';' 
    private NodoArbol reemplazarFilaColumna() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("reemplazarFilaColumna");

        nodo.agregarHijo(match(tokenActual().getTipo(),
            "Se espera FILA o COLUMNA."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.ASIGNACION,
            "Se espera '=' para asignar la nueva fila o columna."));

        nodo.agregarHijo(expresionVector());

        nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA,
            "Se espera ';' al final del reemplazo."));
        return nodo;
    }
    
    
// OBTENER    
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

    private NodoArbol prObtener() {
        NodoArbol nodo = new NodoArbol("prObtener");
        nodo.agregarHijo(match(TipoToken.PR_OBTENER, 
            "Se espera la palabra reservada 'OBTENER'."));
        nodo.agregarHijo(definicionObtener());
        return nodo;
    }

    private NodoArbol definicionObtener() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("definicionObtener");

        switch (tokenActual().getTipo()) {
            case PR_CELDA:
                nodo.agregarHijo(obtenerCelda());
                nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER CELDA."));
                break;

            case PR_FILA:
                nodo.agregarHijo(obtenerFila());
                nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER FILA."));
                break;

            case PR_COLUMNA:
                nodo.agregarHijo(obtenerColumna());
                nodo.agregarHijo(match(TipoToken.PUNTO_Y_COMA, 
                "Se espera ';' al final de la instrucción OBTENER COLUMNA."));
                break;

            default:
                errores.agregarError(
                    TablaErrores.ERROR_ACCION_INVALIDA,
                    lFinal ? lineaFinal : tokenActual().getLinea(),
                    "Se esperaba CELDA, FILA o COLUMNA después de 'OBTENER'."
                );
                panico();
        }
        return nodo;
    }

    private NodoArbol obtenerCelda() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("obtenerCelda");

        nodo.agregarHijo(match(TipoToken.PR_CELDA, 
            "Se espera la palabra reservada 'CELDA'."));
        nodo.agregarHijo(match(TipoToken.PARENT_IZQ, 
            "Se espera '(' después de CELDA."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.COMA, 
            "Se espera ',' para separar fila y columna."));
        validarDobleComa(
            "La separación de la fila y columna debe estar dada por una sola coma ','."
        );

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PARENT_DER, 
            "Se espera ')' para cerrar la definición de la celda."));

        nodo.agregarHijo(match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después de CELDA(fila,columna)."));

        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la celda."));
        return nodo;
    }

    private NodoArbol obtenerFila() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("obtenerFila");

        nodo.agregarHijo(match(TipoToken.PR_FILA, 
            "Se espera la palabra reservada 'FILA'."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después del número de fila."));

        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la fila."));
        return nodo;
    }

    private NodoArbol obtenerColumna() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("obtenerColumna");

        nodo.agregarHijo(match(TipoToken.PR_COLUMNA, 
            "Se espera la palabra reservada 'COLUMNA'."));

        nodo.agregarHijo(expresionNumerica());

        nodo.agregarHijo(match(TipoToken.PR_DE, 
            "Se espera la palabra reservada 'DE' después del número de columna."));

        nodo.agregarHijo(match(TipoToken.IDENTIFICADOR, 
            "Se espera el identificador de la matriz de la cual se desea obtener la columna."));
        return nodo;
    }  

//CONDICIONALES
/* condicional() → SI '(' condicion() ')' ENTONCES inicio() bloqueSino() FIN_SI

bloqueSino() → SINO inicio() | ε

condicion() → expresionComparativa()

expresionComparativa() → operandoNumerico() operadorComparacion() operandoNumerico()
                       | operandoCadena() operadorComparacion() operandoCadena()

operadorComparacion() → '==' | '!=' | '>' | '<' | '>=' | '<='
    
*/   
    private NodoArbol condicional() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("condicional");

        nodo.agregarHijo(match(TipoToken.PR_SI, "Se esperaba la palabra reservada 'SI' para iniciar un condicional."));

        nodo.agregarHijo(match(TipoToken.PARENT_IZQ, "Se esperaba '(' después de 'SI' para abrir la condición."));
        nodo.agregarHijo(condicion());
        
        nodo.agregarHijo(match(TipoToken.PARENT_DER, "Se esperaba ')' para cerrar la condición."));

        nodo.agregarHijo(match(TipoToken.PR_ENTONCES, "Se esperaba 'ENTONCES' después de la condición."));
        
        // Analiza el bloque verdadero
        nodo.agregarHijo(inicioSinWhile());
        // Esto para permitir condicionales anidados
        while(tokenActual().getTipo() == TipoToken.PR_CREAR ||
              tokenActual().getTipo() == TipoToken.PR_MODIFICAR ||
              tokenActual().getTipo() == TipoToken.PR_MOSTRAR ||
              tokenActual().getTipo() == TipoToken.PR_OBTENER ||
              tokenActual().getTipo() == TipoToken.PR_SI ){
              nodo.agregarHijo(inicioSinWhile());
        }
        nodo.agregarHijo(bloqueSino());
        while(tokenActual().getTipo() == TipoToken.PR_CREAR ||
              tokenActual().getTipo() == TipoToken.PR_MODIFICAR ||
              tokenActual().getTipo() == TipoToken.PR_MOSTRAR ||
              tokenActual().getTipo() == TipoToken.PR_OBTENER ||
              tokenActual().getTipo() == TipoToken.PR_SI ){
              nodo.agregarHijo(inicioSinWhile());
        }
        nodo.agregarHijo(match(TipoToken.PR_FIN_SI, "Se esperaba 'FIN_SI' para cerrar el condicional."));
        return nodo;
    }

    private NodoArbol bloqueSino() {
        if (panicoUsado) return null;

        if (tokenActual().getTipo() == TipoToken.PR_SINO) {
            NodoArbol nodo = new NodoArbol("bloqueSino");
            nodo.agregarHijo(match(TipoToken.PR_SINO, "Se esperaba la palabra reservada 'SINO'.")); 
            nodo.agregarHijo(inicioSinWhile());
            return nodo;
        }
        return null;
    }

    private NodoArbol condicion() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("condicion");

        switch (tokenActual().getTipo()) {
            case NUMERO_REAL:
            case IDENTIFICADOR:
            case PARENT_IZQ:
                nodo.agregarHijo(operandoNumerico());
                break;
            case CADENA:
                nodo.agregarHijo(operandoCadena());
                break;
            default:
                errores.agregarError(
                    TablaErrores.ERROR_ACCION_INVALIDA,
                    tokenActual().getLinea(),
                    "Se esperaba un operando numérico o cadena para la condición, pero se encontró: '" + tokenActual().getLexema() + "'."
                );
                panico();
                return null;
        }

        nodo.agregarHijo(operadorComparacion());

        switch (tokenActual().getTipo()) { 
            case NUMERO_REAL:
            case IDENTIFICADOR:
            case PARENT_IZQ:
                
                nodo.agregarHijo(operandoNumerico());
                
                break;
            case CADENA:
                nodo.agregarHijo(operandoCadena());
                break;
            default:
                errores.agregarError(
                    TablaErrores.ERROR_AGREGAR_INVALIDO,
                    tokenActual().getLinea(),
                    "Se esperaba un segundo operando numérico o cadena para la condición, pero se encontró: '" + tokenActual().getLexema() + "'."
                );
                panico();
        }
        return nodo;
    }

    private NodoArbol operadorComparacion() {
        if (panicoUsado) return null;
        NodoArbol nodo = new NodoArbol("operadorComparacion");

        switch (tokenActual().getTipo()) {
            case OP_IGUAL:      // '=='
            case OP_DIFERENTE:   // '!='
            case OP_MAYOR:      // '>'
            case OP_MENOR:      // '<'
            case OP_MAYOR_IGUAL:// '>='
            case OP_MENOR_IGUAL:// '<='
                nodo.agregarHijo(match(tokenActual().getTipo(), "Se esperaba un operador de comparación válido (==, !=, >, <, >=, <=)."));
                break;
            default:
                errores.agregarError(
                    TablaErrores.ERROR_ELIMINAR_INVALIDO,
                    tokenActual().getLinea(),
                    "Se esperaba un operador de comparación (==, !=, >, <, >=, <=) en la condición, pero se encontró: '" + tokenActual().getLexema() + "'."
                );
                panico();
        }
        return nodo;
    }

    /////////////////////////////////////////////////////////////////////////

    private void validarDobleComa(String msg) {
        if (panicoUsado) return;
        if (tokenActual().getTipo() == TipoToken.COMA) {
            errores.agregarError(
                TablaErrores.ERROR_MATRIZ_DIMENSION,
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
                TablaErrores.ERROR_TIPO_DATOS,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                msg1
            );
            panico();
            return;
        }

        if (tokenActual().getTipo() == tk2) {
            errores.agregarError(
                TablaErrores.ERROR_TIPO_DATOS,
                lFinal ?  lineaFinal : tokenActual().getLinea(),
                msg2
            );
            panico();
        }
    }
    
}