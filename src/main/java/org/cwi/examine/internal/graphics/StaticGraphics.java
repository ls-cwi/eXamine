package org.cwi.examine.internal.graphics;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import org.cwi.examine.internal.graphics.draw.PickingGraphics2D;
import org.cwi.examine.internal.graphics.draw.Snippet;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

import org.cwi.examine.internal.Option;

/**
 * Context that provides all graphics related
 * methods (when they are statically imported).
 */
public class StaticGraphics {
    // Draw manager and XPApplet instances to delegate to.
    protected static DrawManager dm;
    protected static Application application;
    
    // Draw plane/volume bounds, null if not applicable.
    protected static PVector viewBoundsTopLeft = null;
    protected static PVector viewBoundsDimension = null;
    public static int fascv = -1;
    

    
    /**
     * Transition function for float value,
     * should only be used in a snippet.
     */
    public static float t(float target) {
        float result;
        
        if(dm.transitioning && dm.snippetValues != null) {
            result = (float) dm.snippetValues.transition(dm.ti, target);
            dm.ti++;
        } else {
            result = target;
        }
        
        return result;
    }
    
    /**
     * Transition function for double value,
     * should only be used in a snippet.
     */
    public static double t(double target) {
        double result;
        
        if(dm.transitioning && dm.snippetValues != null) {
            result = dm.snippetValues.transition(dm.ti, target);
            dm.ti++;
        } else {
            result = target;
        }
        
        return result;
    }
    
    public static PVector t(PVector v) {
        return new PVector(t(v.x), t(v.y));
    }

