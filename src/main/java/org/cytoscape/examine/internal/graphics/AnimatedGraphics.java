package org.cytoscape.examine.internal.graphics;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import org.cytoscape.examine.internal.graphics.draw.Snippet;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

/**
 * Context that provides all graphics related
 * methods (when they are statically imported).
 */
public class AnimatedGraphics {

    private final Application application;
    private final DrawManager drawManager = new DrawManager();

    AnimatedGraphics(Application application) {
        this.application = application;
    }

    DrawManager getDrawManager() {
        return drawManager;
    }
    
    /**
     * Transition function for float value,
     * should only be used in a snippet.
     */
    public float t(float target) {
        float result;
        
        if(drawManager.transitioning && drawManager.snippetValues != null) {
            result = (float) drawManager.snippetValues.transition(drawManager.ti, target);
            drawManager.ti++;
        } else {
            result = target;
        }
        
        return result;
    }
    
    /**
     * Transition function for double value,
     * should only be used in a snippet.
     */
    public double t(double target) {
        double result;
        
        if(drawManager.transitioning && drawManager.snippetValues != null) {
            result = drawManager.snippetValues.transition(drawManager.ti, target);
            drawManager.ti++;
        } else {
            result = target;
        }
        
        return result;
    }
    
    public PVector t(PVector v) {
        return new PVector(t(v.x), t(v.y));
    }

    /**
     * Transition function for float value array,
     * should only be used in a snippet.
     */
    public float[] t(float[] targets) {
        float[] result = new float[targets.length];
        
        for(int i = 0; i < targets.length; i++) {
            result[i] = t(targets[i]);
        }
        
        return result;
    }

    /**
     * Transition function for double value array,
     * should only be used in a snippet.
     */
    public double[] t(double[] targets) {
        double[] result = new double[targets.length];
        
        for(int i = 0; i < targets.length; i++) {
            result[i] = t(targets[i]);
        }
        
        return result;
    }
    
    /**
     * Draw snippet.
     */
    public void snippet(Snippet snippet) {
        // Screen render.
        drawManager.snippet(snippet, this);
    }
    
    /**
     * Draw multiple snippets (in order).
     */
    public <E extends Snippet> void snippets(Collection<E> snippets) {
        for(Snippet s: snippets) {
            snippet(s);
        }
    }
    
    /**
     * Draw multiple snippets (in order).
     */
    public <E extends Snippet> void snippets(E... snippets) {
        for(Snippet s: snippets) {
            snippet(s);
        }
    }
    
    /**
     * Any coordinate or color changes will be transitioned
     * to gradually for a snippet.
     */
    public void transition() {
        drawManager.transitioning = true;
    }
    
    // Any coordinate or color changes will be instant.
    public void noTransition() {
        drawManager.transitioning = false;
    }
    
    // Any drawn geometry will be picked.
    public void picking() {
        drawManager.picking();
    }
    
    // No drawn geometry will be picked.
    public void noPicking() {
        drawManager.noPicking();
    }
    
    // Get hovered snippet.
    public Snippet hovered() {
        return drawManager.hovered;
    }
    
    // --- Begin interpolated graphics methods. ---
    
    public void draw(Shape shape) {
        drawManager.pg.draw(shape);
    }
    
    public void fill(Shape shape) {
        drawManager.pg.fill(shape);
    }
    
    public void drawLine(PVector origin, PVector target) {
        Path2D.Double line = new Path2D.Double();
        line.moveTo(t(origin.x), t(origin.y));
        line.lineTo(t(target.x), t(target.y));
        drawManager.pg.draw(line);
    }
    
    public void drawLineString(LineString lineString) {
        Coordinate[] cs = lineString.getCoordinates();
        
        Path2D.Double line = new Path2D.Double();
        line.moveTo(t(cs[0].x), t(cs[0].y));
        for(int i = 1; i < cs.length; i++) {
            line.lineTo(t(cs[i].x), t(cs[i].y));
        }
        drawManager.pg.draw(line);
    }
    
    public void drawCurve(PVector origin, PVector control, PVector target) {
        Path2D.Double curve = new Path2D.Double();
        curve.moveTo(t(origin.x), t(origin.y));
        double midX = t(control.x);
        double midY = t(control.y);
        curve.curveTo(midX, midY, midX, midY, t(target.x), t(target.y));
        drawManager.pg.draw(curve);
    }
    
    public void drawRect(double a, double b, double c, double d) {
        drawManager.pg.draw(new Rectangle2D.Double(t(a), t(b), t(c), t(d)));
    }
    
    public void fillRect(double a, double b, double c, double d) {
        drawManager.pg.fill(new Rectangle2D.Double(t(a), t(b), t(c), t(d)));
    }

    public void drawEllipse(double a, double b, double c, double d) {
        drawManager.pg.draw(new Ellipse2D.Double(t(a - c), t(b - d), 2 * t(c), 2 * t(d)));
    }
    
