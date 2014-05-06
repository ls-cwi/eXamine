package org.cytoscape.examine.internal.graphics.draw;

import java.awt.Color;
import static java.lang.Math.*;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 *
 * @author kdinkla
 */
public class PickingGraphics2D extends Graphics2D {
    
    private BufferedImage parentImage;
    private Graphics2D parent;
    
    // ID of drawn representation.
    public int snippetId;
    
    // ID that is used to draw.
    public int activeId;
    
    // ID of null snippet (none).
    public static final int nullId = -16777216;
    
    // First ID.
    public static final int firstId = nullId + 1;
    
    public PickingGraphics2D(Graphics2D parent, int width, int height) {
        this.parentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.parent = this.parentImage.createGraphics();
        
        this.parent.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        this.parent.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        this.parent.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                            RenderingHints.VALUE_STROKE_PURE);
        this.parent.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_SPEED);
        this.parent.setRenderingHint(RenderingHints.KEY_DITHERING,
                            RenderingHints.VALUE_DITHER_DISABLE);
        this.parent.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                            RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }
    
    /**
     * Reset before new draw.
     */
    public void preDraw() {
        snippetId = -16777215;
        parent.setBackground(new Color(nullId));
        parent.clearRect(0, 0, parentImage.getWidth(), parentImage.getHeight());
        parent.setTransform(new AffineTransform());
        noPicking();
    }

    /**
     * Get the snippet identification at a specific pixel coordinate.
     */
    public int snippetId(int x, int y) {
        int result = nullId;
        
        if(0 <= x && x < parentImage.getWidth() &&
           0 <= y && y < parentImage.getHeight()) {
            result = parentImage.getRGB(x, y);
        }
        
        return result;
    }
    
    /**
     * Get the snippet identification that is the closest
     * to the given pixel coordinate. Search is bounded
     * by cursor radius parameter.
     */
    public int closestSnippetId(int x, int y) {
        //parent.loadPixels();
        
        // Limit radius by window bounds.
        int radius = min(Parameters.cursorRadius.get(),
                         min(min(parentImage.getWidth() - x, x),
                             min(parentImage.getHeight() - y, y)));
        
        // Pick pixels of every square of increasing radius r.
        for(int r = 0; r < radius; r++) {
            // For -r, 0 and r (left and right).
            for(int dx = -1; dx <= 1; dx += 1) {
                // For -r, 0 and r (top and bottom). 
                for(int dy = -1; dy <= 1; dy += 1) {
                    //int ac = (y + dy * r) * parentImage.getWidth() + (x + dx * r);
                    int pv = parentImage.getRGB(x + dx * r, y + dy * r); //parent.pixels[ac];
                    
                    if(pv != nullId) {
                        //System.out.println("pick id: " + pv);
                        //System.out.println("radius: " + r);
                        //System.out.println("ac: " + ac);
                        //System.out.println("pos: (" + (x + dx * r) + "," + (y + dy * r) + ")");
                        
                        return pv;
                    }
                }
            }
        }
        
        return nullId;
    }
    
    public void picking() {
        activeId = snippetId;
        
        parent.setPaint(new Color(activeId));
        /*if(parent.fill) {
            parent.fill(activeId);
        }
        if(parent.stroke) {
            parent.stroke(activeId);
        }*/
    }
    
    public void noPicking() {
        activeId = nullId;
        
        parent.setPaint(new Color(activeId));
        /*if(parent.fill) {
            parent.fill(activeId);
        }
        if(parent.stroke) {
            parent.stroke(activeId);
        }*/
    }

    @Override
    public void draw3DRect(int i, int i1, int i2, int i3, boolean bln) {
        parent.draw3DRect(i, i1, i2, i3, bln);
    }

    @Override
    public void fill3DRect(int i, int i1, int i2, int i3, boolean bln) {
        parent.fill3DRect(i, i1, i2, i3, bln);
    }

    @Override
    public void draw(Shape shape) {
        parent.draw(shape);
    }

    @Override
    public boolean drawImage(Image image, AffineTransform at, ImageObserver io) {
        return parent.drawImage(image, at, io);
    }

    @Override
    public void drawImage(BufferedImage bi, BufferedImageOp bio, int i, int i1) {
        parent.drawImage(bi, bio, i, i1);
    }

    @Override
    public void drawRenderedImage(RenderedImage ri, AffineTransform at) {
        parent.drawRenderedImage(ri, at);
    }

    @Override
    public void drawRenderableImage(RenderableImage ri, AffineTransform at) {
        parent.drawRenderableImage(ri, at);
    }

    @Override
    public void drawString(String string, int x, int y) {
        FontMetrics fm = parent.getFontMetrics();
        Rectangle2D bounds = parent.getFontMetrics().getStringBounds(string, parent);
        parent.fill(new Rectangle2D.Double(x, y - fm.getAscent(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void drawString(String string, float x, float y) {
        FontMetrics fm = parent.getFontMetrics();
        Rectangle2D bounds = parent.getFontMetrics().getStringBounds(string, parent);
        parent.fill(new Rectangle2D.Double(x, y - fm.getAscent(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void drawString(AttributedCharacterIterator aci, int i, int i1) {
        //parent.drawString(aci, i, i1);
    }

    @Override
    public void drawString(AttributedCharacterIterator aci, float f, float f1) {
        //parent.drawString(aci, f, f1);
    }

    @Override
    public void drawGlyphVector(GlyphVector gv, float f, float f1) {
        //parent.drawGlyphVector(gv, f, f1);
    }

    @Override
    public void fill(Shape shape) {
        parent.fill(shape);
    }

    @Override
    public boolean hit(Rectangle rctngl, Shape shape, boolean bln) {
        return parent.hit(rctngl, shape, bln);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return parent.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite cmpst) {
    }

    @Override
    public void setPaint(Paint paint) {
    }

    @Override
    public void setStroke(Stroke stroke) {
        parent.setStroke(stroke);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key key, Object o) {
        parent.setRenderingHint(key, o);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key key) {
        return parent.getRenderingHint(key);
    }

    @Override
    public void setRenderingHints(Map<?, ?> map) {
        parent.setRenderingHints(map);
    }

    @Override
    public void addRenderingHints(Map<?, ?> map) {
        parent.addRenderingHints(map);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return parent.getRenderingHints();
    }

    @Override
    public void translate(int i, int i1) {
        parent.translate(i, i1);
    }

    @Override
    public void translate(double d, double d1) {
        parent.translate(d, d1);
    }

    @Override
    public void rotate(double d) {
        parent.rotate(d);
    }

    @Override
    public void rotate(double d, double d1, double d2) {
        parent.rotate(d, d1, d2);
    }

    @Override
    public void scale(double d, double d1) {
        parent.scale(d, d1);
    }

    @Override
    public void shear(double d, double d1) {
        parent.shear(d, d1);
    }

    @Override
    public void transform(AffineTransform at) {
        parent.transform(at);
    }

    @Override
    public void setTransform(AffineTransform at) {
        parent.setTransform(at);
    }

    @Override
    public AffineTransform getTransform() {
        return parent.getTransform();
    }

    @Override
    public Paint getPaint() {
        return parent.getPaint();
    }

    @Override
    public Composite getComposite() {
        return parent.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        parent.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return parent.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return parent.getStroke();
    }

    @Override
    public void clip(Shape shape) {
        parent.clip(shape);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return parent.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return parent.create();
    }

    @Override
    public Graphics create(int i, int i1, int i2, int i3) {
        return parent.create(i, i1, i2, i3);
    }

    @Override
    public Color getColor() {
        return parent.getColor();
    }

    @Override
    public void setColor(Color color) {
    }

    @Override
    public void setPaintMode() {

    }

    @Override
    public void setXORMode(Color color) {

    }

    @Override
    public Font getFont() {
        return parent.getFont();
    }

    @Override
    public void setFont(Font font) {
        parent.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics() {
        return parent.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        return parent.getFontMetrics(font);
    }

    @Override
    public Rectangle getClipBounds() {
        return parent.getClipBounds();
    }

    @Override
    public void clipRect(int i, int i1, int i2, int i3) {
        parent.clipRect(i, i1, i2, i3);
    }

    @Override
    public void setClip(int i, int i1, int i2, int i3) {
        parent.setClip(i, i1, i2, i3);
    }

    @Override
    public Shape getClip() {
        return parent.getClip();
    }

    @Override
    public void setClip(Shape shape) {
        parent.setClip(shape);
    }

    @Override
    public void copyArea(int i, int i1, int i2, int i3, int i4, int i5) {
        parent.copyArea(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void drawLine(int i, int i1, int i2, int i3) {
        parent.drawLine(i, i1, i2, i3);
    }

    @Override
    public void fillRect(int i, int i1, int i2, int i3) {
        parent.fillRect(i, i1, i2, i3);
    }

    @Override
    public void drawRect(int i, int i1, int i2, int i3) {
        parent.drawRect(i, i1, i2, i3);
    }

    @Override
    public void clearRect(int i, int i1, int i2, int i3) {
        parent.clearRect(i, i1, i2, i3);
    }

    @Override
    public void drawRoundRect(int i, int i1, int i2, int i3, int i4, int i5) {
        parent.drawRoundRect(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void fillRoundRect(int i, int i1, int i2, int i3, int i4, int i5) {
        parent.fillRoundRect(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void drawOval(int i, int i1, int i2, int i3) {
        parent.drawOval(i, i1, i2, i3);
    }

    @Override
    public void fillOval(int i, int i1, int i2, int i3) {
        parent.fillOval(i, i1, i2, i3);
    }

    @Override
    public void drawArc(int i, int i1, int i2, int i3, int i4, int i5) {
        parent.drawArc(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void fillArc(int i, int i1, int i2, int i3, int i4, int i5) {
        parent.fillArc(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void drawPolyline(int[] ints, int[] ints1, int i) {
        parent.drawPolyline(ints, ints1, i);
    }

    @Override
    public void drawPolygon(int[] ints, int[] ints1, int i) {
        parent.drawPolygon(ints, ints1, i);
    }

    @Override
    public void drawPolygon(Polygon plgn) {
        parent.drawPolygon(plgn);
    }

    @Override
    public void fillPolygon(int[] ints, int[] ints1, int i) {
        parent.fillPolygon(ints, ints1, i);
    }

    @Override
    public void fillPolygon(Polygon plgn) {
        parent.fillPolygon(plgn);
    }

    @Override
    public void drawChars(char[] chars, int i, int i1, int i2, int i3) {
        parent.drawChars(chars, i, i1, i2, i3);
    }

    @Override
    public void drawBytes(byte[] bytes, int i, int i1, int i2, int i3) {
        parent.drawBytes(bytes, i, i1, i2, i3);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, ImageObserver io) {
        return parent.drawImage(image, i, i1, io);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, int i2, int i3, ImageObserver io) {
        return parent.drawImage(image, i, i1, i2, i3, io);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, Color color, ImageObserver io) {
        return parent.drawImage(image, i, i1, color, io);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, int i2, int i3, Color color, ImageObserver io) {
        return parent.drawImage(image, i, i1, i2, i3, color, io);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, ImageObserver io) {
        return parent.drawImage(image, i, i1, i2, i3, i4, i5, i6, i7, io);
    }

    @Override
    public boolean drawImage(Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Color color, ImageObserver io) {
        return parent.drawImage(image, i, i1, i2, i3, i4, i5, i6, i7, color, io);
    }

    @Override
    public void dispose() {
        parent.dispose();
    }

    @Override
    public String toString() {
        return parent.toString();
    }

    @Override
    public Rectangle getClipRect() {
        return parent.getClipRect();
    }

    @Override
    public boolean hitClip(int i, int i1, int i2, int i3) {
        return parent.hitClip(i, i1, i2, i3);
    }

    @Override
    public Rectangle getClipBounds(Rectangle rctngl) {
        return parent.getClipBounds(rctngl);
    }
    
}
