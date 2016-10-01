package org.cwi.examine.internal;

import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.model.Model;

import java.io.FileNotFoundException;

/**
 * Application entry point.
 */
public class Application {

    public static void main(String[] args) throws FileNotFoundException {
        final DataSet dataSet = new DataSet();
        final Model model = new Model(dataSet);
        final Visualization visualization = new Visualization(model);
        dataSet.load(); // Load from filesystem.
    }
}
