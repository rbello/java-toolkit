package fr.evolya.javatoolkit.lexer;

import java.util.regex.Pattern;

/**
 * 
 */
public final class ElementBuilder {

    public static final Pattern PATTERN_FLOAT  = Pattern.compile("^[-+]?[0-9]{0,38}\\.?[0-9]{0,8}$");
    public static final Pattern PATTERN_DOUBLE = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
    
    private ElementBuilder() {
    }
    
    public static final Structure build(String txt) throws Throwable {
        
        if (txt.equalsIgnoreCase("null")) {
            return Structure.Null.INSTANCE;
        }
        else if (txt.equalsIgnoreCase("true")) {
            return Structure.True.INSTANCE;
        }
        else if (txt.equalsIgnoreCase("false")) {
            return Structure.False.INSTANCE;
        }
        else if (PATTERN_FLOAT.matcher(txt).matches()) {
            try {
                Float value = Float.valueOf(txt);
                return new Structure.Float(value);
            } catch (Throwable t) {
                return new Structure.Symbol(txt);
            }
        }
        else if (PATTERN_DOUBLE.matcher(txt).matches()) {
            try {
                Double value = Double.valueOf(txt);
                return new Structure.Double(value);
            } catch (Throwable t) {
                return new Structure.Symbol(txt);
            }
        }
        else return new Structure.Symbol(txt);
        
    }

    public static final Structure.String buildString(String txt) {
        return new Structure.String(txt);
    }

}
