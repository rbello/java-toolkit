package fr.evolya.javatoolkit.gui.swing.anglepicker;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.evolya.javatoolkit.gui.swing.anglepicker.config.IAnglePickerConfig;
import fr.evolya.javatoolkit.gui.swing.anglepicker.model.IAnglePickerModel;


/**
 * 
 * @author matthieu.lhotellerie
 */
public class AnglePickerUI extends AComponentUI<JAnglePicker> {

    // ---- CONSTANTS ----------------------------------------------------------

    /** */
    private static final int CENTER_DIAMETER = 3;

    // ---- PRIVATE FIELDS -----------------------------------------------------

    /** */
    double centerX;

    /** */
    double centerY;

    /** */
    double refX;

    /** */
    double refY;

    /** */
    Ellipse2D circle;

    /** */
    private Rectangle2D centerSquare;

    /** */
    private Line2D referenceLine;

    // ---- PROTECTED FIELDS ---------------------------------------------------

    /** */
    protected MouseListener mouseListener;

    /** */
    protected MouseMotionListener mouseMotionListener;

    /** */
    protected ChangeListener changeModelListener;

    /** */
    protected ChangeListener changeConfigListener;

    /** */
    protected boolean isClickedInside = false;

    private ComponentAdapter componentListener;

    // ---- ACCESSORS ----------------------------------------------------------

    /**
     * @return the component
     */
    JAnglePicker getComponent() {
        return this.component;
    }

    /**
     * @param component the component to set
     */
    void setComponent(JAnglePicker component) {
        this.component = component;
    }

    // ---- PUBLIC METHODS -----------------------------------------------------

    /**
     * 
     * @return
     */
    public IAnglePickerConfig getConfig() {
        return getComponent().getConfig();
    }

    @Override
    public void installComponents() {
        IAnglePickerConfig config = getComponent().getConfig();

        // Geometry
        // ---------------------------------

        this.centerX = config.getCircleRadius() + config.getMargin().left;
        this.centerY = config.getCircleRadius() + config.getMargin().top;

        this.refX = this.centerX;
        this.refY = config.getMargin().top + 1;

        this.circle = new Ellipse2D.Double(config.getMargin().left, config
                .getMargin().top, config.getCircleRadius() * 2, config
                .getCircleRadius() * 2);

        this.centerSquare = new Rectangle2D.Double(this.centerX
                - CENTER_DIAMETER / 2, this.centerY - CENTER_DIAMETER / 2,
                CENTER_DIAMETER, CENTER_DIAMETER);

        if (config.isShowReference()) {
            if (config.getReferenceAngleShift() == 0) {
                this.referenceLine = new Line2D.Double(this.centerX,
                        this.centerY, this.refX, this.refY);
            } else {
                double angle = config.getReferenceAngleShift();

                this.referenceLine = new Line2D.Double(this.centerX,
                        this.centerY, rotateRefPointX(angle),
                        rotateRefPointY(angle));
            }
        } else {
            this.referenceLine = null;
        }
    }

    @Override
    public void installDefaults() {
        //
    }

    @Override
    public void installListeners() {
        // Listener on mouse click
        // ---------------------------------
        this.mouseListener = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Point point = new Point(e.getX(), e.getY());
                AnglePickerUI.this.isClickedInside = false;

                getComponent().setCursorReleasedInside(
                        AnglePickerUI.this.circle.contains(point));

                getComponent().fireAngleChanged();
                getComponent().setCursorClickedInside(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point point = new Point(e.getX(), e.getY());

                AnglePickerUI.this.isClickedInside = AnglePickerUI.this.circle
                        .contains(point);

                getComponent().setCursorClickedInside(
                        AnglePickerUI.this.isClickedInside);

                setAngle(e);
            }
        };
        this.getComponent().addMouseListener(this.mouseListener);

