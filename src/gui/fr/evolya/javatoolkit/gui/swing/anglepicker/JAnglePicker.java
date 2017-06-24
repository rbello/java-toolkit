package fr.evolya.javatoolkit.gui.swing.anglepicker;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.evolya.javatoolkit.gui.swing.anglepicker.config.AnglePickerConfig;
import fr.evolya.javatoolkit.gui.swing.anglepicker.config.IAnglePickerConfig;
import fr.evolya.javatoolkit.gui.swing.anglepicker.listener.AngleEvent;
import fr.evolya.javatoolkit.gui.swing.anglepicker.listener.AngleListener;
import fr.evolya.javatoolkit.gui.swing.anglepicker.listener.AngleNotifier;
import fr.evolya.javatoolkit.gui.swing.anglepicker.model.AnglePickerModel;


/**
 * 
 * @author matthieu.lhotellerie
 */
public class JAnglePicker extends JComponent implements IAnglePickerConfig {
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private static final String uiClassID = "AngleSelectorUI";

    /** */
    AnglePickerModel model;

    /** */
    private AnglePickerConfig config;

    /** */
    private ChangeListener changeModelListener;

    /** */
    double previousAngle = 0;

    /** */
    double previousSelectedAngle = 0;

    /** */
    AngleNotifier angleNotifier = new AngleNotifier();

    /** */
    private boolean cursorReleasedInside = false;

    /** */
    private boolean cursorClickedInside = false;

    /**
     * @return the cursorReleasedInside
     */
    public boolean isCursorReleasedInside() {
        return this.cursorReleasedInside;
    }

    /**
     * @param cursorReleasedInside the cursorReleasedInside to set
     */
    public void setCursorReleasedInside(boolean cursorReleasedInside) {
        this.cursorReleasedInside = cursorReleasedInside;
    }

    /**
     * @return the cursorClickedInside
     */
    public boolean isCursorClickedInside() {
        return this.cursorClickedInside;
    }

    /**
     * @param cursorClickedInside the cursorClickedInside to set
     */
    public void setCursorClickedInside(boolean cursorClickedInside) {
        this.cursorClickedInside = cursorClickedInside;
    }

    /**
     * 
     */
    public JAnglePicker() {
        initComponent();
    }

    /**
     * 
     * @param listener
     */
    public void addAngleListener(AngleListener listener) {
        if (listener != null)
            this.angleNotifier.addAngleListener(listener);
    }

    /**
     * 
     * @param listener
     */
    public void removeAngleListener(AngleListener listener) {
        if (listener != null)
            this.angleNotifier.removeAngleListener(listener);
    }

