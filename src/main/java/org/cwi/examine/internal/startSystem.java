package org.cwi.examine.internal;

import org.cwi.examine.internal.data.DataSet;
import org.cwi.examine.internal.visualization.Visualization;
import org.cwi.examine.internal.model.Model;
import org.cwi.examine.internal.molepan.dataread.DataRead;

//import org.cwi.examine.internal.ui.controller.Main;
import org.cwi.examine.internal.ui.controller.MainController;
//import org.cwi.examine.internal.ui.controller.Thread;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import java.util.concurrent.CountDownLatch;
import javafx.application.Application;



import org.cwi.examine.internal.Option;
import javafx.application.Application;
import java.io.IOException;

/**
 * Application entry point.
 */
public class startSystem {
	
	 
	public static void startsystem() throws IOException {
		//System.out.println(startExamol.class.getResource("Users/garethbilaney/Desktop/bla/eXamine-eXamol-/src/main/java/org/cwi/examine/internal/ui/view/view.fxml "));
		
		
		//Application.launch(Main.class, null);
    	Option option = new Option();
    	option.setScel("On");
    	System.out.println( option.getScel() );
    	
    	//Thread thread = new Thread();
    	//thread.main(args);
    	
    	DataRead dataread = new DataRead();
        final DataSet dataSet = new DataSet();
        final Model model = new Model(dataSet);
        final Visualization visualization = new Visualization(model);
        dataSet.load(); // Load from filesystem.
	
		
	} 
	
	
	
	
}
