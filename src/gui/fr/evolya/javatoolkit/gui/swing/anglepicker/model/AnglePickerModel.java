package fr.evolya.javatoolkit.gui.swing.anglepicker.model;

import fr.evolya.javatoolkit.gui.swing.anglepicker.AChangeNotifier;

/**
 * 
 * @author matthieu.lhotellerie
 */
public class AnglePickerModel extends AChangeNotifier implements
        IAnglePickerModel {
    /** */
    private double angleDegreeStep = 0;

    /** */
    private boolean clockwiseAngle = false;

    /** */
    private double northReferenceShift = 0;

    /** */
    private double radianAngle = 0;

    /**
     * @return the angleDegreeStep
     */
    @Override
    public double getAngleDegreeStep() {
        return this.angleDegreeStep;
    }

    /**
     * @return the northReferenceShift
     */
    @Override
    public double getNorthReferenceShift() {
        return this.northReferenceShift;
    }

    /**
     * @return the radianAngle
     */
    @Override
    public double getRadianAngle() {
        return this.radianAngle;
    }

    /**
     * @return the clockwiseAngle
     */
    @Override
    public boolean isClockwiseAngle() {
        return this.clockwiseAngle;
    }

    /**
     * @param angleDegreeStep the angleDegreeStep to set
     */
    @Override
    public void setAngleDegreeStep(double angleDegreeStep) {
        this.angleDegreeStep = angleDegreeStep;
    }

    /**
     * @param clockwiseAngle the clockwiseAngle to set
     */
    @Override
    public void setClockwiseAngle(boolean clockwiseAngle) {
        this.clockwiseAngle = clockwiseAngle;
    }

    /**
     * @param northReferenceShift the northReferenceShift to set
     */
    @Override
    public void setNorthReferenceShift(double northReferenceShift) {
        this.northReferenceShift = northReferenceShift;
    }

    @Override
    public void setRadianAngle(double radianAngle) {
        double tempAngle = getFloorPositiveAngle(radianAngle);

        // In case of forced angle step. We work with degree angles
        // ----------------------------
        if (this.angleDegreeStep > 0) {
            double degreeAngle = Math.toDegrees(tempAngle);

            tempAngle = Math.toRadians(getSteppedAngle(degreeAngle,
                    this.angleDegreeStep));
        }

        if (this.radianAngle == tempAngle)
            return;

        this.radianAngle = tempAngle;
        fireStateChanged();
    }

    @Override
    public void setRadianAngle(double pointX, double pointY, double centerX,
            double centerY) {
        double angle = getAngleFromNorth(pointX, pointY, centerX, centerY);

        if (!isClockwiseAngle()) {
            angle = angle - (Math.PI * 2 + getNorthReferenceShift());
        } else {
            angle = reverseAngle(angle);
            angle = angle + (Math.PI * 2 - getNorthReferenceShift());
        }

        setRadianAngle(angle);
    }

    /**
     * To get an angle between 0 and 2PI
     * 
     * @param radianAngle
     * @return
     */
    protected static double getFloorPositiveAngle(double radianAngle) {
        double tempAngle = radianAngle;

        while (tempAngle < 0) {
            tempAngle += Math.PI * 2;
        }

        if (tempAngle > Math.PI * 2) {
            tempAngle = tempAngle % (Math.PI * 2);
        }

        if (tempAngle == Math.PI * 2) {
            tempAngle = 0;
        }

        return tempAngle;
    }

    /**
     * 
     * @param degAngle
     * @param degStep
     * @return
     */
    protected static double getSteppedAngle(double degAngle, double degStep) {
        double degreeAngle = degAngle;
        double rest = degreeAngle % degStep;

        degreeAngle -= rest;

        if (rest > degStep / 2) {
            degreeAngle += degStep;
        }

        if (degreeAngle == 360) {
            degreeAngle = 0;
        }

        return degreeAngle;
    }

    /**
     * 
     * @param degAngle
     * @return
     */
    protected static double reverseAngle(double degAngle) {
        double out = degAngle;

        if (degAngle > 0) {
            while (out > Math.PI * 2) {
                out -= Math.PI * 2;
            }

            out = Math.PI * 2 - out;
        } else if (degAngle < 0) {
            while (out < -Math.PI * 2) {
                out += Math.PI * 2;
            }

            out = -Math.PI * 2 - out;
        }

        return out;
    }

    /**
     * 
     * @param degAngle
     * @param angleShift
     * @return
     */
    protected static double shiftAngle(double degAngle, double angleShift) {
        if (angleShift == 0)
            return degAngle;

        double out = degAngle;

        if (out > 0) {
            out += Math.PI * 2;
        } else if (out < 0) {
            out -= Math.PI * 2;
        }

        out += angleShift;

        return out;
    }

    /**
     * 
     * @param pointX
     * @param pointY
     * @param centerX
     * @param centerY
     * @return
     */
    protected static double getAngleFromNorth(double pointX, double pointY,
            double centerX, double centerY) {
        if (pointX == centerX && pointY == centerY) {
            return 0;
        } else if (pointX == centerX) {
            // NORD
            if (pointY > centerY) {
                return Math.toRadians(0);
            }

            // SUD
            return Math.toRadians(180);
        } else if (pointY == centerY) {
            // EST
            if (pointX > centerX) {
                return Math.toRadians(270);
            }

            // OUEST
            return Math.toRadians(90);
        }

        double x = Math.abs(centerX - pointX);
        double y = Math.abs(pointY - centerY);

        // NORD-OUEST
        if (pointX < centerX && pointY > centerY) {
            return Math.toRadians(90) - Math.atan(y / x);
        }
        // SUD-OUEST
        else if (pointX < centerX && pointY < centerY) {
            return Math.toRadians(90) + Math.atan(y / x);
        }
        // SUD-EST
        else if (pointX > centerX && pointY < centerY) {
            return Math.toRadians(270) - Math.atan(y / x);
        }
        // NORD-EST
        else if (pointX > centerX && pointY > centerY) {
            return Math.toRadians(270) + Math.atan(y / x);
        }

        return 0;
    }
}