    /**
     * 
     */
    protected void initComponent() {
        this.model = new AnglePickerModel();
        this.config = new AnglePickerConfig();

        this.changeModelListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fireAngleChanging();
            }
        };
        this.model.addChangeListener(this.changeModelListener);

        this.updateUI();
    }

    /**
     * 
     */
    protected void fireAngleChanging() {
        double newValue = this.model.getRadianAngle();
        final double oldValue = this.previousAngle;

        this.previousAngle = newValue;

        firePropertyChange("value", oldValue, newValue);

        this.angleNotifier.fireAngleChanging(new AngleEvent(oldValue, newValue,
                this.cursorReleasedInside, this.cursorClickedInside));

    }

    /**
     * 
     */
    protected void fireAngleChanged() {
        double newValue = this.model.getRadianAngle();
        final double oldValue = this.previousSelectedAngle;

        this.previousSelectedAngle = newValue;

        this.angleNotifier.fireAngleChanged(new AngleEvent(oldValue, newValue,
                this.cursorReleasedInside, this.cursorClickedInside));
    }

    /**
     * @param ui
     */
    public void setUI(AnglePickerUI ui) {
        super.setUI(ui);
    }

    @Override
    public void updateUI() {
        if (UIManager.get(getUIClassID()) != null) {
            setUI((AnglePickerUI) UIManager.getUI(this));
        } else {
            setUI(new AnglePickerUI());
        }
    }

    public AnglePickerUI getUI() {
        return (AnglePickerUI) this.ui;
    }

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * @return the model
     */
    AnglePickerModel getModel() {
        return this.model;
    }

    /**
     * @return the config
     */
    AnglePickerConfig getConfig() {
        return this.config;
    }

    /**
     * 
     * @return
     */
    public double getValueRad() {
        return getModel().getRadianAngle();
    }

    /**
     * 
     * @return
     */
    public double getValueDeg() {
        return Math.toDegrees(getModel().getRadianAngle());
    }

    /**
     * 
     * @param radianAngle
     */
    public void setValueRad(double radianAngle) {
        this.cursorClickedInside = false;
        this.cursorReleasedInside = false;

        getModel().setRadianAngle(radianAngle);

        if (Math.abs(radianAngle - this.previousSelectedAngle) > 0.001) {
            fireAngleChanged();
        }
    }

    /**
     * 
     * @param radianAngle
     */
    public void setValuePrevious(AngleEvent e) {
        this.previousSelectedAngle = e.getPreviousAngleRad();
        getModel().setRadianAngle(e.getPreviousAngleRad());
    }

    /**
     * 
     * @param radianAngle
     */
    public void setValueDeg(double degreeAngle) {
        setValueRad(Math.toRadians(degreeAngle));
    }

    public int getCircleRadius() {
        return getConfig().getCircleRadius();
    }

    public Color getColorAngleArea() {
        return getConfig().getColorAngleArea();
    }

    public Color getColorAngleLine() {
        return getConfig().getColorAngleLine();
    }

    public Color getColorBackground() {
        return getConfig().getColorBackground();
    }

    public Color getColorBorder() {
        return getConfig().getColorBorder();
    }

    public Color getColorCircle() {
        return getConfig().getColorCircle();
    }

    public Color getColorReference() {
        return getConfig().getColorReference();
    }

    public Insets getMargin() {
        return getConfig().getMargin();
    }

    public double getReferenceAngleShift() {
        return getConfig().getReferenceAngleShift();
    }

    public boolean isShowAngleArea() {
        return getConfig().isShowAngleArea();
    }

    public boolean isShowReference() {
        return getConfig().isShowReference();
    }

    public void setCircleRadius(int circleRadius) {
        getConfig().setCircleRadius(circleRadius);
    }

    public void setColorAngleArea(Color colorAngleArea) {
        getConfig().setColorAngleArea(colorAngleArea);
    }

    public void setColorAngleLine(Color colorAngleLine) {
        getConfig().setColorAngleLine(colorAngleLine);
    }

    public void setColorBackground(Color colorBackground) {
        getConfig().setColorBackground(colorBackground);
    }

    public void setColorBorder(Color colorBorder) {
        getConfig().setColorBorder(colorBorder);
    }

    public void setColorCircle(Color colorCircle) {
        getConfig().setColorCircle(colorCircle);
    }

    public void setColorReference(Color colorReference) {
        getConfig().setColorReference(colorReference);
    }

    public void setMargin(Insets margin) {
        getConfig().setMargin(margin);
    }

    public void setReferenceAngleShift(double d) {
        getConfig().setReferenceAngleShift(Math.toRadians(d));
        getModel().setNorthReferenceShift(Math.toRadians(d));
    }

    public void setShowAngleArea(boolean showAngleArea) {
        getConfig().setShowAngleArea(showAngleArea);
    }

    public void setShowReference(boolean showReference) {
        getConfig().setShowReference(showReference);
    }

    public boolean isAntialiasing() {
        return getConfig().isAntialiasing();
    }

    public void setAntialiasing(boolean antialiasing) {
        getConfig().setAntialiasing(antialiasing);
    }

    public boolean isClockwiseAngle() {
        return getConfig().isClockwiseAngle();
    }

    public void setClockwiseAngle(boolean clockwiseAngle) {
        getConfig().setClockwiseAngle(clockwiseAngle);
        getModel().setClockwiseAngle(clockwiseAngle);
    }

    @Override
    public float getAngleStep() {
        return getConfig().getAngleStep();
    }

    @Override
    public void setAngleStep(float angleStep) {
        getConfig().setAngleStep(angleStep);
        getModel().setAngleDegreeStep(angleStep);
    }

    @Override
    public float getPrimaryStepTick() {
        return getConfig().getPrimaryStepTick();
    }

    @Override
    public int getPrimaryStepTickSize() {
        return getConfig().getPrimaryStepTickSize();
    }

    @Override
    public float getSecondStepTick() {
        return getConfig().getSecondStepTick();
    }

    @Override
    public int getSecondStepTickSize() {
        return getConfig().getSecondStepTickSize();
    }

    @Override
    public void setPrimaryStepTick(float primaryStepTick) {
        getConfig().setPrimaryStepTick(primaryStepTick);
    }

    @Override
    public void setPrimaryStepTickSize(int primaryStepTickSize) {
        getConfig().setPrimaryStepTickSize(primaryStepTickSize);
    }

    @Override
    public void setSecondStepTick(float secondStepTick) {
        getConfig().setSecondStepTick(secondStepTick);
    }

    @Override
    public void setSecondStepTickSize(int secondStepTickSize) {
        getConfig().setSecondStepTickSize(secondStepTickSize);
    }
}