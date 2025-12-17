import java.util.HashMap;
import java.util.Map;

public class TablaErrores {

    // --- CÓDIGOS DE ERROR (CONSTANTES) ---
    
    // Errores Generales / Léxicos
    public static final int ERROR_INICIAL = 1;
    public static final int ERROR_IDENTIFICADOR_CON_NOMBRE_RESERVADO = 2;
    
    // Errores Léxicos (Provenientes del AFD)
    public static final int ERROR_LEXICO_SIMBOLO_INVALIDO = 100; // Usado en AFD.java
    
    // Errores Sintácticos
    public static final int ERROR_SINTACTICO = 101; // Usado en AFD (palabra desconocida) y Parser (general)
    public static final int ERROR_CADENA_OPERANDO = 103;
    
    // Errores Semánticos / Funciones
    public static final int ERROR_ARGUMENTO_VACIO = 203;
    public static final int ERROR_TIPO_DATOS = 210;
    public static final int ERROR_FUNCION_INVALIDA = 211;
    
    // Errores de Vectores
    public static final int ERROR_VECTOR_DEF = 309;
    public static final int ERROR_OBTENER_OPCION = 310;
    
    // Errores de Matrices
    public static final int ERROR_MATRIZ_DEF = 400;
    public static final int ERROR_MATRIZ_DIMENSION = 401; 
    public static final int ERROR_OPERACION_ESPECIAL = 420; // Transpuesta, Inversa, Gauss...
    public static final int ERROR_MATRIZ_OPERACION = 450;
    public static final int ERROR_METODO_DETERMINANTE = 460;
    public static final int ERROR_PROPIEDAD_NUMERICA = 461; // Rango, Determinante
    
    // Errores de Modificación y Lógica
    public static final int ERROR_ACCION_INVALIDA = 500; // Usado para acciones de matriz, vector y condiciones
    public static final int ERROR_AGREGAR_INVALIDO = 501;
    public static final int ERROR_ELIMINAR_INVALIDO = 502; // Usado también para operadores de comparación

    // --- MAPA DE DESCRIPCIONES ---
    private static final Map<Integer, String> mapaErrores = new HashMap<>();

    static {
        // 1. Errores de Inicio e Identificadores
        mapaErrores.put(ERROR_INICIAL, "Error de inicio de instrucción. Se esperaba una acción válida (CREAR, MODIFICAR, ETC).");
        mapaErrores.put(ERROR_IDENTIFICADOR_CON_NOMBRE_RESERVADO, "El identificador ingresado es una palabra reservada y no puede usarse como nombre.");

        // 2. Léxicos y Sintácticos
        mapaErrores.put(ERROR_LEXICO_SIMBOLO_INVALIDO, "Error Léxico: Símbolo no definido en el alfabeto.");
        mapaErrores.put(ERROR_SINTACTICO, "Error sintáctico o palabra desconocida."); 
        mapaErrores.put(ERROR_CADENA_OPERANDO, "Error en expresión de cadena: Se esperaba cadena literal, identificador u operación válida.");
        
        // 3. Semánticos / Numéricos
        mapaErrores.put(ERROR_ARGUMENTO_VACIO, "Argumento inválido o vacío en la función.");
        mapaErrores.put(ERROR_TIPO_DATOS, "Error de tipos: Se encontró un dato que no coincide con el esperado (ej. número vs cadena).");
        mapaErrores.put(ERROR_FUNCION_INVALIDA, "Función numérica no reconocida o mal escrita.");
        
        // 4. Vectores y Matrices
        mapaErrores.put(ERROR_VECTOR_DEF, "Definición de vector incorrecta. Use [a,b] u OBTENER.");
        mapaErrores.put(ERROR_OBTENER_OPCION, "Opción inválida para OBTENER. Se esperaba FILA, COLUMNA o CELDA.");
        mapaErrores.put(ERROR_MATRIZ_DEF, "Definición de matriz incorrecta. Verifique el TIPO o el TAMAÑO.");
        mapaErrores.put(ERROR_MATRIZ_DIMENSION, "Error de dimensión o estructura: Filas/Columnas no coinciden o sintaxis incorrecta.");
        mapaErrores.put(ERROR_OPERACION_ESPECIAL, "Operación especial de matriz inválida (Transpuesta, Inversa, etc).");
        mapaErrores.put(ERROR_MATRIZ_OPERACION, "Operación de matrices inválida. Verifique que los operandos sean matrices.");
        mapaErrores.put(ERROR_METODO_DETERMINANTE, "Método de determinante inválido. Use COFACTORES o GAUSS.");
        mapaErrores.put(ERROR_PROPIEDAD_NUMERICA, "Propiedad numérica no reconocida (Se esperaba RANGO o DETERMINANTE).");
        
        // 5. Modificación y Lógica
        mapaErrores.put(ERROR_ACCION_INVALIDA, "Acción u operación no válida en este contexto.");
        mapaErrores.put(ERROR_AGREGAR_INVALIDO, "Uso incorrecto de AGREGAR. Se esperaba FILA o COLUMNA.");
        mapaErrores.put(ERROR_ELIMINAR_INVALIDO, "Uso incorrecto de ELIMINAR o del Operador de comparación.");
    }

    public static String getMensaje(int codigo) {
        return mapaErrores.getOrDefault(codigo, "Error desconocido (" + codigo + ")");
    }
}