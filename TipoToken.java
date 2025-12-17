public enum TipoToken {

    // Operadores de comparación
    OP_IGUAL,          // ==
    OP_DIFERENTE,      // !=
    OP_MENOR,          // <
    OP_MAYOR,          // >
    OP_MENOR_IGUAL,    // <=
    OP_MAYOR_IGUAL,    // >=

    // Operadores aritméticos
    OP_SUMA,           // +
    OP_RESTA,          // -
    OP_MULT, // *
    OP_DIV,       // /

    // Delimitadores
    PARENT_IZQ,        // (
    PARENT_DER,        // )
    CORCHETE_IZQ,      // [
    CORCHETE_DER,      // ]
    COMA,              // ,
    PUNTO_Y_COMA,      // ;
    GUION_BAJO,        // _
    ASIGNACION,        // =

    // Literales
    CADENA,
    NUMERO_REAL,

    // Identificadores
    IDENTIFICADOR,

    // Otros
    DESCONOCIDO,
    EOF,
    
    //PALABRAS RESERVADAS
    PR_ADJUNTA,
    PR_AGREGAR,
    PR_ARCCOS,
    PR_ARCSEN,
    PR_ARCTAN,

    PR_CADENA,
    PR_CELDA,
    PR_CEROS,
    PR_COFACTORES,
    PR_COLUMNA,
    PR_COS,
    PR_CREAR,
    PR_CRUZ,
    
    PR_DE,
    PR_DETERMINANTE,
    PR_DIAGONAL,

    PR_ELIMINAR,
    PR_ENTONCES,
    PR_EXP,
    PR_EMPIEZA_WHILE,

    PR_FILA,
    PR_FIN_SI,
    PR_FIN_WHILE,

    PR_GAUSS,
    PR_GAUSSJ,

    PR_IDENTIDAD,
    PR_INVERSA,

    PR_LN,
    PR_LOG,
    PR_LOG10,

    PR_MAGNITUD,
    PR_MATRIZ,
    PR_MODIFICAR,
    PR_MOSTRAR,
    PR_METODO,

    PR_NORMALIZAR,
    PR_NUM,

    PR_OBTENER,

    PR_PI,
    PR_POTENCIA,
    PR_PPUNTO,
    PR_PROCEDIMIENTO,

    PR_RAIZ,
    PR_RANGO,

    PR_SEN,
    PR_SI,
    PR_SINO,

    PR_TAMAÑO,
    PR_TAN,
    PR_TRANSPUESTA,

    PR_UNOS,

    PR_VECTOR,
    PR_WHILE
}