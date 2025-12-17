import java.util.HashMap;
import java.util.Map;

public class TablaErrores {

    // --- CÓDIGOS DE ERROR (CONSTANTES) ---
    public static final int ERROR_INICIAL = 1;
    public static final int ERROR_SINTACTICO = 101;
    public static final int ERROR_CADENA_OPERANDO = 103;
    
    public static final int ERROR_ARGUMENTO_VACIO = 203;
    public static final int ERROR_TIPO_DATOS = 210;
    public static final int ERROR_FUNCION_INVALIDA = 211;
    
    public static final int ERROR_VECTOR_DEF = 309;
    public static final int ERROR_OBTENER_OPCION = 310;
    
    public static final int ERROR_MATRIZ_DEF = 400;
    public static final int ERROR_MATRIZ_DIMENSION = 401; 
    public static final int ERROR_OPERACION_ESPECIAL = 420; // Transpuesta, Inversa...
    public static final int ERROR_MATRIZ_OPERACION = 450;
    public static final int ERROR_METODO_DETERMINANTE = 460;
    public static final int ERROR_PROPIEDAD_NUMERICA = 461; // Rango, Determinante
    
    public static final int ERROR_ACCION_INVALIDA = 500; // Usado para acciones de matriz, vector y condiciones
    public static final int ERROR_AGREGAR_INVALIDO = 501;
    public static final int ERROR_ELIMINAR_INVALIDO = 502; // Usado también para operadores de comparación

    // --- MAPA DE DESCRIPCIONES ---
    private static final Map<Integer, String> mapaErrores = new HashMap<>();

    static {
        // Códigos genéricos
        mapaErrores.put(ERROR_INICIAL, "Error de inicio de instrucción.");
        mapaErrores.put(ERROR_SINTACTICO, "Error sintáctico."); 
        mapaErrores.put(ERROR_CADENA_OPERANDO, "Error en expresión de cadena.");
        
        // Semánticos / Numéricos
        mapaErrores.put(ERROR_ARGUMENTO_VACIO, "Argumento inválido.");
        mapaErrores.put(ERROR_TIPO_DATOS, "Error de tipos.");
        mapaErrores.put(ERROR_FUNCION_INVALIDA, "Función numérica no reconocida.");
        
        // Vectores y Matrices
        mapaErrores.put(ERROR_VECTOR_DEF, "Definición de vector incorrecta.");
        mapaErrores.put(ERROR_OBTENER_OPCION, "Opción inválida para OBTENER.");
        mapaErrores.put(ERROR_MATRIZ_DEF, "Definición de matriz incorrecta.");
        mapaErrores.put(ERROR_MATRIZ_DIMENSION, "Error de dimensión o estructura.");
        mapaErrores.put(ERROR_OPERACION_ESPECIAL, "Operación especial de matriz inválida.");
        mapaErrores.put(ERROR_MATRIZ_OPERACION, "Operación de matrices inválida.");
        mapaErrores.put(ERROR_METODO_DETERMINANTE, "Método de determinante inválido.");
        mapaErrores.put(ERROR_PROPIEDAD_NUMERICA, "Propiedad numérica no reconocida.");
        
        // Modificación y Lógica
        mapaErrores.put(ERROR_ACCION_INVALIDA, "Acción u operación no válida.");
        mapaErrores.put(ERROR_AGREGAR_INVALIDO, "Uso incorrecto de AGREGAR/Operando.");
        mapaErrores.put(ERROR_ELIMINAR_INVALIDO, "Uso incorrecto de ELIMINAR/Operador.");
    }

    public static String getMensaje(int codigo) {
        return mapaErrores.getOrDefault(codigo, "Error desconocido");
    }
}