    public void fillEllipse(double a, double b, double c, double d) {
        drawManager.pg.fill(new Ellipse2D.Double(t(a - c), t(b - d), 2 * t(c), 2 * t(d)));
    }
    
    public void circleArc(PVector p1, PVector p2, PVector p3) {
        drawManager.pg.draw(Shapes.getArc(t(p1), t(p2), t(p3)));
    }

    public void pushTransform() {
        drawManager.pushTransform();
    }

    public void popTransform() {
        drawManager.popTransform();
    }

    public void pushStyle() {
        drawManager.pushStyle();
    }

    public void popStyle() {
        drawManager.popStyle();
    }

    public void setStyle(Style style) {
        drawManager.setStyle(style);
    }

    public Style getStyle() {
        return drawManager.getStyle();
    }

    public void translate(double tx, double ty) {
        drawManager.pg.translate(t(tx), t(ty));
    }
    
    public void translate(PVector t) {
        translate(t.x, t.y);
    }
    
    public void scale(double sx, double sy) {
        drawManager.pg.scale(t(sx), t(sy));
    }
    
    public void scale(PVector s) {
        drawManager.pg.scale(s.x, s.y);
    }

    public void rotate(double angle) {
        drawManager.pg.rotate(t(angle));
    }

    public void strokeWeight(double weight) {
        drawManager.pg.setStroke(new BasicStroke((float) t(weight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }
    
    public void stroke(Stroke stroke) {
        drawManager.pg.setStroke(stroke);
    }

    public void drawRect(double a, double b, double c, double d, double r) {
        double tr = t(r);
        drawManager.pg.draw(new RoundRectangle2D.Double(t(a), t(b), t(c), t(d), tr, tr));
    }
    
    public void fillRect(double a, double b, double c, double d, double r) {
        double tr = t(r);
        drawManager.pg.fill(new RoundRectangle2D.Double(t(a), t(b), t(c), t(d), tr, tr));
    }

    public double textAscent() {
        return drawManager.pg.getFontMetrics().getAscent();
    }

    public double textDescent() {
        return drawManager.pg.getFontMetrics().getDescent();
    }
    
    public double textHeight() {
        return textAscent() + textDescent();
    }
    
    // Approximate middle of text height.
    public double textMiddle() {
        return (3f * textAscent() + textDescent()) / 4f;
    }

    public void textFont(Font which) {
        drawManager.pg.setFont(which);
    }

    public double textWidth(String str) {
        return (double) drawManager.pg.getFontMetrics().getStringBounds(str, drawManager.pg).getWidth();
    }
    
    // Place text with top left at the origin of the current coordinate space.
    public void text(String str) {
        text(str, 0, textAscent());
    }

    public void text(String str, double x, double y) {
        drawManager.pg.drawString(str, (float) t(x), (float) t(y));
    }
    
    private float alphaPresence(double alpha) {
        double presence = drawManager.snippetValues == null ? 1 : drawManager.snippetValues.presence; 
        return (float) java.lang.Math.max(0, java.lang.Math.min(1, presence));
    }
    
    public void color(Color color) {
        float[] tComponents = t(color.getComponents(null));
        drawManager.pg.setColor(new Color(color.getColorSpace(),
                                 tComponents,
                                 (float) alphaPresence(tComponents[tComponents.length - 1])));
    }
    
    public void color(Color color, double alpha) {
        float[] tComponents = t(color.getComponents(null));
        drawManager.pg.setColor(new Color(color.getColorSpace(),
                                 tComponents,
                                 (alphaPresence(t(alpha) * tComponents[tComponents.length - 1]))));
    }
    
    public Color getColor() {
        return drawManager.pg.getColor();
    }
    
    public MouseEvent mouseEvent() {
        return application.mouseEvent;
    }
    
    public int mouseX() {
        return application.mouseX;
    }
    
    public int mouseY() {
        return application.mouseY;
    }
    
    // Returns the mouse coordinates according to the current coordinate space (2D).
    public PVector mouseLocal() {
        PVector mouseLocal = PVector.v();
        
        PVector global = PVector.v(mouseX(), mouseY());
        
        AffineTransform globalToLocal = drawManager.pg.getTransform();
        try {
            globalToLocal.invert();
            Point2D tP = globalToLocal.transform(new Point2D.Double(global.x, global.y), null);
            mouseLocal.x = (double) tP.getX();
            mouseLocal.y = (double) tP.getY();
        } catch(NoninvertibleTransformException ex) {
            
        }
        
        return mouseLocal;
    }
    
    // Application width.
    public double applicationWidth() {
        return application.getWidth();
    }
    
    // Application height.
    public double applicationHeight() {
        return application.getHeight();
    }

    // Applet dimensions.
    public PVector applicationDimensions() {
        return new PVector(applicationWidth(), applicationHeight());
    }
    
}
