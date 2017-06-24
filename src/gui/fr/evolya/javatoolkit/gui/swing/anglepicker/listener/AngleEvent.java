package fr.evolya.javatoolkit.gui.swing.anglepicker.listener;

/**
 * 
 * @author matthieu.lhotellerie
 */
public class AngleEvent {
    /** */
    private double currentAngleRad;

    /** */
    private double previousAngleRad;

    /** */
    private boolean cursorReleasedInside = false;

    /** */
    private boolean cursorClickedInside = false;

    /**
     * @param currentAngleRad
     * @param previousAngleRad
     * @param cursorInside
     * @param clickedInside
     */
    public AngleEvent(double previousAngleRad, double currentAngleRad,
            boolean cursorReleasedInside, boolean cursorClickedInside) {
        super();
        this.currentAngleRad = currentAngleRad;
        this.previousAngleRad = previousAngleRad;
        this.cursorReleasedInside = cursorReleasedInside;
        this.cursorClickedInside = cursorClickedInside;
    }

    /**
     * @return the cursorReleasedInside
     */
    public boolean isCursorReleasedInside() {
        return this.cursorReleasedInside;
    }

    /**
     * @return the cursorClickedInside
     */
    public boolean isCursorClickedInside() {
        return this.cursorClickedInside;
    }

    /**
     * @return the currentAngle
     */
    public double getCurrentAngleRad() {
        return this.currentAngleRad;
    }

    /**
     * @return the currentAngle
     */
    public double getCurrentAngleDeg() {
        return Math.toDegrees(this.currentAngleRad);
    }

    /**
     * @return the previousAngle
     */
    public double getPreviousAngleRad() {
        return this.previousAngleRad;
    }

    /**
     * @return the currentAngle
     */
    public double getPreviousAngleDeg() {
        return Math.toDegrees(this.previousAngleRad);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AngleEvent{" + (float) Math.toDegrees(this.previousAngleRad)
                + "�->" + (float) Math.toDegrees(this.currentAngleRad)
                + "�, releasedInside: " + this.cursorReleasedInside
                + ", clickedInside: " + this.cursorClickedInside + "}";
    }
}