    /**
     * Transition function for float value array,
     * should only be used in a snippet.
     */
    public static float[] t(float[] targets) {
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
    public static double[] t(double[] targets) {
        double[] result = new double[targets.length];
        
        for(int i = 0; i < targets.length; i++) {
            result[i] = t(targets[i]);
        }
        
        return result;
    }
    
    /**
     * Draw snippet.
     */
    public static void snippet(Snippet snippet) {
        // Screen render.
        dm.snippet(snippet);
    }
    
    /**
     * Draw multiple snippets (in order).
     */
    public static <E extends Snippet> void snippets(Collection<E> snippets) {
        for(Snippet s: snippets) {
            snippet(s);
        }
    }
    
    /**
     * Draw multiple snippets (in order).
     */
    public static <E extends Snippet> void snippets(E... snippets) {
        for(Snippet s: snippets) {
            snippet(s);
        }
    }
    
    /**
     * Any coordinate or color changes will be transitioned
     * to gradually for a snippet.
     */
    public static void transition() {
        dm.transitioning = true;
    }
    
    // Any coordinate or color changes will be instant.
    public static void noTransition() {
        dm.transitioning = false;
    }
    
    // Any drawn geometry will be picked.
    public static void picking() {
        if(dm.pg instanceof PickingGraphics2D) {
            ((PickingGraphics2D) dm.pg).picking();
        }
    }
    
    // No drawn geometry will be picked.
    public static void noPicking() {
        if(dm.pg instanceof PickingGraphics2D) {
            ((PickingGraphics2D) dm.pg).noPicking();
        }
    }
    
    // Get hovered snippet.
    public static Snippet hovered() {
        return dm.hovered;
    }
    
    // Set draw plane/volume bounds.
    public static void viewBounds(PVector topLeft, PVector dimensions) {
        viewBoundsTopLeft = topLeft.get();
        viewBoundsDimension = dimensions.get();
    }
    
    // Do not use draw place/volume bounds.
    public static void noViewBounds() {
        viewBoundsTopLeft = null;
        viewBoundsDimension = null;
    }
    
    // --- Begin interpolated graphics methods. ---
    
    public static void draw(Shape shape) {
        dm.pg.draw(shape);
    }
    
    public static void fill(Shape shape) {
        dm.pg.fill(shape);
    }
    
    public static void drawLine(PVector origin, PVector target, String bondtype) {
        Path2D.Double line = new Path2D.Double();
        //Single Bonds
  	boolean scel =  Option.getScel(); 
  	
  	double x1p = 0;
	double x2p = 0;
	double y1p = 0;
	double y2p = 0;	 
	double L = 0;  
	int offsetPixels = 0;

	    
        if(scel == false )
        	strokeWeight(1);
        
          if(scel == true )
       		 strokeWeight(2);
        
        
        if( bondtype.contains("single")    ||    bondtype.contains("aromat_a")  ||    bondtype.contains("aromat_b")){
        line.moveTo(t(origin.x), t(origin.y));
        line.lineTo(t(target.x), t(target.y));
        color(Color.BLACK);
        dm.pg.draw(line);  
        
    
        
             
        }
        
      
            if( bondtype.contains("aromat_a")){
            
            	 L = Math.sqrt((origin.x-target.x)*(origin.x-target.x)+(origin.y-target.y)*(origin.y-target.y));
		offsetPixels =  -53;
            	x1p = origin.x - offsetPixels * ( (target.y-origin.y) / L);
		x2p = target.x - offsetPixels * ( (target.y-origin.y) / L);
	 	y1p = origin.y - offsetPixels * ( (origin.x-target.x) / L );
	 	y2p = target.y  - offsetPixels * ( (origin.x-target.x) / L );
	 	
	 	/*line.moveTo(t(x1p), t(y1p));
       		line.lineTo(t(x2p), t(y2p));
       		dm.pg.draw(line); 	*/
	 	
	 	double cx = (x1p + x2p) / 2;
	 	double cy = (y1p + y2p) / 2;
         if(scel == true )
	   strokeWeight(2);
            	  
            	   //     color(Color.LIGHT_GRAY);
            	    Shape circle = new Ellipse2D.Double(cx-35, cy-35, 70, 70);
       		    dm.pg.draw(circle);     
        
        	}
        	
        	   if( bondtype.contains("aromat_b")){
            
            	 L = Math.sqrt((origin.x-target.x)*(origin.x-target.x)+(origin.y-target.y)*(origin.y-target.y));
		offsetPixels =  53;
            	x1p = origin.x - offsetPixels * ( (target.y-origin.y) / L);
		x2p = target.x - offsetPixels * ( (target.y-origin.y) / L);
	 	y1p = origin.y - offsetPixels * ( (origin.x-target.x) / L );
	 	y2p = target.y  - offsetPixels * ( (origin.x-target.x) / L );
	 	
	 	/*line.moveTo(t(x1p), t(y1p));
       		line.lineTo(t(x2p), t(y2p));
       		dm.pg.draw(line); 	*/
	 	
	 	double cx = (x1p + x2p) / 2;
	 	double cy = (y1p + y2p) / 2;
	 	if(scel == true )
	   strokeWeight(2);
	 	
            	// color(Color.LIGHT_GRAY);
            	Shape circle = new Ellipse2D.Double(cx-35, cy-35, 70, 70);
       		 dm.pg.draw(circle);     
        
        	}
        
        
        //Double Bonds
        
        if( bondtype.contains("double")){
        
        
         if(scel == true )
		 strokeWeight(2);
        /* 
        line.moveTo(t(origin.x), t(origin.y));
        line.lineTo(t(target.x), t(target.y));
        color(Color.BLACK);
        dm.pg.draw(line);    */
       
        
        //var x1 = ..., x2 = ..., y1 = ..., y2 = ... // The original line
	 L = Math.sqrt((origin.x-target.x)*(origin.x-target.x)+(origin.y-target.y)*(origin.y-target.y));



	 offsetPixels = 3;

	// This is the second line
	
	
	 
	  if(scel == true )
	   strokeWeight(2);
	    //color(Color.LIGHT_GRAY);}
	    
	 x1p = origin.x + offsetPixels * ( (target.y-origin.y) / L);
	 x2p = target.x + offsetPixels * ( (target.y-origin.y) / L);
	 y1p = origin.y + offsetPixels * ( (origin.x-target.x) / L );
	 y2p = target.y  + offsetPixels * ( (origin.x-target.x) / L );	
	line.moveTo(t(x1p), t(y1p));
        line.lineTo(t(x2p), t(y2p));
        dm.pg.draw(line); 
        
        
        
	 x1p = origin.x - offsetPixels * ( (target.y-origin.y) / L);
	 x2p = target.x - offsetPixels * ( (target.y-origin.y) / L);
	 y1p = origin.y - offsetPixels * ( (origin.x-target.x) / L );
	 y2p = target.y  - offsetPixels * ( (origin.x-target.x) / L );
	
	
		//setStroke(dashed);
	    //strokeWeight(2);
	line.moveTo(t(x1p), t(y1p));
        line.lineTo(t(x2p), t(y2p));
        dm.pg.draw(line); 
      /* */
	   
       /*  */
        
        
        
        
        /*
        if( origin.y == target.y ){
        line.moveTo(t(origin.x), t(origin.y+g));
        line.lineTo(t(target.x), t(target.y+g));
        dm.pg.draw(line);
        
        
         line.moveTo(t(origin.x), t(origin.y-g));
        line.lineTo(t(target.x), t(target.y-g));
        dm.pg.draw(line);}
        
        
         else if( origin.x == target.x ){
        line.moveTo(t(origin.x+g), t(origin.y));
        line.lineTo(t(target.x+g), t(target.y));
        dm.pg.draw(line);
        
        
         line.moveTo(t(origin.x-g), t(origin.y));
        line.lineTo(t(target.x-g), t(target.y));
        dm.pg.draw(line);}
        
        else if(  Math.abs(origin.y-target.y) < Math.abs(origin.x-target.x) ) {
        line.moveTo(t(origin.x), t(origin.y+g));
        line.lineTo(t(target.x), t(target.y+g));
        dm.pg.draw(line);
        
        
         line.moveTo(t(origin.x), t(origin.y-g));
        line.lineTo(t(target.x), t(target.y-g));
        dm.pg.draw(line);}
        
         else if(  Math.abs(origin.y-target.y) > Math.abs(origin.x-target.x) ) {
        line.moveTo(t(origin.x+g), t(origin.y));
        line.lineTo(t(target.x+g), t(target.y));
        dm.pg.draw(line);
        
        
         line.moveTo(t(origin.x-g), t(origin.y));
        line.lineTo(t(target.x-g), t(target.y));
        dm.pg.draw(line);}
        
     
   	 */	}
       
         fascv = fascv *(-1);  
        
       
    }
    
    public static void drawLineString(LineString lineString) {
        Coordinate[] cs = lineString.getCoordinates();
        
        Path2D.Double line = new Path2D.Double();
        line.moveTo(t(cs[0].x), t(cs[0].y));
        for(int i = 1; i < cs.length; i++) {
            line.lineTo(t(cs[i].x), t(cs[i].y));
            System.out.println("here");
        }
      //  dm.pg.draw(line);
    }
    
    public static void drawCurve(PVector origin, PVector control, PVector target) {
        Path2D.Double curve = new Path2D.Double();
        curve.moveTo(t(origin.x), t(origin.y));
        double midX = t(control.x);
        double midY = t(control.y);
        curve.curveTo(midX, midY, midX, midY, t(target.x), t(target.y));
       // dm.pg.draw(curve);
    }
    
    public static void drawRect(double a, double b, double c, double d) {
        dm.pg.draw(new Rectangle2D.Double(t(a), t(b), t(c), t(d)));
    }
    
    public static void fillRect(double a, double b, double c, double d) {
        dm.pg.fill(new Rectangle2D.Double(t(a), t(b), t(c), t(d)));
    }

    public static void drawEllipse(double a, double b, double c, double d) {
        dm.pg.draw(new Ellipse2D.Double(t(a - c), t(b - d), 2 * t(c), 2 * t(d)));
    }
    
    public static void fillEllipse(double a, double b, double c, double d) {
        dm.pg.fill(new Ellipse2D.Double(t(a - c), t(b - d), 2 * t(c), 2 * t(d)));
    }
    
    public static void circleArc(PVector p1, PVector p2, PVector p3) {
        
        dm.pg.draw(getArc(t(p1), t(p2), t(p3)));  // get link
    }
    
    public static Shape getArc(PVector p1, PVector p2, PVector p3) {
        Shape path;
        
        PVector v21 = PVector.sub(p2, p1);
        double d21 = v21.dot(v21);
        PVector v31 = PVector.sub(p3, p1);
        double d31 = v31.dot(v31);
        double a4 = 2 * v21.crossZ(v31);
        //double a123 = Math.abs(PVector.angle(p1, p2, p3));
        
        double d13 = PVector.distance(p1, p3);
        boolean wellFormed = PVector.distance(p1, p2) < d13 &&
                             PVector.distance(p2, p3) < d13;
        
        if(wellFormed /*a123 > 0.001 * Math.PI*/ && Math.abs(a4) > 0.001) {
            PVector center = new PVector(p1.x + (v31.y*d21-v21.y*d31)/a4,
                                         p1.y + (v21.x*d31-v31.x*d21)/a4);
            double radius =
                    Math.sqrt(d21*d31*(
                            Math.pow(p3.x-p2.x,2) +
                            Math.pow(p3.y-p2.y,2))) /
                    Math.abs(a4);
            
            Arc2D arc = new Arc2D.Double(Arc2D.OPEN);
            arc.setFrame(center.x - radius, center.y - radius,
                         2 * radius, 2 * radius);
            
            boolean cross = PVector.sub(p2, p1).crossZ(PVector.sub(p3, p2)) < 0;
            if(cross) arc.setAngles(p1.x, p1.y, p3.x, p3.y);
            else {
                arc.setAngles(p3.x, p3.y, p1.x, p1.y);
                double extent = arc.getAngleExtent();
                arc.setAngleStart(arc.getAngleStart() + extent);
                arc.setAngleExtent(-extent);
            }
            
            path = arc;
        }
        // There is no circle, so take a straight line between p0 and p1.
        else {
            Path2D.Double line = new Path2D.Double();
            line.moveTo(p1.x, p1.y);
            line.lineTo(p3.x, p3.y);
            
            path = line;
        }
        
        return path;
    }

    public static void pushTransform() {
        dm.transformStack.add(dm.pg.getTransform());
    }

    public static void popTransform() {
        dm.pg.setTransform(dm.transformStack.remove(dm.transformStack.size() - 1));
    }

    public static void translate(double tx, double ty) {
        dm.pg.translate(t(tx), t(ty));
    }
    
    public static void translate(PVector t) {
        translate(t.x, t.y);
    }
    
    public static void scale(double sx, double sy) {
        dm.pg.scale(t(sx), t(sy));
    }
    
    public static void scale(PVector s) {
        dm.pg.scale(s.x, s.y);
    }

    public static void rotate(double angle) {
        dm.pg.rotate(t(angle));
    }

    public static void strokeWeight(double weight) {
        dm.pg.setStroke(new BasicStroke((float) t(weight), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }
    
    public static void stroke(Stroke stroke) {
        dm.pg.setStroke(stroke);
    }

    public static void drawRect(double a, double b, double c, double d, double r) {
        double tr = t(r);
        dm.pg.draw(new RoundRectangle2D.Double(t(a), t(b), t(c), t(d), tr, tr));
    }
    
    public static void fillRect(double a, double b, double c, double d, double r) {
        double tr = t(r);
        dm.pg.fill(new RoundRectangle2D.Double(t(a), t(b), t(c), t(d), tr, tr));
    }

    public static double textAscent() {
        return dm.pg.getFontMetrics().getAscent();
    }

    public static double textDescent() {
        return dm.pg.getFontMetrics().getDescent();
    }
    
    public static double textHeight() {
        return textAscent() + textDescent();
    }
    
    // Approximate middle of text height.
    public static double textMiddle() {
        return (3f * textAscent() + textDescent()) / 4f;
    }

    public static void textFont(Font which) {
        dm.pg.setFont(which);
    }

    public static double textWidth(String str) {
        return (double) dm.pg.getFontMetrics().getStringBounds(str, dm.pg).getWidth();
    }
    
    // Place text with top left at the origin of the current coordinate space.
    public static void text(String str) {
        text(str, 0, textAscent());
    }

    public static void text(String str, double x, double y) {
        dm.pg.drawString(str, (float) t(x), (float) t(y));
    }

    public static void pushStyle() {
        dm.styleStack.add(getStyle());
    }

    public static void popStyle() {
        Style style = dm.styleStack.remove(dm.styleStack.size() - 1);
        style(style);
    }
    
    public static void style(Style style) {
        dm.pg.setPaint(style.paint);
        dm.pg.setStroke(style.stroke);
    }
    
    public static Style getStyle() {
        return new Style(dm.pg.getPaint(), dm.pg.getStroke());
    }
    
    private static float alphaPresence(double alpha) {
        double presence = dm.snippetValues == null ? 1 : dm.snippetValues.presence; 
        return (float) java.lang.Math.max(0, java.lang.Math.min(1, presence));
    }
    
    public static void color(Color color) {
        float[] tComponents = t(color.getComponents(null));
        dm.pg.setColor(new Color(color.getColorSpace(),
                                 tComponents,
                                 (float) alphaPresence(tComponents[tComponents.length - 1])));
    }
    
    public static void color(Color color, double alpha) {
        float[] tComponents = t(color.getComponents(null));
        dm.pg.setColor(new Color(color.getColorSpace(),
                                 tComponents,
                                 (alphaPresence(t(alpha) * tComponents[tComponents.length - 1]))));
    }
    
    public static Color getColor() {
        return dm.pg.getColor();
    }
    
    public static MouseEvent mouseEvent() {
        return application.mouseEvent;
    }
    
    public static int mouseX() {
        return application.mouseX;
    }
    
    public static int mouseY() {
        return application.mouseY;
    }
    
    // Returns the mouse coordinates according to the current coordinate space (2D).
    public static PVector mouseLocal() {
        PVector mouseLocal = PVector.v();
        
        PVector global = PVector.v(mouseX(), mouseY());
        
        AffineTransform globalToLocal = dm.pg.getTransform();
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
    public static double applicationWidth() {
        return application.getWidth();
    }
    
    // Application height.
    public static double applicationHeight() {
        return application.getHeight();
    }
    
    // Applet dimensions.
    public static PVector appletDimensions() {
        return new PVector(applicationWidth(), applicationHeight());
    }

    // Sketch area width.
    public static double sketchWidth() {
        return applicationWidth();
    }

    // Sketch area height.
    public static double sketchHeight() {
        return applicationHeight();
    }
    
    // Sketch area dimensions.
    public static PVector sketchDimensions() {
        return PVector.v(sketchWidth(), sketchHeight());
    }

    public static void cursor(Cursor cursor) {
        application.setCursor(cursor);
    }
    
}
