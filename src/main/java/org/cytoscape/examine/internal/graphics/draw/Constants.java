package org.cytoscape.examine.internal.graphics.draw;

import org.cytoscape.examine.internal.graphics.Colors;

import java.awt.*;

public class Constants {

    public static final double PRESENCE_TRANSITION_DURATION = 0.5;
    public static final double MOVE_TRANSITION_DURATION = 0.6;

    public static final int CURSOR_DILATION_RADIUS = 15;
    public static final Color BACKGROUND_COLOR = Colors.grey(1f);
    public static final Color CONTAINMENT_COLOR = Colors.grey(0.25f);

    public static Font FONT = null;
    public static Font LABEL_FONT = null;
    public static Font NOTE_FONT = null;
    public static final Color TEXT_COLOR = Colors.grey(0.25f);
    public static final Color TEXT_HIGHLIGHT_COLOR = Colors.grey(0.15f);
    public static final Color TEXT_HOVER_COLOR = Colors.grey(0f);
    public static final Color TEXT_CONTAINED_COLOR = Colors.grey(0.8f);
    public static final Color TEXT_CONTAINED_HIGHLIGHT_COLOR = Colors.grey(0.9f);
    public static final Color TEXT_CONTAINED_HOVER_COLOR = Colors.grey(1f);

    public static final double SPACING = 8.0;

}
