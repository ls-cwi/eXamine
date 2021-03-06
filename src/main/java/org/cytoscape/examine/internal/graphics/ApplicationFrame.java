package org.cytoscape.examine.internal.graphics;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.cytoscape.examine.internal.graphics.draw.Snippet;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cytoscape.examine.internal.graphics.draw.Constants.FONT;
import static org.cytoscape.examine.internal.graphics.draw.Constants.LABEL_FONT;
import static org.cytoscape.examine.internal.graphics.draw.Constants.NOTE_FONT;

// Graphics application.
@SuppressWarnings("serial") //TODO: We can assume we never serialize this right?
public abstract class ApplicationFrame extends JFrame {

    private AnimatedGraphics animatedGraphics = new AnimatedGraphics();
    private final JPanel rootPanel;

    protected Snippet rootSnippet;
    protected int mouseX, mouseY;
    protected MouseEvent mouseEvent;
    
    public ApplicationFrame() {
        // Fair default size, but maximize.
        setSize(1400, 800);
        setVisible(true);
        
        // Set title to class name by default.
        setTitle(getClass().getSimpleName());
        
        // Graphics setup.
        setup();

        rootPanel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g); // Clears screen?
        
                Graphics2D g2 = (Graphics2D) g;
                animatedGraphics.getDrawManager().defaultGraphics = g2;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                                    RenderingHints.VALUE_STROKE_DEFAULT);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_DITHERING,
                                    RenderingHints.VALUE_DITHER_ENABLE);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                    RenderingHints.VALUE_COLOR_RENDER_QUALITY);

                rootDraw();

                repaint();
            }

            @Override
            public String getToolTipText(MouseEvent event) {
                String text = null;
                
                Snippet hovered = animatedGraphics.hovered();
                if(hovered != null && hovered.toolTipText() != null) {
                    text = hovered.toolTipText();
                }
                
                return text;
            }
            
        };
        rootPanel.setBackground(Color.WHITE);
        ToolTipManager.sharedInstance().registerComponent(rootPanel);
        
        rootPanel.setDoubleBuffered(true);
        setContentPane(rootPanel);
        
        // Root snippet, contains application rootDraw calls.
        rootSnippet = new Snippet() {

            @Override
            public void draw(AnimatedGraphics g) {
                // Font.
                animatedGraphics.textFont(FONT);
                
                // Make room for side pane.
                animatedGraphics.pushTransform();
                
                // Draw (sub-class behavior).
                ApplicationFrame.this.draw(animatedGraphics);
                animatedGraphics.popTransform();
            }
            
        };
        
        // Event listeners.
        rootPanel.addMouseListener(new LocalMouseListener());
        rootPanel.addMouseMotionListener(new LocalMouseMotionListener());
        this.addKeyListener(new LocalKeyListener());
    }
    
    public final void setup() {
        
        // Adapt picking buffer to canvas size.
        updateDimensions();
        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent ce) {
                updateDimensions();
            }

            @Override
            public void componentMoved(ComponentEvent ce) {
            }

            @Override
            public void componentShown(ComponentEvent ce) {
            }

            @Override
            public void componentHidden(ComponentEvent ce) {
            }
            
        });
        
        // Mouse wheel listener.
        addMouseWheelListener(new MouseWheelListener() {
            
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) { 
                // Send event to hovered item.
                if(animatedGraphics.getDrawManager().hovered != null) {
                    animatedGraphics.getDrawManager().hovered.mouseWheel(mwe.getWheelRotation());
                }
            }
        
        });
        
        // Load base and label fonts; load open sans and use Arial as fall-back.
        try {
            InputStream input = ApplicationFrame.class.getResourceAsStream("/font/OpenSans-Regular.ttf");
            Font inputFont = Font.createFont(Font.TRUETYPE_FONT, input);
            
            FONT = inputFont.deriveFont(24f);
            LABEL_FONT = inputFont.deriveFont(14f);
            NOTE_FONT = inputFont.deriveFont(8f);
        } catch(Exception ex) {
            System.out.println("Font load exception: " + ex.getLocalizedMessage());
            
            FONT = new Font("Arial", Font.PLAIN, 18);
            LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
            NOTE_FONT = new Font("Arial", Font.PLAIN, 8);
        }
    }

    private void updateDimensions() {
        animatedGraphics.setCanvasWidth(getWidth());
        animatedGraphics.setCanvasHeight(getHeight());
        animatedGraphics.getDrawManager().updatePickingBuffer(getWidth(), getHeight());
    }

    public final void rootDraw() {
        DrawManager drawManager = animatedGraphics.getDrawManager();

        // Manager global pre rootDraw.
        drawManager.pre();
        
        // Draw to normal graphics.
        drawManager.preScreen();
        animatedGraphics.snippet(rootSnippet);
        drawManager.postScreen(animatedGraphics);
        
        // Draw to picking graphics.
        drawManager.prePicking();
        animatedGraphics.snippet(rootSnippet);
        Snippet hovered = drawManager.updateHoveredSnippet(mouseX, mouseY);
        setCursor(hovered == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Manager global post rootDraw.
        drawManager.post();
    }

    // Input methods.
    private void storeEvent(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
        mouseEvent = me;
    }

    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
            try {
                exportSVG();
            } catch (IOException ex) {
                Logger.getLogger(ApplicationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public void keyPressed() {}

    public void keyReleased() {}
    
    // Draw commands, to be implemented.
    public abstract void draw(AnimatedGraphics graphics);
    
    // Export application paint to SVG file.
    public void exportSVG() throws IOException {
        // Get a DOMImplementation.
        DOMImplementation domImpl =
          GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Paint application to SVG structure.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        rootPanel.paint(svgGenerator);
        
        // Target file via dialog.
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("Export SVG");
        fileChooser.setSelectedFile(new File("eXamine_export.svg"));
        int fileConfirm = fileChooser.showSaveDialog(rootPanel);

        // Output to valid file.
        if(fileConfirm == JFileChooser.APPROVE_OPTION) {
            boolean useCSS = true; // we want to use CSS style attributes
            Writer out = new FileWriter(fileChooser.getSelectedFile());
            svgGenerator.stream(out, useCSS);
        }
    }

    private class LocalMouseListener implements MouseListener {

        public void mouseClicked(MouseEvent me) {
            storeEvent(me);

            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.mouseClicked(me);
            }
        }

        public void mousePressed(MouseEvent me) {
            storeEvent(me);

            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.mousePressed(me);
            }
        }

        public void mouseReleased(MouseEvent me) {
            storeEvent(me);

            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.mouseReleased(me);
            }
        }

        public void mouseEntered(MouseEvent me) {
            storeEvent(me);
        }

        public void mouseExited(MouseEvent me) {
            storeEvent(me);
        }

    }

    private class LocalMouseMotionListener implements MouseMotionListener {

        public void mouseDragged(MouseEvent me) {
            storeEvent(me);

            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.mouseDragged(me);
            }
        }

        public void mouseMoved(MouseEvent me) {
            storeEvent(me);

            // Send event to hovered item.
            if(animatedGraphics.getDrawManager() != null && animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.mouseMoved(me);
            }
        }

    }

    private class LocalKeyListener implements KeyListener {

        public void keyPressed(KeyEvent e) {
            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.keyPressed(e);
            }

            // Delegate.
            ApplicationFrame.this.keyPressed(e);
        }

        public void keyReleased(KeyEvent e) {
            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.keyReleased(e);
            }

            // Delegate.
            ApplicationFrame.this.keyReleased(e);
        }

        public void keyTyped(KeyEvent e) {
            // Send event to hovered item.
            if(animatedGraphics.getDrawManager().hovered != null) {
                animatedGraphics.getDrawManager().hovered.keyTyped(e);
            }

            // Delegate.
            ApplicationFrame.this.keyTyped(e);
        }
//
//        public void keyPressed() {
//            // Send event to hovered item.
//            if(animatedGraphics.getDrawManager().hovered != null) {
//                animatedGraphics.getDrawManager().hovered.keyPressed();
//            }
//
//            ApplicationFrame.this.keyPressed();
//        }
//
//        public void keyReleased() {
//            // Send event to hovered item.
//            if(animatedGraphics.getDrawManager().hovered != null) {
//                animatedGraphics.getDrawManager().hovered.keyReleased();
//            }
//
//            ApplicationFrame.this.keyReleased();
//        }

    }

}
