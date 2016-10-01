package org.cwi.examine.internal.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

public class Style {
    public final Paint paint;
    public final Stroke stroke;
    
    public Style() {
        this.paint = Color.BLACK;
        this.stroke = new BasicStroke();
    }
    
    public Style(Paint paint, Stroke stroke) {
        this.paint = paint;
        this.stroke = stroke;
    }
}
