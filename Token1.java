public class Token1 {
    private TipoToken tipo;
    private String lexema;

    public Token1(TipoToken tipo, String lexema) {
        this.tipo = tipo;
        this.lexema = lexema;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }
}