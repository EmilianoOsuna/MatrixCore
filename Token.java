public class Token {
    private String lexema;
    private int linea;
    private TipoToken tipo;
    private String estadoFinal;
    private boolean existeSimbolo;
    

    public Token(String lexema, int linea) {
        this.lexema = lexema;
        this.linea = linea;
    }  
    public Token(String lexema, int linea, TipoToken tipo, String estadoFinal, boolean existeSimbolo) {
        this.lexema = lexema;
        this.linea = linea;
        this.tipo = tipo;
        this.estadoFinal = estadoFinal;
        this.existeSimbolo = existeSimbolo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getEstadoFinal() {
        return estadoFinal;
    }

    public boolean existeSimbolo() {
        return existeSimbolo;
    }
}
