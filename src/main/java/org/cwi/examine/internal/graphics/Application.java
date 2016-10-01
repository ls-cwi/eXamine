package org.cwi.examine.internal.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.cwi.examine.internal.graphics.draw.Parameters;
import org.cwi.examine.internal.graphics.draw.Snippet;
import org.cwi.examine.internal.signal.gui.SidePane;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import static org.cwi.examine.internal.graphics.StaticGraphics.*;

// Graphics application.
public abstract class Application extends JFrame {
    private final JPanel rootPanel;     // Main content panel.
    private SidePane sidePane;          // Side pane (for GUI).
    protected Snippet rootSnippet;      // Root snippet.
    private DrawManager drawManager;    // Snippet manager.
    
    protected int mouseX, mouseY;       // Mouse event information.
    protected MouseEvent mouseEvent;
    
    public Application() {
        setSize(1000, 600);
        setExtendedState(Frame.NORMAL);
        setVisible(true);
        
        // Side pane.
        sidePane = new SidePane();
        
        // Graphics setup.
        setup();

        rootPanel = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
        
                Graphics2D g2 = (Graphics2D) g;
                drawManager.defaultGraphics = g2;

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

                Snippet hovered = hovered();
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
            public void draw() {
                // Font.
                textFont(Parameters.font);
                
                // Make room for side pane.
                pushTransform();
                translate(sidePane.paneWidth(), 0);
                
                // Draw (sub classed).
                Application.this.draw();
                popTransform();
                
                // Draw side pane on top of everything.
                snippet(sidePane);
            }
            
        };
        
        // Event listeners.
        rootPanel.addMouseListener(new LocalMouseListener());
        rootPanel.addMouseMotionListener(new LocalMouseMotionListener());
        this.addKeyListener(new LocalKeyListener());
    }
    
    public final void setup() {
        // Draw manager.
        drawManager = new DrawManager(this);
        
        // Adapt picking buffer to canvas size.
        drawManager.updatePickingBuffer();
        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent ce) {
                drawManager.updatePickingBuffer();
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
        
        // Mouse wheel listener, routes to hovered item.
        addMouseWheelListener(mwe -> {
            if(drawManager.hovered != null) {
                drawManager.hovered.mouseWheel(mwe.getWheelRotation());
            }
        });
        
        // Load base and label fonts; load open sans and use Arial as fall-back.
        try {
            InputStream input = Application.class.getResourceAsStream("/font/OpenSans-Regular.ttf");
            Font inputFont = Font.createFont(Font.TRUETYPE_FONT, input);
            
            Parameters.font = inputFont.deriveFont(24f);
            Parameters.labelFont = inputFont.deriveFont(14f);
            Parameters.noteFont = inputFont.deriveFont(8f);
        } catch(Exception ex) {
            System.out.println("Font load exception: " + ex.getLocalizedMessage());
            
            Parameters.font = new Font("Arial", Font.PLAIN, 18);
            Parameters.labelFont = new Font("Arial", Font.PLAIN, 14);
            Parameters.noteFont = new Font("Arial", Font.PLAIN, 8);
        }
    }
    
    // Get side pane.
    public SidePane sidePane() {
        return sidePane;
    }
    
    public final void rootDraw() {
        // Manager global pre rootDraw.
        drawManager.pre();
        
        // Draw to normal graphics.
        drawManager.preScreen();
        StaticGraphics.snippet(rootSnippet);
        drawManager.postScreen();
        
        // Draw to picking graphics.
        drawManager.prePicking();
        StaticGraphics.snippet(rootSnippet);
        drawManager.postPicking();
        
        // Manager global post rootDraw.
        drawManager.post();
    }
    
    // Input methods.
    private void storeEvent(MouseEvent me) {
        mouseX = me.getX();
        mouseY = me.getY();
        mouseEvent = me;
    }
    
    private class LocalMouseListener implements MouseListener {
        
        public void mouseClicked(MouseEvent me) {            
            storeEvent(me);
            
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.mouseClicked(me);
            }
        }

        public void mousePressed(MouseEvent me) {
            storeEvent(me);
            
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.mousePressed(me);
            }
        }

        public void mouseReleased(MouseEvent me) {    
            storeEvent(me);
            
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.mouseReleased(me);
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
            if(drawManager.hovered != null) {
                drawManager.hovered.mouseDragged(me);
            }
        }

        public void mouseMoved(MouseEvent me) {
            storeEvent(me);        
            
            // Send event to hovered item.
            if(drawManager != null && drawManager.hovered != null) {
                drawManager.hovered.mouseMoved(me);
            }
        }
        
    }
    
    private class LocalKeyListener implements KeyListener {

        public void keyPressed(KeyEvent e) {        
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.keyPressed(e);
            }

            // Delegate.
            Application.this.keyPressed(e);
        }

        public void keyReleased(KeyEvent e) {
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.keyReleased(e);
            }

            // Delegate.
            Application.this.keyReleased(e);
        }

        public void keyTyped(KeyEvent e) {
            // Expand options pane by pressing Ctrl + o.
            if(e.getKeyChar() == 'o') {
                System.out.println("Press o");
                if(sidePane.root().active) {
                    sidePane.activate(sidePane.root());
                } else {
                    sidePane.deactivate(sidePane.root());
                }
            }

            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.keyTyped(e);
            }

            // Delegate.
            Application.this.keyTyped(e);
        }

        public void keyPressed() {
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.keyPressed();
            }

            Application.this.keyPressed();
        }

        public void keyReleased() {
            // Send event to hovered item.
            if(drawManager.hovered != null) {
                drawManager.hovered.keyReleased();
            }

            Application.this.keyReleased();
        }
        
    }

    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_P) {
            try {
                exportSVG();
            } catch (IOException ex) {
                Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public void keyPressed() {}

    public void keyReleased() {}
    
    // Draw commands, to be implemented.
    public abstract void draw();
    
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
}
