import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Proyecto_Final_Automatas1 {

    public static AFD obtenerAFD() {
        // DEFINICIÓN DE ESTADOS
        Set<String> estados = Set.of(
            "INICIO",
            "A","AD","ADJ","ADJU","ADJUN","ADJUNT","ADJUNTA",
            "AG","AGR","AGRE","AGREG","AGREGA","AGREGAR",
            "AR","ARC","ARCC","ARCCO","ARCCOS",
            "ARCS","ARCSE","ARCSEN",
            "ARCT","ARCTA","ARCTAN",

            "C","CA","CAD","CADE","CADEN","CADENA",
            "CE","CEL","CELD","CELDA",
            "CER","CERO","CEROS",
            "CO","COF","COFA","COFAC","COFACT","COFACTO","COFACTOR","COFACTORES",
            "COL","COLU","COLUM","COLUMN","COLUMNA",
            "COS",
            "CR","CRE","CREA","CREAR",
            "CRU","CRUZ",

            "D","DE","DET","DETE","DETER","DETERM","DETERMI","DETERMIN","DETERMINA","DETERMINAN","DETERMINANT","DETERMINANTE",
            "DI","DIA","DIAG","DIAGO","DIAGON","DIAGONA","DIAGONAL",

            "EL","ELI","ELIM","ELIMI","ELIMIN","ELIMINA","ELIMINAR",
            "EN","ENT","ENTO","ENTON","ENTONC","ENTONCE","ENTONCES",
            "EX","EXP",

            "F","FI","FIN","FIN_","FIN_S","FIN_SI",
            "FIL","FILA",

            "G","GA","GAU","GAUS","GAUSS","GAUSSJ",

            "I","ID","IDE","IDEN","IDENT","IDENTI","IDENTID","IDENTIDA","IDENTIDAD",
            "IN","INV","INVE","INVER","INVERS","INVERSA",

            "L","LN",
            "LO","LOG",
            "LOG1","LOG10",

            "M","MA","MAG","MAGN","MAGNI","MAGNIT","MAGNITU","MAGNITUD",
            "MAT","MATR","MATRI","MATRIZ",
            "MO","MOS","MOST","MOSTR","MOSTRA","MOSTRAR",
            "MOD","MODI","MODIF","MODIFI","MODIFIC","MODIFICA","MODIFICAR",

            "N","NO","NOR","NORM","NORMA","NORMAL","NORMALI","NORMALIZ","NORMALIZA","NORMALIZAR",
            "NU","NUM",

            "O", "OB","OBT","OBTE","OBTEN","OBTENE","OBTENER",

            "P","PI",
            "PO","POT","POTE","POTEN","POTENC","POTENCI","POTENCIA",
            "PP","PPU","PPUN","PPUNT","PPUNTO",
            "PR","PRO","PROC","PROCE","PROCED","PROCEDI","PROCEDIM","PROCEDIMI","PROCEDIMIE","PROCEDIMIEN","PROCEDIMIENT","PROCEDIMIENTO",

            "R","RA","RAI","RAIZ",
            "RAN","RANG","RANGO",

            "S","SE","SEN",
            "SI",
            "SIN","SINO",

            "T","TA","TAM","TAMA","TAMAÑ","TAMAÑO",
            "TAN",
            "TR","TRA","TRAN","TRANS","TRANSP","TRANSPU","TRANSPUE","TRANSPUES","TRANSPUESTA",

            "U","UN","UNO","UNOS",

            "V","VE","VEC","VECT","VECTO","VECTOR"
        );

        // DEFINICIÓN DEL ALFABETO
        Set<Character> alfabeto = Set.of(
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','Ñ',
            'O','P','Q','R','S','T','U','V','W','X','Y','Z',

            'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
            'o','p','q','r','s','t','u','v','w','x','y','z',

            '0','1','2','3','4','5','6','7','8','9',

            '+','-','*','/','=','<','>','!',

            '[',']','(',')',',',';','_',

            '"',

            ' '
        );

        // DEFINICIÓN DE TRANSICIONES
        Map<String, Map<Character, String>> transiciones = new HashMap<>();
        // TRANSICIONES DE INICIO:
                transiciones.put("INICIO", Map.ofEntries(
            Map.entry('A', "A"),
            Map.entry('C', "C"),
            Map.entry('D', "D"),
            Map.entry('E', "E"),
            Map.entry('F', "F"),
            Map.entry('G', "G"),
            Map.entry('I', "I"),
            Map.entry('L', "L"),
            Map.entry('M', "M"),
            Map.entry('N', "N"),
            Map.entry('O', "O"),
            Map.entry('P', "P"),
            Map.entry('R', "R"),
            Map.entry('S', "S"),
            Map.entry('T', "T"),
            Map.entry('U', "U"),
            Map.entry('V', "V")
        ));

        // Transiciones para todos los estados que comienzan con 'A'
        transiciones.put("A", Map.of(
            'D', "AD",
            'G', "AG",
            'R', "AR"
        ));

        // Para hacer "ADJUNTA"
        transiciones.put("AD",   Map.of('J', "ADJ"));
        transiciones.put("ADJ",  Map.of('U', "ADJU"));
        transiciones.put("ADJU", Map.of('N', "ADJUN"));
        transiciones.put("ADJUN",Map.of('T', "ADJUNT"));
        transiciones.put("ADJUNT",Map.of('A', "ADJUNTA"));

        // Para hacer "AGREGAR"
        transiciones.put("AG",    Map.of('R', "AGR"));
        transiciones.put("AGR",   Map.of('E', "AGRE"));
        transiciones.put("AGRE",  Map.of('G', "AGREG"));
        transiciones.put("AGREG", Map.of('A', "AGREGA"));
        transiciones.put("AGREGA",Map.of('R', "AGREGAR"));

       // "AR" y "ARC" sirven para ARCCOS, ARCSEN y ARCTAN 
        transiciones.put("AR",    Map.of(
            'C', "ARC",
            'T', "ARCT"
        ));

        transiciones.put("ARC",   Map.of(
            'C', "ARCC",
            'S', "ARCS",
            'T', "ARCT"
        ));

        // Para hacer "ARCCOS"
        transiciones.put("ARCC",  Map.of('O', "ARCCO"));
        transiciones.put("ARCCO", Map.of('S', "ARCCOS"));

        // Para hacer "ARCSEN"
        transiciones.put("ARCS",  Map.of('E', "ARCSE"));
        transiciones.put("ARCSE", Map.of('N', "ARCSEN"));

        // Para hacer "ARCTAN"
        transiciones.put("ARCT",  Map.of('A', "ARCTA"));
        transiciones.put("ARCTA", Map.of('N', "ARCTAN"));
        


        // Transiciones para todos los estados que comienzan con 'C'
        transiciones.put("C", Map.of(
            'A', "CA",
            'E', "CE",
            'O', "CO",
            'R', "CR"
        ));

        // Para hacer "CADENA"
        transiciones.put("CA",   Map.of('D', "CAD"));
        transiciones.put("CAD",  Map.of('E', "CADE"));
        transiciones.put("CADE", Map.of('N', "CADEN"));
        transiciones.put("CADEN",Map.of('A', "CADENA"));

        //Para hacer CELDA y CEROS
        transiciones.put("CE",     Map.of(
                    'L', "CEL",
                    'R', "CER"
                ));
        // Para hacer "CELDA"
        transiciones.put("CEL", Map.of('D', "CELD"));
        transiciones.put("CELD",Map.of('A', "CELDA"));

        // Para hacer "CEROS"
        transiciones.put("CER",  Map.of('O', "CERO"));
        transiciones.put("CERO", Map.of('S', "CEROS"));

        // Para hacer "COFACTORES"
        transiciones.put("CO",     Map.of(
            'F', "COF",
            'L', "COL"
        ));

        transiciones.put("COF",     Map.of('A', "COFA"));
        transiciones.put("COFA",    Map.of('C', "COFAC"));
        transiciones.put("COFAC",   Map.of('T', "COFACT"));
        transiciones.put("COFACT",  Map.of('O', "COFACTO"));
        transiciones.put("COFACTO", Map.of('R', "COFACTOR"));
        transiciones.put("COFACTOR",Map.of('E', "COFACTORE"));
        transiciones.put("COFACTORE", Map.of('S', "COFACTORES"));

        // Para hacer "COLUMNA"
        transiciones.put("COL",   Map.of('U', "COLU"));
        transiciones.put("COLU",  Map.of('M', "COLUM"));
        transiciones.put("COLUM", Map.of('N', "COLUMN"));
        transiciones.put("COLUMN",Map.of('A', "COLUMNA"));

        // Para hacer "COS"
        transiciones.put("CO", Map.of(
            'F', "COF",    // ya existente para COFACTORES
            'L', "COL",    // ya existente para COLUMNA
            'S', "COS"     // para COS
        ));

        // Para hacer "CREAR"
        transiciones.put("CR",   Map.of(
            'E', "CRE",    // C R E ...
            'U', "CRU"     // C R U Z
        ));

        transiciones.put("CRE",  Map.of('A', "CREA"));
        transiciones.put("CREA", Map.of('R', "CREAR"));

        // Para hacer "CRUZ"
        transiciones.put("CRU", Map.of('Z', "CRUZ"));

        // Transiciones para todos los estados que comienzan con 'D'
        transiciones.put("D", Map.of(
            'E', "DE",
            'I', "DI"
        ));

        // Para hacer "DETERMINANTE"
        transiciones.put("DE",          Map.of('T', "DET"));
        transiciones.put("DET",         Map.of('E', "DETE"));
        transiciones.put("DETE",        Map.of('R', "DETER"));
        transiciones.put("DETER",       Map.of('M', "DETERM"));
        transiciones.put("DETERM",      Map.of('I', "DETERMI"));
        transiciones.put("DETERMI",     Map.of('N', "DETERMIN"));
        transiciones.put("DETERMIN",    Map.of('A', "DETERMINA"));
        transiciones.put("DETERMINA",   Map.of('N', "DETERMINAN"));
        transiciones.put("DETERMINAN",  Map.of('T', "DETERMINANT"));
        transiciones.put("DETERMINANT", Map.of('E', "DETERMINANTE"));

        // Para hacer "DIAGONAL"
        transiciones.put("DI",      Map.of('A', "DIA"));
        transiciones.put("DIA",     Map.of('G', "DIAG"));
        transiciones.put("DIAG",    Map.of('O', "DIAGO"));
        transiciones.put("DIAGO",   Map.of('N', "DIAGON"));
        transiciones.put("DIAGON",  Map.of('A', "DIAGONA"));
        transiciones.put("DIAGONA",     Map.of('L', "DIAGONAL"));



        // Transiciones para todos los estados que comienzan con 'E'
        transiciones.put("E", Map.of(
            'L', "EL",
            'N', "EN",
            'X', "EX"
        ));

        // Para hacer "ELIMINAR"
        transiciones.put("EL",   Map.of('I', "ELI"));
        transiciones.put("ELI",  Map.of('M', "ELIM"));
        transiciones.put("ELIM", Map.of('I', "ELIMI"));
        transiciones.put("ELIMI",Map.of('N', "ELIMIN"));
        transiciones.put("ELIMIN",Map.of('A', "ELIMINA"));
        transiciones.put("ELIMINA",Map.of('R', "ELIMINAR"));

        // Para hacer "ENTONCES"
        transiciones.put("EN",   Map.of('T', "ENT"));
        transiciones.put("ENT",  Map.of('O', "ENTO"));
        transiciones.put("ENTO", Map.of('N', "ENTON"));
        transiciones.put("ENTON",Map.of('C', "ENTONC"));
        transiciones.put("ENTONC",Map.of('E', "ENTONCE"));
        transiciones.put("ENTONCE",Map.of('S', "ENTONCES"));

        // Para hacer "EXP"
        transiciones.put("EX", Map.of('P', "EXP"));

        // Transiciones para todos los estados que comienzan con 'F'
        transiciones.put("F", Map.of('I', "FI"));

        transiciones.put("FI", Map.of(
                    'N', "FIN",   
                    'L', "FIL"    
        ));
        // Para hacer "FIN_SI"
        transiciones.put("FIN", Map.of('_', "FIN_"));
        transiciones.put("FIN_",Map.of('S', "FIN_S"));
        transiciones.put("FIN_S",Map.of('I', "FIN_SI"));
        
        // Para hacer "FILA"
        transiciones.put("FIL", Map.of('A', "FILA"));

        // Transiciones para todos los estados que comienzan con 'G'
        transiciones.put("G", Map.of('A', "GA"));

        // Para hacer "GAUSS"
        transiciones.put("GA",  Map.of('U', "GAU"));
        transiciones.put("GAU", Map.of('S', "GAUS"));
        transiciones.put("GAUS",Map.of('S', "GAUSS"));

        // Para hacer "GAUSSJ"
        transiciones.put("GAUSS", Map.of('J', "GAUSSJ"));

        // Transiciones para todos los estados que comienzan con 'I'
        transiciones.put("I", Map.of(
            'D', "ID",
            'N', "IN"
        ));

        // Para hacer "IDENTIDAD"
        transiciones.put("ID",   Map.of('E', "IDE"));
        transiciones.put("IDE",  Map.of('N', "IDEN"));
        transiciones.put("IDEN", Map.of('T', "IDENT"));
        transiciones.put("IDENT",Map.of('I', "IDENTI"));
        transiciones.put("IDENTI",Map.of('D', "IDENTID"));
        transiciones.put("IDENTID",Map.of('A', "IDENTIDA"));
        transiciones.put("IDENTIDA",Map.of('D', "IDENTIDAD"));

        // Para hacer "INVERSA"
        transiciones.put("IN",   Map.of('V', "INV"));
        transiciones.put("INV",  Map.of('E', "INVE"));
        transiciones.put("INVE", Map.of('R', "INVER"));
        transiciones.put("INVER",Map.of('S', "INVERS"));
        transiciones.put("INVERS",Map.of('A', "INVERSA"));


        // Transiciones para todos los estados que comienzan con 'L'
        transiciones.put("L", Map.of(
            'N', "LN",
            'O', "LO"
        ));

        // Para "LN"
        transiciones.put("LN", Map.of());

        // Para hacer "LOG"
        transiciones.put("LO",   Map.of('G', "LOG"));

        // Para hacer "LOG10"
        transiciones.put("LOG",  Map.of('1', "LOG1"));
        transiciones.put("LOG1", Map.of('0', "LOG10"));


        // Transiciones para todos los estados que comienzan con 'M'
        transiciones.put("M", Map.of(
            'A', "MA",
            'E', "ME",
            'O', "MO"
        ));
        
        // Para hacer "METODO"
        transiciones.put("ME",  Map.of('T', "MET"));
        transiciones.put("MET", Map.of('O', "METO"));
        transiciones.put("METO", Map.of('D', "METOD"));
        transiciones.put("METOD", Map.of('O', "METODO"));
        
        transiciones.put("MA",    Map.of(
            'G', "MAG",
            'T', "MAT"
            ));
        
        // Para hacer "MAGNITUD"
        transiciones.put("MAG",   Map.of('N', "MAGN"));
        transiciones.put("MAGN",  Map.of('I', "MAGNI"));
        transiciones.put("MAGNI", Map.of('T', "MAGNIT"));
        transiciones.put("MAGNIT",Map.of('U', "MAGNITU"));
        transiciones.put("MAGNITU",Map.of('D', "MAGNITUD"));

        // Para hacer "MATRIZ"
        transiciones.put("MAT",   Map.of('R', "MATR"));
        transiciones.put("MATR",  Map.of('I', "MATRI"));
        transiciones.put("MATRI", Map.of('Z', "MATRIZ"));

        transiciones.put("MO", Map.of(
            'S', "MOS",
            'D', "MOD"
        ));

        // Para hacer "MOSTRAR"
        transiciones.put("MOS",    Map.of('T', "MOST"));
        transiciones.put("MOST",   Map.of('R', "MOSTR"));
        transiciones.put("MOSTR",  Map.of('A', "MOSTRA"));
        transiciones.put("MOSTRA", Map.of('R', "MOSTRAR"));

        // Para hacer "MODIFICAR"
        transiciones.put("MOD",    Map.of('I', "MODI"));
        transiciones.put("MODI",   Map.of('F', "MODIF"));
        transiciones.put("MODIF",  Map.of('I', "MODIFI"));
        transiciones.put("MODIFI", Map.of('C', "MODIFIC"));
        transiciones.put("MODIFIC",Map.of('A', "MODIFICA"));
        transiciones.put("MODIFICA",Map.of('R', "MODIFICAR"));

        // Transiciones para todos los estados que comienzan con 'N'
        transiciones.put("N", Map.of(
            'O', "NO",
            'U', "NU"
        ));

        // Para hacer "NORMALIZAR"
        transiciones.put("NO",  Map.of('R', "NOR"));
        transiciones.put("NOR", Map.of('M', "NORM"));
        transiciones.put("NORM",Map.of('A', "NORMA"));
        transiciones.put("NORMA",Map.of('L', "NORMAL"));
        transiciones.put("NORMAL",Map.of('I', "NORMALI"));
        transiciones.put("NORMALI",Map.of('Z', "NORMALIZ"));
        transiciones.put("NORMALIZ",Map.of('A', "NORMALIZA"));
        transiciones.put("NORMALIZA",Map.of('R', "NORMALIZAR"));
        
        // Para hacer "NUM"
        transiciones.put("NU", Map.of('M', "NUM"));

        // Transiciones para todos los estados que comienzan con 'O'
        transiciones.put("O", Map.of('B', "OB"));

        // Para hacer "OBTENER"
        transiciones.put("OB",  Map.of('T', "OBT"));
        transiciones.put("OBT", Map.of('E', "OBTE"));
        transiciones.put("OBTE",Map.of('N', "OBTEN"));
        transiciones.put("OBTEN",Map.of('E', "OBTENE"));
        transiciones.put("OBTENE",Map.of('R', "OBTENER"));


        // Transiciones para todos los estados que comienzan con 'P'
        transiciones.put("P", Map.of(
            'I', "PI",
            'O', "PO",
            'P', "PP",
            'R', "PR"
        ));

        // Para "PI"
        transiciones.put("PI", Map.of());

        // Para hacer "POTENCIA"
        transiciones.put("PO",   Map.of('T', "POT"));
        transiciones.put("POT",  Map.of('E', "POTE"));
        transiciones.put("POTE", Map.of('N', "POTEN"));
        transiciones.put("POTEN",Map.of('C', "POTENC"));
        transiciones.put("POTENC",Map.of('I', "POTENCI"));
        transiciones.put("POTENCI",Map.of('A', "POTENCIA"));

        // Para hacer "PPUNTO"
        transiciones.put("PP",   Map.of('U', "PPU"));
        transiciones.put("PPU",  Map.of('N', "PPUN"));
        transiciones.put("PPUN", Map.of('T', "PPUNT"));
        transiciones.put("PPUNT",Map.of('O', "PPUNTO"));

        // Para hacer "PROCEDIMIENTO"
        transiciones.put("PR",   Map.of('O', "PRO"));
        transiciones.put("PRO",  Map.of('C', "PROC"));
        transiciones.put("PROC", Map.of('E', "PROCE"));
        transiciones.put("PROCE",Map.of('D', "PROCED"));
        transiciones.put("PROCED", Map.of('I', "PROCEDI"));
        transiciones.put("PROCEDI",Map.of('M', "PROCEDIM"));
        transiciones.put("PROCEDIM",Map.of('I', "PROCEDIMI"));
        transiciones.put("PROCEDIMI",Map.of('E', "PROCEDIMIE"));
        transiciones.put("PROCEDIMIE",Map.of('N', "PROCEDIMIEN"));
        transiciones.put("PROCEDIMIEN",Map.of('T', "PROCEDIMIENT"));
        transiciones.put("PROCEDIMIENT",Map.of('O', "PROCEDIMIENTO"));



        // Transiciones para todos los estados que comienzan con 'R'
        transiciones.put("R", Map.of(
            'A', "RA"
        ));

        transiciones.put("RA",  Map.of(
            'I', "RAI",
            'N', "RAN"
            ));

        // Para hacer "RAIZ"
        transiciones.put("RAI", Map.of('Z', "RAIZ"));

        // Para hacer "RANGO"
        transiciones.put("RAN", Map.of('G', "RANG"));
        transiciones.put("RANG",Map.of('O', "RANGO"));

        // Transiciones para todos los estados que comienzan con 'S'
        transiciones.put("S", Map.of(
            'E', "SE",
            'I', "SI"
        ));

        // Para hacer "SEN"
        transiciones.put("SE", Map.of('N', "SEN"));

        // Para "SI"
        transiciones.put("SI", Map.of('N', "SIN"));

        // Para hacer "SINO"
        transiciones.put("SIN", Map.of('O', "SINO"));

        // Transiciones para todos los estados que comienzan con 'T'
        transiciones.put("T", Map.of(
            'A', "TA",
            'R', "TR"
        ));

        // Para hacer "TAMAÑO"
        transiciones.put("TA",   Map.of('M', "TAM"));
        transiciones.put("TAM",  Map.of('A', "TAMA"));
        transiciones.put("TAMA", Map.of('Ñ', "TAMAÑ"));
        transiciones.put("TAMAÑ",Map.of('O', "TAMAÑO"));

        // Para hacer "TAN"
        transiciones.put("TA", Map.of(
            'M', "TAM",
            'N', "TAN"
        ));

        // Para hacer "TRANSPUESTA"
        transiciones.put("TR",          Map.of('A', "TRA"));
        transiciones.put("TRA",         Map.of('N', "TRAN"));
        transiciones.put("TRAN",        Map.of('S', "TRANS"));
        transiciones.put("TRANS",       Map.of('P', "TRANSP"));
        transiciones.put("TRANSP",      Map.of('U', "TRANSPU"));
        transiciones.put("TRANSPU",     Map.of('E', "TRANSPUE"));
        transiciones.put("TRANSPUE",    Map.of('S', "TRANSPUES"));
        transiciones.put("TRANSPUES",   Map.of('T', "TRANSPUEST"));
        transiciones.put("TRANSPUEST",  Map.of('A', "TRANSPUESTA"));


        // Transiciones para todos los estados que comienzan con 'U'
        transiciones.put("U", Map.of('N', "UN"));

        // Para hacer "UNOS"
        transiciones.put("UN",  Map.of('O', "UNO"));
        transiciones.put("UNO", Map.of('S', "UNOS"));


        // Transiciones para todos los estados que comienzan con 'V'
        transiciones.put("V", Map.of('E', "VE"));

        // Para hacer "VECTOR"
        transiciones.put("VE",   Map.of('C', "VEC"));
        transiciones.put("VEC",  Map.of('T', "VECT"));
        transiciones.put("VECT", Map.of('O', "VECTO"));
        transiciones.put("VECTO",Map.of('R', "VECTOR"));
        
        String estadoInicial = "INICIO";

        // 4. Mueve aquí los estados de aceptación
        Set<String> estadosAceptacion = Set.of(
            "ADJUNTA",
            "AGREGAR",
            "ARCCOS",
            "ARCSEN",
            "ARCTAN",

            "CADENA",
            "CELDA",
            "CEROS",
            "COFACTORES",
            "COLUMNA",
            "COS",
            "CREAR",
            "CRUZ",

            "DETERMINANTE",
            "DIAGONAL",

            "ELIMINAR",
            "ENTONCES",
            "EXP",

            "FILA",
            "FIN_SI",

            "GAUSS",
            "GAUSSJ",

            "IDENTIDAD",
            "INVERSA",

            "LN",
            "LOG",
            "LOG10",

            "MAGNITUD",
            "MATRIZ",
            "MODIFICAR",
            "MOSTRAR",

            "NORMALIZAR",
            "NUM",

            "OBTENER",

            "PI",
            "POTENCIA",
            "PPUNTO",
            "PROCEDIMIENTO",

            "RAIZ",
            "RANGO",

            "SEN",
            "SI",
            "SINO",

            "TAMAÑO",
            "TAN",
            "TRANSPUESTA",

            "UNOS",

            "VECTOR"
        );

        // AFD COMPLETO
        return new AFD(estados, alfabeto, transiciones, estadoInicial, estadosAceptacion);
    }

    public static void main(String[] args) {
        
        AFD afd = obtenerAFD();

        String codigo = """
            // Línea 1
            CREAR CADENA msg1 = "Resultado: " + (5 + 3);                     //(OK)
            
            // Línea 2
            CREAR CADENA msg2 = "Área: " + (base * altura);                  //(OK)
            
            // Línea 3
            CREAR CADENA msg3 = "Valor: " + (SEN(45) + COS(30));             //(OK)
            
            // Línea 4
            CREAR CADENA msg4 = "Error " + 5 + 3;                             //(304) falta paréntesis
            
            // Línea 5
            CREAR CADENA msg5 = "X = " + (5 + );                              //(201) expresión numérica incompleta
            
            // Línea 6
            CREAR CADENA msg6 = "Dato: " + ();                                //(203) expresión vacía
            
            // Línea 7
            CREAR CADENA msg7 = "Total: " + (5 * (2 + 3));                   //(OK)
            
            // Línea 8
            CREAR CADENA msg8 = "Número: " + (RAIZ());                        //(203) argumento vacío
            
            // Línea 9
            CREAR CADENA msg9 = "Resultado " + (5 + 3) + " unidades";         //(OK)
            
            // Línea 10
            CREAR CADENA msg10 = "Prueba " + 5 * 3;                           //(304) expresión sin paréntesis
            CREAR CADENA msg10 = "Prueba " + (5 * 3)(;
        """;

        Token[] tokens = tokenizador(codigo);

        for (Token tk : tokens) {
            System.out.println(tk.getLexema() + " (linea " + tk.getLinea() + ")");
        }

        List<Token> tablaSimbolos = afd.aceptar(tokens);
        
        System.out.println("\n=== Tabla de Símbolos ===");
        System.out.println("Lexema\t\tLínea\tTipoToken\tEstado Final\tExiste Símbolo");
        for (Token tk : tablaSimbolos) {
            System.out.printf("%-12s\t%-5d\t%-9s\t%-12s\t%-14s\n",
                tk.getLexema(),
                tk.getLinea(),
                tk.getTipo(),
                tk.getEstadoFinal(),
                tk.existeSimbolo() ? "Sí" : "No"
            );
        }
            //ANÁLISIS SINTÁCTICO:
            List<Token> tokensParaParser = new ArrayList();
            for(Token tkParser:tablaSimbolos){
                if(tkParser.existeSimbolo()){
                    tokensParaParser.add(tkParser);
                }
            }
            ParserLL1 parser = new ParserLL1(tokensParaParser);

                // Iniciar análisis sintáctico
                parser.inicio();

                // Mostrar errores si existen
                if (parser.errores.hayErrores()) {
                    System.out.println("Errores sintácticos encontrados:");
                    parser.errores.imprimirTabla();
                } else {
                    System.out.println("Análisis sintáctico finalizado correctamente.");
                }

    }
        public static String eliminarComentarios(String entrada) {

            StringBuilder salida = new StringBuilder();
            int i = 0;
            int n = entrada.length();

            while (i < n) {

                // Comentario de línea //
                if (i + 1 < n && entrada.charAt(i) == '/' && entrada.charAt(i + 1) == '/') {
                    i += 2;

                    // eliminar contenido PERO conservar salto de línea
                    while (i < n && entrada.charAt(i) != '\n') i++;

                    // si llega el salto, lo copiamos
                    if (i < n && entrada.charAt(i) == '\n') {
                        salida.append('\n');
                        i++;
                    }
                }

                // Comentario de bloque /*
                else if (i + 1 < n && entrada.charAt(i) == '/' && entrada.charAt(i + 1) == '*') {

                    i += 2;
                    boolean cerrado = false;

                    // procesar todo el bloque
                    while (i < n) {

                        // si encontramos cierre */
                        if (i + 1 < n && entrada.charAt(i) == '*' && entrada.charAt(i + 1) == '/') {
                            i += 2;
                            cerrado = true;
                            break;
                        }

                        // Siempre conservar saltos de línea
                        if (entrada.charAt(i) == '\n') {
                            salida.append('\n');
                        }

                        i++;
                    }

                    // si no se cerró, seguir eliminando lo que queda, pero conservando saltos
                    if (!cerrado) {
                        while (i < n) {
                            if (entrada.charAt(i) == '\n') salida.append('\n');
                            i++;
                        }
                        return salida.toString();
                    }
                }

                // Caracter normal
                else {
                    salida.append(entrada.charAt(i));
                    i++;
                }
            }

            return salida.toString();
        }


        public static String[] tokenizarLinea(String entrada) {
            List<String> tokens = new ArrayList<>();
            StringBuilder actual = new StringBuilder();
            boolean enCadena = false;

            for (int i = 0; i < entrada.length(); i++) {
                char c = entrada.charAt(i);

                // Inicio / Fin de cadena
                if (c == '"') {
                    actual.append(c);
                    enCadena = !enCadena;
                    continue;
                }

                // Si estamos dentro de una cadena, TODO se copia
                if (enCadena) {
                    actual.append(c);
                    continue;
                }

                // Espacio fuera de cadena → cortar token
                if (Character.isWhitespace(c)) {
                    if (actual.length() > 0) {
                        tokens.add(actual.toString());
                        actual.setLength(0);
                    }
                    continue;
                }

                // Operadores de dos caracteres
                if (i + 1 < entrada.length()) {
                    String doble = "" + c + entrada.charAt(i + 1);
                    if (doble.equals("==") || doble.equals("!=") ||
                        doble.equals("<=") || doble.equals(">=")) {

                        if (actual.length() > 0) {
                            tokens.add(actual.toString());
                            actual.setLength(0);
                        }

                        tokens.add(doble);
                        i++;
                        continue;
                    }
                }

                // Operadores simples
                if ("+-*/=<>()[],;!".indexOf(c) != -1) {
                    if (actual.length() > 0) {
                        tokens.add(actual.toString());
                        actual.setLength(0);
                    }
                    tokens.add(String.valueOf(c));
                    continue;
                }

                // Caracter normal
                actual.append(c);
            }

            // Último token
            if (actual.length() > 0) {
                tokens.add(actual.toString());
            }

            return tokens.toArray(new String[0]);
        }

        public static Token[] tokenizador(String entrada) {
            // Eliminar comentarios antes de procesar
            entrada = eliminarComentarios(entrada);
            String[] lineas = entrada.split("\n");
            Token[] tokensTemp = new Token[10000]; // capacidad grande temporal
            int contador = 0;

            int numLinea = 1;

            for (String linea : lineas) {

                String[] toks = tokenizarLinea(linea);

                for (String t : toks) {
                    tokensTemp[contador++] = new Token(t, numLinea);
                }

                numLinea++;
            }

            // Convertir a arreglo exacto
            Token[] resultado = new Token[contador];
            System.arraycopy(tokensTemp, 0, resultado, 0, contador);

            return resultado;
        }
}
