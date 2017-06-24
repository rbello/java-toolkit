package fr.evolya.javatoolkit.gui.swing.anglepicker.config;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.UIManager;

import fr.evolya.javatoolkit.gui.swing.anglepicker.AChangeNotifier;


/**
 * 
 * @author matthieu.lhotellerie
 */
public class AnglePickerConfig extends AChangeNotifier implements
        IAnglePickerConfig {
    /**
     * If <code>true</code> the angle is calculated clowkise direction, else
     * anticlockwise
     */
    private boolean clockwiseAngle = false;

    /** Radius in pixel of the circle (input area) */
    private int circleRadius = 17;

    /**
     * If <code>true</code> show the angle reference line (0° line by default).
     * 
     * @see AnglePickerConfig#colorReference
     * @see AnglePickerConfig#referenceAngleShift
     */
    private boolean showReference = true;

    /**
     * If <code>true</code> show the area between the reference angle and the
     * reference line
     * 
     * @see AnglePickerConfig#colorAngleArea
     * @see AnglePickerConfig#referenceAngleShift
     */
    private boolean showAngleArea = false;

    /**
     * Angle in radian of the reference (the 0� angle), north by default.
     * Shift is also influenced by the angle direction (clockwise /
     * anticlockwise).
     * 
     * For exemple, to set a EAST angle reference, in anticlockwise direction
     * 
     * <pre>
     * setReferenceAngleShift(Math.toRadian(270));
     * </pre>
     * 
     * @see AnglePickerConfig#clockwiseAngle
     * */
    private double referenceAngleShift = 0;

    /** The margins of the component */
    private Insets margin = new Insets(1, 1, 1, 1);

    /** The color of the exterior border of the circle, ticks and reference line */
    private Color colorBorder = UIManager.getColor("TextArea.foreground");

    /** The color of the interior of the circle */
    private Color colorCircle = UIManager.getColor("TextArea.background");

    /** The background color of the component */
    private Color colorBackground = null;

    /** The color of the reference angle line */
    private Color colorReference = UIManager.getColor("TextField.shadow");

    /** The color of the current angle area */
    private Color colorAngleArea = UIManager
            .getColor("TextField.inactiveBackground");

    /** The color of the current angle */
    private Color colorAngleLine = UIManager.getColor("TextArea.foreground");

    /** If <code>true</code>, the painting of the component use antialiasing */
    private boolean antialiasing = true;

    /**
     * Angle step, in degrees. Force any selected angle to be a multiple of this
     * step. Disabled if zero.
     */
    private float angleStep = 0;

    /**
     * Tick angle step, in degrees. Display a tick next to the circle border for
     * each step. Disabled if zero or less.
     * 
     * @see #primaryStepTickSize
     */
    private float primaryStepTick = 0;

    /**
     * The size of tick in pixel
     * 
     * @see #primaryStepTick
     */
    private int primaryStepTickSize = 5;

    /**
     * Tick angle step, in degrees. Display a tick next to the circle border for
     * each step. Disabled if zero or less.
     * 
     * @see #secondStepTickSize
     */
    private float secondStepTick = 0;

    /**
     * The size of tick in pixel
     * 
     * @see #secondStepTickSize
     */
    private int secondStepTickSize = 3;

    // -------------------------------------------------------------------------

    /**
     * @return the primaryStepTick
     */
    @Override
    public float getPrimaryStepTick() {
        return this.primaryStepTick;
    }

    /**
     * @param primaryStepTick the primaryStepTick to set
     */
    @Override
    public void setPrimaryStepTick(float primaryStepTick) {
        if (this.primaryStepTick == primaryStepTick)
            return;

        this.primaryStepTick = primaryStepTick;
        fireStateChanged();
    }

    /**
     * @return the primaryStepTickSize
     */
    @Override
    public int getPrimaryStepTickSize() {
        return this.primaryStepTickSize;
    }

    /**
     * @param primaryStepTickSize the primaryStepTickSize to set
     */
    @Override
    public void setPrimaryStepTickSize(int primaryStepTickSize) {
        if (this.primaryStepTickSize == primaryStepTickSize)
            return;

        this.primaryStepTickSize = primaryStepTickSize;
        fireStateChanged();
    }

    /**
     * @return the secondStepTick
     */
    @Override
    public float getSecondStepTick() {
        return this.secondStepTick;
    }

    /**
     * @param secondStepTick the secondStepTick to set
     */
    @Override
    public void setSecondStepTick(float secondStepTick) {
        if (this.secondStepTick == secondStepTick)
            return;

        this.secondStepTick = secondStepTick;
        fireStateChanged();
    }

    /**
     * @return the secondStepTickSize
     */
    @Override
    public int getSecondStepTickSize() {
        return this.secondStepTickSize;
    }

    /**
     * @param secondStepTickSize the secondStepTickSize to set
     */
    @Override
    public void setSecondStepTickSize(int secondStepTickSize) {
        if (this.secondStepTickSize == secondStepTickSize)
            return;

        this.secondStepTickSize = secondStepTickSize;
        fireStateChanged();
    }

    /**
     * @return the angleStep
     */
    @Override
    public float getAngleStep() {
        return this.angleStep;
    }

    /**
     * @param angleStep the angleStep to set
     */
    @Override
    public void setAngleStep(float angleStep) {
        if (this.angleStep == angleStep)
            return;

        this.angleStep = angleStep;
        fireStateChanged();
    }

    /**
     * @return the circleRadius
     */
    @Override
    public int getCircleRadius() {
        return this.circleRadius;
    }

    /**
     * @param circleRadius the circleRadius to set
     */
    @Override
    public void setCircleRadius(int circleRadius) {
        if (this.circleRadius == circleRadius)
            return;

        this.circleRadius = circleRadius;
        fireStateChanged();
    }

    /**
     * @return the showReference
     */
    @Override
    public boolean isShowReference() {
        return this.showReference;
    }

    /**
     * @param showReference the showReference to set
     */
    @Override
    public void setShowReference(boolean showReference) {
        if (this.showReference == showReference)
            return;

        this.showReference = showReference;
        fireStateChanged();
    }

    /**
     * @return the showAngleArea
     */
    @Override
    public boolean isShowAngleArea() {
        return this.showAngleArea;
    }

    /**
     * @param showAngleArea the showAngleArea to set
     */
    @Override
    public void setShowAngleArea(boolean showAngleArea) {
        if (this.showAngleArea == showAngleArea)
            return;

        this.showAngleArea = showAngleArea;
        fireStateChanged();
    }

    /**
     * @return the referenceAngleShift
     */
    @Override
    public double getReferenceAngleShift() {
        return this.referenceAngleShift;
    }

    /**
     * @param referenceAngleShift the referenceAngleShift to set
     */
    @Override
    public void setReferenceAngleShift(double referenceAngleShift) {
        if (this.referenceAngleShift == referenceAngleShift)
            return;

        this.referenceAngleShift = referenceAngleShift;
        fireStateChanged();
    }

    /**
     * @return the marging
     */
    @Override
    public Insets getMargin() {
        return this.margin;
    }

    /**
     * @param margin the margin to set
     */
    @Override
    public void setMargin(Insets margin) {
        if (this.margin != null && this.margin.equals(margin))
            return;

        if (this.margin == null && margin == null)
            return;

        this.margin = margin;
        fireStateChanged();
    }

    /**
     * @return the colorBorder
     */
    @Override
    public Color getColorBorder() {
        return this.colorBorder;
    }

    /**
     * @param colorBorder the colorBorder to set
     */
    @Override
    public void setColorBorder(Color colorBorder) {
        if (this.colorBorder != null && this.colorBorder.equals(colorBorder))
            return;

        if (this.colorBorder == null && colorBorder == null)
            return;

        this.colorBorder = colorBorder;
        fireStateChanged();
    }

    /**
     * @return the colorCircle
     */
    @Override
    public Color getColorCircle() {
        return this.colorCircle;
    }

    /**
     * @param colorCircle the colorCircle to set
     */
    @Override
    public void setColorCircle(Color colorCircle) {
        if (this.colorCircle != null && this.colorCircle.equals(colorCircle))
            return;

        if (this.colorCircle == null && colorCircle == null)
            return;

        this.colorCircle = colorCircle;
        fireStateChanged();
    }

    /**
     * @return the colorBackground
     */
    @Override
    public Color getColorBackground() {
        return this.colorBackground;
    }

    /**
     * @param colorBackground the colorBackground to set
     */
    @Override
    public void setColorBackground(Color colorBackground) {
        if (this.colorBackground != null
                && this.colorBackground.equals(colorBackground))
            return;

        if (this.colorBackground == null && colorBackground == null)
            return;

        this.colorBackground = colorBackground;
        fireStateChanged();
    }

    /**
     * @return the colorReference
     */
    @Override
    public Color getColorReference() {
        return this.colorReference;
    }

    /**
     * @param colorReference the colorReference to set
     */
    @Override
    public void setColorReference(Color colorReference) {
        if (this.colorReference != null
                && this.colorReference.equals(colorReference))
            return;

        if (this.colorReference == null && colorReference == null)
            return;

        this.colorReference = colorReference;
        fireStateChanged();
    }

    /**
     * @return the colorAngleArea
     */
    @Override
    public Color getColorAngleArea() {
        return this.colorAngleArea;
    }

    /**
     * @param colorAngleArea the colorAngleArea to set
     */
    @Override
    public void setColorAngleArea(Color colorAngleArea) {
        if (this.colorAngleArea != null
                && this.colorAngleArea.equals(colorAngleArea))
            return;

        if (this.colorAngleArea == null && colorAngleArea == null)
            return;

        this.colorAngleArea = colorAngleArea;
        fireStateChanged();
    }

    /**
     * @return the colorAngleLine
     */
    @Override
    public Color getColorAngleLine() {
        return this.colorAngleLine;
    }

    /**
     * @param colorAngleLine the colorAngleLine to set
     */
    @Override
    public void setColorAngleLine(Color colorAngleLine) {
        if (this.colorAngleLine != null
                && this.colorAngleLine.equals(colorAngleLine))
            return;

        if (this.colorAngleLine == null && colorAngleLine == null)
            return;

        this.colorAngleLine = colorAngleLine;
        fireStateChanged();
    }

    /**
     * @return the antialiasing
     */
    @Override
    public boolean isAntialiasing() {
        return this.antialiasing;
    }

    /**
     * @param antialiasing the antialiasing to set
     */
    @Override
    public void setAntialiasing(boolean antialiasing) {
        if (this.antialiasing == antialiasing)
            return;

        this.antialiasing = antialiasing;
        fireStateChanged();
    }

    /**
     * @return the clockwiseAngle
     */
    @Override
    public boolean isClockwiseAngle() {
        return this.clockwiseAngle;
    }

    /**
     * @param clockwiseAngle the clockwiseAngle to set
     */
    @Override
    public void setClockwiseAngle(boolean clockwiseAngle) {
        if (this.clockwiseAngle == clockwiseAngle)
            return;

        this.clockwiseAngle = clockwiseAngle;
        fireStateChanged();
    }
}
