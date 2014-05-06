package org.cytoscape.examine.internal.visualization.overview;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import org.cytoscape.examine.internal.data.HNode;
import org.cytoscape.examine.internal.data.HSet;
import org.cytoscape.examine.internal.som.Coordinates;
import org.cytoscape.examine.internal.som.SelfOrganizingMap;
import org.cytoscape.examine.internal.som.Trainer;
import org.cytoscape.examine.internal.visualization.Parameters;
import org.cytoscape.examine.internal.visualization.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.cytoscape.examine.internal.graphics.PVector;

/**
 * Generates set contours for a SOM.
 */
public class SOMContours {
    
    // Target SOM.
    public final SelfOrganizingMap som;
    
    // SOM learning model, for sets and proteins.
    public final Trainer trainer;
    
    // SOM tile set membership map.
    private final boolean[][] memberships;
    private final boolean[][] filleds;
    
    // Fuzzy level required for empty tile to be marked as part of a set.
    public double emptyMembershipThreshold;
    
    // Neighborhood offset coordinates, ordered clockwise.
    private Coordinates[] neighborhoodOffsets;
    
    // Set body and outline shapes, per set index.
    public final List<Geometry> setBodyShapes, setOutlineShapes;
    
    /**
     * Base constructor.
     */
    public SOMContours(Trainer trainer) {
        this.som = trainer.som;
        this.trainer = trainer;
        this.emptyMembershipThreshold = Parameters.emptyMembershipThreshold.get();
        
        this.memberships = new boolean[som.topology.xSize][som.topology.ySize];
        this.filleds = new boolean[som.topology.xSize][som.topology.ySize];
        
        // Fetch neighborhood offsets and sort by angle.
        this.neighborhoodOffsets = new Coordinates[6];
        for(int i = 0; i < 6; i++) {
            int xO = som.topology.xNeighborhoodOffsets.length > 1 ?
                        som.topology.xNeighborhoodOffsets[1][i] :
                        0;
            int yO = som.topology.yNeighborhoodOffsets.length > 1 ?
                        som.topology.yNeighborhoodOffsets[1][i] :
                        0;
            neighborhoodOffsets[i] = new Coordinates(xO, yO);
        }
        Arrays.sort(neighborhoodOffsets, new Comparator<Coordinates>() {

            @Override
            public int compare(Coordinates co1, Coordinates co2) {
                PVector v1 = new PVector(co1.x, co1.y);
                PVector v2 = new PVector(co2.x, co2.y);

                return v1.heading2D() < v2.heading2D() ? -1 : 1;
            }

        });
        
        this.setBodyShapes = new ArrayList<Geometry>();
        this.setOutlineShapes = new ArrayList<Geometry>();
    }
    
    /**
     * Update sets contours.
     */
    public void update() {
        setBodyShapes.clear();
        setOutlineShapes.clear();
        
        // Update sets individually.
        for(int i = 0; i < trainer.learningModel.proteinSets.size(); i++) {
            updateSet(i);
        }
    }
    
    private void updateSet(int setIndex) {
        HSet set = trainer.learningModel.proteinSets.get(setIndex);
        double setWeight = trainer.learningModel.proteinSetWeights.get(set);
        
        // Set membership map.
        for(int i = 0; i < som.neurons.length; i++) {
            double value = som.neurons[i][setIndex] / setWeight;
            
            int x = som.topology.x[i];
            int y = som.topology.y[i];
            memberships[x][y] = value > emptyMembershipThreshold;
            filleds[x][y] = false;
        }
        
        // Strict membership map for filled tiles.
        for(int i = 0; i < trainer.learningModel.proteins.size(); i++) {
            HNode p = trainer.learningModel.proteins.get(i);
            Coordinates co = trainer.proteinCoordinates[i];
            memberships[co.x][co.y] = set.elements.contains(p);
            filleds[co.x][co.y] = true;
        }
        
        // Base tile geometry.
        double rimWidth = 10;
        double roundDilationErosion = SOMOverview.tileSide;
        int setSize = trainer.learningModel.proteinSets.size() - 1;
        double indexErosion = setSize == 0 ?
                                0 :
                                Math.min((rimWidth + 2) * setIndex,
                                    (SOMOverview.tileSide - Parameters.NODE_RADIUS) *
                                    (double) setIndex / (double) setSize);
        
        // Membership tiles.
        List<Geometry> tiles = new ArrayList<Geometry>();
        for(int x = -1; x <= memberships.length; x++) {
            for(int y = -1; y <= memberships[0].length; y++) {
                Coordinates cs = new Coordinates(x, y);
                PVector position = SOMOverview.somToOverview(cs);
                boolean membership = 0 <= x && x < memberships.length &&
                                     0 <= y && y < memberships[0].length &&
                                     memberships[x][y];
                boolean filled = 0 <= x && x < memberships.length &&
                                 0 <= y && y < memberships[0].length &&
                                 filleds[x][y];
                
                // Neighbor memberships.
                boolean[] nghMbs = new boolean[6];
                PVector[] nghPos = new PVector[6];
                for(int i = 0; i < neighborhoodOffsets.length; i++) {
                    Coordinates nO = neighborhoodOffsets[i];
                    Coordinates nCs = cs.hexagonallyTranslated(nO);
                    nghPos[i] = SOMOverview.somToOverview(nCs);

                    nghMbs[i] = 0 <= nCs.x && nCs.x < memberships.length &&
                                0 <= nCs.y && nCs.y < memberships[0].length &&
                                memberships[nCs.x][nCs.y];
                }

                // Tile points.
                List<Coordinate> partialCs = new ArrayList<Coordinate>();
                for(int i = 0; i < 6; i++) {
                    if(membership ||
                       (!filled && (nghMbs[i] || nghMbs[(i+1) % 6]))) {
                        PVector partialP = position.get();
                        partialP.add(nghPos[i]);
                        partialP.add(nghPos[(i+1) % 6]);
                        partialP.div(3);

                        partialCs.add(new Coordinate(partialP.x, partialP.y));
                    }
                }
                
                // There is something two dimensional to add.
                if(partialCs.size() > 2) {
                    Coordinate[] partialCsAr = new Coordinate[partialCs.size() + 1];
                    for(int i = 0; i < partialCsAr.length; i++) {
                        partialCsAr[i] = partialCs.get(i % partialCs.size());
                    }
                    
                    tiles.add(Util.geometryFactory.createPolygon(partialCsAr));
                }
            }
        }
        Geometry tileUnion = Util.geometryFactory.buildGeometry(tiles)
                                 .buffer(1)
                                 .union()
                                 .buffer(-roundDilationErosion)
                                 .buffer(2 * roundDilationErosion)
                                 .buffer(-roundDilationErosion - indexErosion);
        
        setOutlineShapes.add(tileUnion);
        
        Geometry tileInnerContour = tileUnion.buffer(-rimWidth);
        Geometry dilatedInnerContour = tileInnerContour.buffer(rimWidth);
        Geometry roundedInnerContour = dilatedInnerContour
                                       .buffer(-2 * rimWidth)
                                       .buffer(rimWidth);
        Geometry tileBody = tileUnion.symDifference(roundedInnerContour);
        
        setBodyShapes.add(tileBody);
    }
    
}
