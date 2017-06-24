package fr.evolya.javatoolkit.gui.swing.anglepicker.model;

/**
 * 
 * @author matthieu.lhotellerie
 */
public interface IAnglePickerModel {
    /**
     * 
     * @return
     */
    public double getRadianAngle();

    /**
     * 
     * @param angle
     */
    public void setRadianAngle(double angle);

    /**
     * 
     * @param pointX
     * @param pointY
     * @param centerX
     * @param centerY
     */
    public void setRadianAngle(double pointX, double pointY, double centerX,
            double centerY);

    /**
     * 
     * @return
     */
    boolean isClockwiseAngle();

    /**
     * 
     * @param clockwiseAngle
     */
    void setClockwiseAngle(boolean clockwiseAngle);

    /**
     * 
     * @return
     */
    double getAngleDegreeStep();

    /**
     * 
     * @param angleDegreeStep
     */
    void setAngleDegreeStep(double angleDegreeStep);

    /**
     * @return the northReferenceShift
     */
    public double getNorthReferenceShift();

    /**
     * @param northReferenceShift the northReferenceShift to set
     */
    public void setNorthReferenceShift(double northReferenceShift);
}
