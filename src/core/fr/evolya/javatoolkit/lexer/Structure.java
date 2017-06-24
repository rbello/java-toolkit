package fr.evolya.javatoolkit.lexer;

import java.util.LinkedList;
import java.util.List;

/**
 * Contient toutes les structures reconnues par le lexer
 */
public interface Structure {
    
    // Affiche l'element en xml
    public java.lang.String toXml(java.lang.String prefix);

    public static abstract class Token implements Structure {
    }
    public static abstract class Operator implements Structure {
    }
    
    public static final class Plus extends Operator {

        public static final Plus INSTANCE = new Plus();
        
        private Plus() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<plus />";
        }
        
    }
    
    public static final class Minus extends Operator {

        public static final Minus INSTANCE = new Minus();
        
        private Minus() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<minus />";
        }
        
    }
    
    public static final class Division extends Operator {

        public static final Division INSTANCE = new Division();
        
        private Division() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<division />";
        }
        
    }
    
    public static final class Multiply extends Operator {

        public static final Multiply INSTANCE = new Multiply();
        
        private Multiply() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<multiply />";
        }
        
    }
    
    public static final class Modulo extends Operator {

        public static final Modulo INSTANCE = new Modulo();
        
        private Modulo() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<modulo />";
        }
        
    }
    
    // Une expression est un conteneur de structure
    public static final class Expression implements Structure {

        private List<Structure> struct;
        
        public Expression() {
            struct = new LinkedList<Structure>();
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return toXml(this, prefix);
        }

        private static java.lang.String toXml(Expression ex, java.lang.String prefix) {
            StringBuilder b = new StringBuilder();
            b.append(prefix+"<expression>");
            for (Structure s : ex.getStructures()) {
                b.append("\n"+s.toXml(prefix+prefix));
            }
            b.append("\n"+prefix+"</expression>");
            return b.toString();
        }
        
        public synchronized void addStructure(Structure s) {
            struct.add(s);
        }

        public List<Structure> getStructures() {
            return struct;
        }

        public synchronized Structure getLastElement() {
            // @TODO voir si il ne serait pas plus performant de declarer une
            // variable pour stoquer le dernier element plutot que de faire un size+get
            int size = struct.size();
            return size == 0 ? null : struct.get(size-1);
        }
        
    }
    
    // Une string représente une chaîne de caractère
    public static final class String implements Structure {

        private java.lang.String value = null;
        
        public String(java.lang.String value) {
            this.value = value;
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<string>"+toString()+"</string>";
        }
        
        @Override
        public java.lang.String toString() {
            return value;
        }
        
    }
    
    // Un element null
    public static final class Null extends Token {

        public static final Null INSTANCE = new Null();
        
        private Null() {
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<null />";
        }
        
    }
    
    // Un true boolean
    public static final class True extends Token {

        public static final True INSTANCE = new True();
        
        private True() {
        }
        
        public boolean getValue() {
            return true;
        }

        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<true />";
        }
        
    }
    
    // Un false boolean
    public static final class False extends Token {
        
        public static final False INSTANCE = new False();

        private False() {
        }
        
        public boolean getValue() {
            return false;
        }

        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<false />";
        }
        
    }
    
    public static final class Float extends Token {

        private java.lang.Float value;
        
        public Float(java.lang.Float value) {
            if (value == null) throw new NullPointerException();
            this.value = value;
        }
        
        public boolean isInifinite() {
            return value.isInfinite();
        }
        
        public boolean isNaN() {
            return value.isNaN();
        }

        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<float>"+value+"</float>";
        }
        
    }
    
    public static final class Double extends Token {

        private java.lang.Double value;
        
        public Double(java.lang.Double value) {
            if (value == null) throw new NullPointerException();
            this.value = value;
        }
        
        public boolean isInifinite() {
            return value.isInfinite();
        }
        
        public boolean isNaN() {
            return value.isNaN();
        }

        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<double>"+value.toString().replace('E', 'e')+"</double>";
        }
        
    }
    
    // Un symbole est une variable connue
    public static final class Symbol extends Token {

        private java.lang.String name;

        public Symbol(java.lang.String name) {
            this.name = name;
        }
        
        public java.lang.String getName() {
            return name;
        }
        
        public java.lang.String toXml(java.lang.String prefix) {
            return prefix+"<symbol name=\""+name+"\" />";
        }
        
    }
    
}