        // Listener on mouse model
        // ---------------------------------
        this.mouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setAngle(e);
            }

        };
        this.getComponent().addMouseMotionListener(this.mouseMotionListener);

        // Listener on model change
        // ---------------------------------
        this.changeModelListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                getComponent().repaint();
            }
        };
        this.getComponent().getModel().addChangeListener(
                this.changeModelListener);

        // Listener on config change
        // ---------------------------------
        this.changeConfigListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                installDefaults();
                installComponents();
                AnglePickerUI.this.getComponent().repaint();
            }
        };
        this.getComponent().getConfig().addChangeListener(
                this.changeConfigListener);

        // Listener on component resize
        // ---------------------------------
        this.componentListener = new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                doResize();
            }
        };

        this.getComponent().addComponentListener(this.componentListener);
    }

    protected void doResize() {
        IAnglePickerConfig config = getConfig();
        Insets margin = config.getMargin();
        Rectangle bounds = getComponent().getBounds();

        // New circle radius
        // -------------------------
        int height = bounds.height - margin.top - margin.bottom;
        int width = bounds.width - margin.left - margin.right;

        int min = (height < width) ? height : width;

        getConfig().setCircleRadius(min / 2);
    }

    @Override
    public void uninstallComponents() {
        this.circle = null;
        this.centerSquare = null;
        this.referenceLine = null;
    }

    @Override
    public void uninstallDefaults() {
        //
    }

    @Override
    public void uninstallListeners() {
        this.component.removeMouseListener(this.mouseListener);
        this.mouseListener = null;

        this.component.removeMouseMotionListener(this.mouseMotionListener);
        this.mouseMotionListener = null;

        this.component.getModel()
                .removeChangeListener(this.changeModelListener);
        this.changeModelListener = null;

        this.component.getConfig().removeChangeListener(
                this.changeConfigListener);
        this.changeConfigListener = null;

        this.component.removeComponentListener(this.componentListener);
        this.componentListener = null;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        IAnglePickerConfig config = getComponent().getConfig();

        int dim = this.component.getConfig().getCircleRadius() * 2 + 1;

        int x = dim + config.getMargin().left + +config.getMargin().right;
        int y = dim + config.getMargin().top + +config.getMargin().bottom;
        return new Dimension(x, y);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        Graphics2D g2 = (Graphics2D) g;

        // Turn on or off antialising
        // ---------------------------------
        if (getConfig().isAntialiasing()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        paintBackground(g2);
        paintCircleBackground(g2);
        paintAngleArea(g2);
        paintReferenceAngle(g2);
        paintCurrentAngle(g2);
        paintCenter(g2);
        paintPrimaryTick(g2);
        paintSecondaryTick(g2);
        paintCircleForeground(g2);
    }

    // ---- PROTECTED METHODS --------------------------------------------------

    /**
     * 
     * @param e
     */
    protected void setAngle(MouseEvent e) {
        if (!getComponent().isEnabled())
            return;

        if (!AnglePickerUI.this.isClickedInside)
            return;

        IAnglePickerModel model = getComponent().getModel();

        model.setRadianAngle(e.getX(), -e.getY(), AnglePickerUI.this.centerX,
                -AnglePickerUI.this.centerY);
    }

    /**
     * 
     * @param g2
     */
    protected void paintBackground(Graphics2D g2) {
        if (getConfig().getColorBackground() == null)
            return;

        g2.setColor(getConfig().getColorBackground());
        g2.fill(g2.getClip());
    }

    /**
     * 
     * @param g2
     */
    protected void paintCurrentAngle(Graphics2D g2) {
        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorAngleLine());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveForeground"));

        double angle = getComponent().getModel().getRadianAngle()
                + getComponent().getReferenceAngleShift();

        g2.drawLine((int) this.centerX, (int) this.centerY,
                rotateRefPointX(angle), rotateRefPointY(angle));
    }

    /**
     * 
     * @param g2
     */
    protected void paintReferenceAngle(Graphics2D g2) {
        if (!getComponent().isEnabled())
            return;

        if (this.referenceLine == null)
            return;

        g2.setColor(getConfig().getColorReference());
        g2.draw(this.referenceLine);
    }

    /**
     * 
     * @param g2
     */
    protected void paintCircleForeground(Graphics2D g2) {
        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorBorder());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveForeground"));

        g2.draw(this.circle);
    }

    /**
     * 
     * @param g2
     */
    protected void paintCircleBackground(Graphics2D g2) {
        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorCircle());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveBackground"));

        g2.fill(this.circle);
    }

    /**
     * 
     * @param g2
     */
    protected void paintAngleArea(Graphics2D g2) {
        if (!getComponent().isEnabled())
            return;
        if (!getComponent().isShowAngleArea())
            return;

        g2.setColor(getConfig().getColorAngleArea());

        int angleStart = (int) (Math.toDegrees(getComponent()
                .getReferenceAngleShift()));
        if (getComponent().isClockwiseAngle()) {
            angleStart = -angleStart;
        }
        angleStart += 90;

        int angleEnd = (int) (Math.toDegrees(getComponent().getModel()
                .getRadianAngle()));
        if (getComponent().isClockwiseAngle()) {
            angleEnd = -angleEnd;
        }

        Arc2D arc = new Arc2D.Double(getConfig().getMargin().left, getConfig()
                .getMargin().top, getConfig().getCircleRadius() * 2,
                getConfig().getCircleRadius() * 2, angleStart, angleEnd,
                Arc2D.PIE);

        g2.fill(arc);
    }

    /**
     * 
     * @param g2
     */
    protected void paintCenter(Graphics2D g2) {
        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorBorder());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveForeground"));

        g2.fill(this.centerSquare);
    }

    /**
     * 
     * @param g2
     */
    protected void paintPrimaryTick(Graphics2D g2) {
        if (getConfig().getPrimaryStepTick() <= 0)
            return;

        if (getConfig().getPrimaryStepTickSize() <= 0)
            return;

        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorBorder());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveForeground"));

        paintTick(g2, getConfig().getPrimaryStepTickSize(), getConfig()
                .getPrimaryStepTick());
    }

    /**
     * 
     * @param g2
     */
    protected void paintSecondaryTick(Graphics2D g2) {
        if (getConfig().getSecondStepTick() <= 0)
            return;

        if (getConfig().getSecondStepTickSize() <= 0)
            return;

        if (getComponent().isEnabled())
            g2.setColor(getConfig().getColorBorder());
        else
            g2.setColor(UIManager.getColor("TextField.inactiveForeground"));

        paintTick(g2, getConfig().getSecondStepTickSize(), getConfig()
                .getSecondStepTick());
    }

    /**
     * 
     * @param g2
     * @param tickSize
     * @param tickStep
     */
    protected void paintTick(Graphics2D g2, int tickSize, float tickStep) {
        double startX = this.centerX;
        double startY = getConfig().getMargin().top;

        double endX = this.centerX;
        double endY = getConfig().getMargin().top + tickSize;

        for (float i = 0; i < 360; i += tickStep) {
            int newStartX = rotatePointX(startX, startY, Math.toRadians(i));
            int newStartY = rotatePointY(startX, startY, Math.toRadians(i));

            int newEndX = rotatePointX(endX, endY, Math.toRadians(i));
            int newEndY = rotatePointY(endX, endY, Math.toRadians(i));

            g2.drawLine(newStartX, newStartY, newEndX, newEndY);
        }
    }

    // ---- PRIVATE METHODS --------------------------------------------------

    /**
     * 
     * @param pointX
     * @param pointY
     * @param theta
     * @return
     */
    private int rotatePointX(double pointX, double pointY, double theta) {
        double angle = theta;

        if (getComponent().isClockwiseAngle()) {
            angle = Math.PI * 2 - theta;
        }

        return (int) Math.round(rotatePointX(this.centerX, this.centerY,
                pointX, pointY, angle));
    }

    /**
     * 
     * @param pointX
     * @param pointY
     * @param theta
     * @return
     */
    private int rotatePointY(double pointX, double pointY, double theta) {
        double angle = theta;

        if (getComponent().isClockwiseAngle()) {
            angle = Math.PI * 2 - theta;
        }

        return (int) Math.round(rotatePointY(this.centerX, this.centerY,
                pointX, pointY, angle));
    }

    /**
     * 
     * @param theta
     * @return
     */
    private int rotateRefPointX(double theta) {
        return rotatePointX(this.refX, this.refY, theta);
    }

    /**
     * 
     * @param theta
     * @return
     */
    private int rotateRefPointY(double theta) {
        return rotatePointY(this.refX, this.refY, theta);
    }

    /**
     * 
     * @param centerX
     * @param centerY
     * @param pointX
     * @param pointY
     * @param theta
     * @return
     */
    private static double rotatePointX(double centerX, double centerY,
            double pointX, double pointY, double theta) {
        double lenX = pointX - centerX;
        double lenY = pointY - centerY;

        return centerX + lenX * Math.cos(theta) + lenY * Math.sin(theta);
    }

    private static double rotatePointY(double centerX, double centerY,
            double pointX, double pointY, double theta) {
        double lenX = pointX - centerX;
        double lenY = pointY - centerY;

        return centerY - lenX * Math.sin(theta) + lenY * Math.cos(theta);
    }
}