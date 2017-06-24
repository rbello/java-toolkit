package fr.evolya.javatoolkit.gui.swing.anglepicker.config;

import java.awt.Color;
import java.awt.Insets;

/**
 * 
 * @author matthieu.lhotellerie
 */
public interface IAnglePickerConfig {

    public int getCircleRadius();

    public void setCircleRadius(int circleRadius);

    public boolean isShowReference();

    public void setShowReference(boolean showReference);

    public boolean isShowAngleArea();

    public void setShowAngleArea(boolean showAngleArea);

    public double getReferenceAngleShift();

    public void setReferenceAngleShift(double referenceAngleShift);

    public Insets getMargin();

    public void setMargin(Insets margin);

    public Color getColorBorder();

    public void setColorBorder(Color colorBorder);

    public Color getColorCircle();

    public void setColorCircle(Color colorCircle);

    public Color getColorBackground();

    public void setColorBackground(Color colorBackground);

    public Color getColorReference();

    public void setColorReference(Color colorReference);

    public Color getColorAngleArea();

    public void setColorAngleArea(Color colorAngleArea);

    public Color getColorAngleLine();

    public void setColorAngleLine(Color colorAngleLine);

    boolean isAntialiasing();

    void setAntialiasing(boolean antialiasing);

    boolean isClockwiseAngle();

    void setClockwiseAngle(boolean clockwiseAngle);

    void setAngleStep(float angleStep);

    float getAngleStep();

    float getPrimaryStepTick();

    void setPrimaryStepTick(float primaryStepTick);

    int getPrimaryStepTickSize();

    void setPrimaryStepTickSize(int primaryStepTickSize);

    float getSecondStepTick();

    void setSecondStepTick(float secondStepTick);

    void setSecondStepTickSize(int secondStepTickSize);

    int getSecondStepTickSize();
}
