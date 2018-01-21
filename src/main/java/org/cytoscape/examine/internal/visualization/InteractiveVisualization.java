package org.cytoscape.examine.internal.visualization;

import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.graphics.ApplicationFrame;
import org.cytoscape.examine.internal.model.Model;

public class InteractiveVisualization extends Visualization {

    private final VisualizationFrame applicationFrame = new VisualizationFrame();

    public InteractiveVisualization(DataSet dataSet, Model model) {
        super(dataSet, model);
    }

    private class VisualizationFrame extends ApplicationFrame {

        @Override
        public void draw(AnimatedGraphics graphics) {
            drawVisualization(graphics, getWidth(), getHeight());
        }

        @Override
        public void dispose() {
            overview.stop();    // Request overview animation stop.
            super.dispose();
        }
    }

}
