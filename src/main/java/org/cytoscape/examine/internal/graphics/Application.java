package org.cytoscape.examine.internal.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.InputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import static org.cytoscape.examine.internal.graphics.draw.Parameters.*;
import static org.cytoscape.examine.internal.graphics.StaticGraphics.*;
import org.cytoscape.examine.internal.graphics.draw.Snippet;
import org.cytoscape.examine.internal.signal.gui.SidePane;

/**
 * XProcessing application, sports scene drawing code and initialization.
 */
public abstract class Application extends JFrame {
    
    // Side pane (for GUI).
    private SidePane sidePane;
    
    // Root snippet.
    protected Snippet rootSnippet;
    
    // Snippet manager.
    private DrawManager drawManager;
    
    // Mouse event information.
    protected int mouseX, mouseY;
    protected MouseEvent mouseEvent;
    
    /**
     * Base constructor.
     */
    public Application() {
        // Fair default size, but maximize.
        setSize(1400, 800);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
        
        // Set title to class name by default.
        setTitle(getClass().getSimpleName());
        
        // Set background color.
        setBackground(Color.WHITE);
        
        // Side pane.
        sidePane = new SidePane();
        
        // Graphics setup.
        setup();
        
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel pane = new JPanel() {

            @Override
            public void paint(Graphics g) {
                super.paint(g);
        
                Graphics2D g2 = (Graphics2D) g;
                dm.defaultGraphics = g2;

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

                // Clear screen.
                g2.clearRect(0, 0, getWidth(), getHeight());

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
        ToolTipManager.sharedInstance().registerComponent(pane);
        
        pane.setDoubleBuffered(true);
        setContentPane(pane);
        
        // Root snippet, contains application rootDraw calls.
        rootSnippet = new Snippet() {

            @Override
            public void draw() {
                // Font.
                textFont(font.get());
                
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
        pane.addMouseListener(new LocalMouseListener());
        pane.addMouseMotionListener(new LocalMouseMotionListener());
    }

    @Override
    public void dispose() {
        // TODO: stop animation process.
        
        super.dispose();
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
        
        // Mouse wheel listener.
        addMouseWheelListener(new MouseWheelListener() {
            
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) { 
                // Send event to hovered item.
                if(drawManager.hovered != null) {
                    drawManager.hovered.mouseWheel(mwe.getWheelRotation());
                }
            }
        
        });
        
        // Load base and label fonts; load open sans and use Arial as fall-back.
        try {
            InputStream input = Application.class.getResourceAsStream("/font/OpenSans-Regular.ttf");
            Font inputFont = Font.createFont(Font.TRUETYPE_FONT, input);
            
            font.set(inputFont.deriveFont(18f));
            labelFont.set(inputFont.deriveFont(14f));
        } catch(Exception ex) {
            System.out.println("Font load exception: " + ex.getLocalizedMessage());
            
            font.set(new Font("Arial", Font.PLAIN, 18));
            labelFont.set(new Font("Arial", Font.PLAIN, 14));
        }
        
        // Initialize implementing class.
        initialize();
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

    
    /**
     * Input methods.
     */
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

    public void keyPressed(KeyEvent e) {        
        // Send event to hovered item.
        if(drawManager.hovered != null) {
            drawManager.hovered.keyPressed(e);
        }
    }

    public void keyReleased(KeyEvent e) {
        // Send event to hovered item.
        if(drawManager.hovered != null) {
            drawManager.hovered.keyReleased(e);
        }
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
    }

    public void keyPressed() {
        // Send event to hovered item.
        if(drawManager.hovered != null) {
            drawManager.hovered.keyPressed();
        }
    }

    public void keyReleased() {
        // Send event to hovered item.
        if(drawManager.hovered != null) {
            drawManager.hovered.keyReleased();
        }
    }

    // Draw initialization commands, to be implemented.
    public abstract void initialize();
    
    // Draw commands, to be implemented.
    public abstract void draw();
    
}
