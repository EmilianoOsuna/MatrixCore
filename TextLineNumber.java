import java.awt.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * 
 * NOTA: Esta clase fue tomada de:
 *  https://tips4java.wordpress.com/2009/05/23/text-component-line-number
 * 
 * 
 *  This class will display line numbers for a related text component. The text
 *  component must use the same line height for each line. TextLineNumber
 *  supports wrapped lines and will highlight the line number of the current
 *  line in the text component.
 *
 * Esta clase fue diseñada para ser usada como un componente agregado a la cabecera de fila (row header)
 * de un JScrollPane.
 * 
 * 
 * 
 * 
 * NOTA: Esta clase fue adaptada de un ejemplo público disponible en:
 * https://tips4java.wordpress.com/2009/05/23/text-component-line-number
 * 
 * 
 * 
 */
public class TextLineNumber extends JPanel
    implements CaretListener, DocumentListener, PropertyChangeListener
{
    public final static float LEFT = 0.0f;
    public final static float CENTER = 0.5f;
    public final static float RIGHT = 1.0f;

    private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Componente de texto con el que este componente TextLineNumber está sincronizado

    private JTextComponent component;

    //  Propiedades que pueden cambiarse

    private boolean updateFont;
    private int borderGap;
    private Color currentLineForeground;
    private float digitAlignment;
    private int minimumDisplayDigits;

    //  Mantiene información histórica para reducir el número de veces que el componente
    //  necesita redibujarse

    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    private HashMap<String, FontMetrics> fonts;

    /**
     * Crea un componente de número de línea para un componente de texto. Este ancho mínimo
     * de visualización se basará en 3 dígitos.
     *
     * @param component  el componente de texto relacionado
     */
    public TextLineNumber(JTextComponent component)
    {
        this(component, 3);
    }

    /**
     * Crea un componente de número de línea para un componente de texto.
     *
     * @param component  el componente de texto relacionado
     * @param minimumDisplayDigits  el número de dígitos usados para calcular
     * el ancho mínimo del componente
     */
    public TextLineNumber(JTextComponent component, int minimumDisplayDigits)
    {
        this.component = component;

        setFont( component.getFont() );

		setBorderGap( 5 );
		setCurrentLineForeground( Color.RED);
		setDigitAlignment( RIGHT );
		setMinimumDisplayDigits( minimumDisplayDigits );

        component.getDocument().addDocumentListener(this);
        component.addCaretListener( this );
        component.addPropertyChangeListener("font", this);
    }

    /**
     * Obtiene la propiedad de actualización de fuente.
     *
     * @return la propiedad de actualización de fuente
     */
    public boolean getUpdateFont()
    {
        return updateFont;
    }

    /**
     * Establece la propiedad de actualización de fuente. Indica si esta Fuente debe ser
     * actualizada automáticamente cuando la Fuente del componente de texto relacionado
     * cambie.
     *
     * @param updateFont  cuando es true actualiza la Fuente y repinta los números
     * de línea, de lo contrario solo repinta los números de línea.
     */
    public void setUpdateFont(boolean updateFont)
    {
        this.updateFont = updateFont;
    }

    /**
     * Obtiene el espacio del borde.
     *
     * @return el espacio del borde en píxeles
     */
    public int getBorderGap()
    {
        return borderGap;
    }

    /**
     * El espacio del borde se usa para calcular los márgenes (insets) izquierdo y derecho del
     * borde. El valor predeterminado es 5.
     *
     * @param borderGap  el espacio en píxeles
     */
    public void setBorderGap(int borderGap)
    {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder( new CompoundBorder(OUTER, inner) );
        lastDigits = 0;
        setPreferredWidth();
    }

    /**
     * Obtiene el Color de renderizado de la línea actual.
     *
     * @return el Color usado para renderizar el número de la línea actual
     */
    public Color getCurrentLineForeground()
    {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }

    /**
     * El Color usado para renderizar los dígitos de la línea actual. El predeterminado es Color.RED.
     *
     * @param currentLineForeground  el Color usado para renderizar la línea actual
     */
    public void setCurrentLineForeground(Color currentLineForeground)
    {
        this.currentLineForeground = currentLineForeground;
    }

    /**
     * Obtiene la alineación de los dígitos.
     *
     * @return la alineación de los dígitos pintados
     */
    public float getDigitAlignment()
    {
        return digitAlignment;
    }

    /**
     * Especifica la alineación horizontal de los dígitos dentro del componente.
     * Los valores comunes serían:
     * <ul>
     * <li>TextLineNumber.LEFT
     * <li>TextLineNumber.CENTER
     * <li>TextLineNumber.RIGHT (predeterminado)
     * </ul>
     * @param digitAlignment la alineación float
     */
    public void setDigitAlignment(float digitAlignment)
    {
        this.digitAlignment =
            digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }

    /**
     * Obtiene el mínimo de dígitos a mostrar.
     *
     * @return el mínimo de dígitos a mostrar
     */
    public int getMinimumDisplayDigits()
    {
        return minimumDisplayDigits;
    }

    /**
     * Especifica el número mínimo de dígitos usados para calcular el ancho preferido
     * del componente. El predeterminado es 3.
     *
     * @param minimumDisplayDigits  el número de dígitos usados en el cálculo del
     * ancho preferido
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits)
    {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth();
    }

    /**
     * Calcula el ancho necesario para mostrar el número de línea máximo.
     */
    private void setPreferredWidth()
    {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Actualizar tamaños cuando el número de dígitos en el número de línea cambia

        if (lastDigits != digits)
        {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics( getFont() );
            int width = fontMetrics.charWidth( '0' ) * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize( d );
            setSize( d );
        }
    }

    /**
     * Dibuja los números de línea.
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //  Determina el ancho del espacio disponible para dibujar el número de línea

        FontMetrics fontMetrics = component.getFontMetrics( component.getFont() );
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        //  Determina las filas a dibujar dentro de los límites recortados.

        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel( new Point(0, clip.y) );
        int endOffset = component.viewToModel( new Point(0, clip.y + clip.height) );

        while (rowStartOffset <= endOffset)
        {
            try
            {
                if (isCurrentLine(rowStartOffset))
                    g.setColor( getCurrentLineForeground() );
                else
                    g.setColor( getForeground() );

                //  Obtiene el número de línea como una cadena y luego determina los
                //  desplazamientos "X" e "Y" para dibujar la cadena.

                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth( lineNumber );
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                g.drawString(lineNumber, x, y);

                //  Mover a la siguiente fila

                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            }
            catch(Exception e) {break;}
        }
    }

    /*
     * Necesitamos saber si el cursor está posicionado actualmente en la línea que
     * vamos a pintar para que el número de línea pueda ser resaltado.
     */
    private boolean isCurrentLine(int rowStartOffset)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        if (root.getElementIndex( rowStartOffset ) == root.getElementIndex(caretPosition))
            return true;
        else
            return false;
    }

    /*
     * Obtiene el número de línea a dibujar. Se devolverá una cadena vacía
     * cuando una línea de texto se haya ajustado (wrapped).
     */
    protected String getTextLineNumber(int rowStartOffset)
    {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex( rowStartOffset );
        Element line = root.getElement( index );

        if (line.getStartOffset() == rowStartOffset)
            return String.valueOf(index + 1);
        else
            return "";
    }

    /*
     * Determina el desplazamiento X para alinear correctamente el número de línea cuando se dibuje.
     */
    private int getOffsetX(int availableWidth, int stringWidth)
    {
        return (int)((availableWidth - stringWidth) * digitAlignment);
    }

    /*
     * Determina el desplazamiento Y para la fila actual.
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
        throws BadLocationException
    {
        //  Obtiene el rectángulo delimitador de la fila

        Rectangle r = component.modelToView( rowStartOffset );
        int lineHeight = fontMetrics.getHeight();
        int y = r.y + r.height;
        int descent = 0;

        //  El texto necesita posicionarse por encima de la parte inferior del rectángulo
        //  delimitador basado en el descenso de la(s) fuente(s) contenida(s) en la fila.

        if (r.height == lineHeight)  // se está usando la fuente predeterminada
        {
            descent = fontMetrics.getDescent();
        }
        else  // Necesitamos verificar todos los atributos para cambios de fuente
        {
            if (fonts == null)
                fonts = new HashMap<String, FontMetrics>();

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex( rowStartOffset );
            Element line = root.getElement( index );

            for (int i = 0; i < line.getElementCount(); i++)
            {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer)as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get( key );

                if (fm == null)
                {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics( font );
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }

//
//  Implementación de la interfaz CaretListener
//
    @Override
    public void caretUpdate(CaretEvent e)
    {
        //  Obtiene la línea en la que está posicionado el cursor

        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int currentLine = root.getElementIndex( caretPosition );

        //  Necesita repintar para que el número de línea correcto pueda ser resaltado

        if (lastLine != currentLine)
        {
//          repaint();
            getParent().repaint();
            lastLine = currentLine;
        }
    }

//
//  Implementación de la interfaz DocumentListener
//
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    /*
     * Un cambio en el documento puede afectar el número de líneas de texto mostradas.
     * Por lo tanto, los números de línea también cambiarán.
     */
    private void documentChanged()
    {
        //  La vista del componente no se ha actualizado en el momento en que
        //  se dispara el DocumentEvent

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int endPos = component.getDocument().getLength();
                    Rectangle rect = component.modelToView(endPos);

                    if (rect != null && rect.y != lastHeight)
                    {
                        setPreferredWidth();
//                      repaint();
                        getParent().repaint();
                        lastHeight = rect.y;
                    }
                }
                catch (BadLocationException ex) { /* nada que hacer */ }
            }
        });
    }

//
//  Implementación de la interfaz PropertyChangeListener
//
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getNewValue() instanceof Font)
        {
            if (updateFont)
            {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            }
            else
            {
//              repaint();
                getParent().repaint();
            }
        }
    }
}