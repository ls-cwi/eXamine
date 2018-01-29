package org.cytoscape.examine.internal.visualization;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.graphics.AnimatedGraphics;
import org.cytoscape.examine.internal.model.Model;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SnapshotVisualization extends Visualization {

    public SnapshotVisualization(DataSet dataSet, Model model) {
        super(dataSet, model);
    }

    @Override
    protected void finalize() throws Throwable {
        overview.stop();    // Request overview animation stop.
        super.finalize();
    }

    public void exportSVG(File exportFile) throws IOException {
        // Create document.
        final DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        final String svgNS = "http://www.w3.org/2000/svg";
        final Document document = domImpl.createDocument(svgNS, "svg", null);

        // Embed fonts.
        final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setEmbeddedFontsOn(true);

        // Write visualization.
        final SVGGraphics2D svgGraphics = new SVGGraphics2D(ctx, true);
        visualize(svgGraphics);

        final Writer out = new FileWriter(exportFile);
        svgGraphics.stream(out, true);
    }

    private void visualize(Graphics2D graphics) {
        final AnimatedGraphics animatedGraphics = new AnimatedGraphics(graphics);

        animatedGraphics.getDrawManager().pre();
        drawVisualization(animatedGraphics, 0, 0);
    }

}
