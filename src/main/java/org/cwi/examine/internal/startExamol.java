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
public class startExamol extends Application {
	Stage primaryStage;
	boolean file_chsn = false;
	
	
	/**
	if(args(1) not NULL):
	args {open}:
 * DataSet dataSet = new DataSet("file");
 */
	
	
	@Override
   	public void start(final Stage stage) throws java.io.IOException {
        Button button = new Button("Choose");
        Button vbf = new Button("Valence Bond Formula");
        Button scel = new Button("Skeletal Formula");
        Option option = new Option();
        Label chosen = new Label();
	
        
        
        
      	button.setOnAction(event -> {
       		 FileChooser chooser = new FileChooser();
       		 File file = chooser.showOpenDialog(stage);
        	 option.setPath(file.toString());
        	 if (file != null) {
                 	String  fileAsString = file.toString();
			chosen.setText("Chosen: " + fileAsString);
			 file_chsn = true;
            	} else {
                	chosen.setText(null);
            	} 
           });
     		
     		
     	// EVENT SF	
     	scel.setOnAction(event -> {
     	
     	
     		if (file_chsn  != false) {
        	try{      
        		 option.setScel("On");
        		 DataRead dataread = new DataRead();
         		 DataSet dataSet = new DataSet();
         		 Model model = new Model(dataSet);
         		 Visualization visualization = new Visualization(model);
         		 dataSet.load();   
                 } catch (IOException e) {
       			System.err.println("Error: " + e);
     		 }    
     		
     		 } else {
                	chosen.setText("File Missing! ");
            	} 
     		 
     	});
     		
     	// EVENT VBF
     	vbf.setOnAction(event -> {
     	
   	  	if (file_chsn != false) {
        	try{      
        		 option.setScel("Off");
         		 DataRead dataread = new DataRead();
         		 DataSet dataSet = new DataSet();
         		 Model model = new Model(dataSet);
         		 Visualization visualization = new Visualization(model);
         		 dataSet.load();
         
         	} catch (IOException e) {
       		System.err.println("Error: " + e);
     		}
     		
     		
     		  } else {
                	chosen.setText("File Missing! ");
            	} 
        });
        
        
        
            
          
            
     

        VBox layout = new VBox(10, button, chosen, scel, vbf);
        layout.setMinWidth(200);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout));
        stage.show();
    }

/*
	
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		mainWindow();
		System.out.println("test main");
	
	}
	
	public void mainWindow() {
		
		try {
		
			FXMLLoader loader = new FXMLLoader(getClass().getResource(
					"ui/view/view.fxml "
					));
		
			AnchorPane pane = loader.load();
		
			primaryStage.setMinWidth(400.00);
		
			primaryStage.setMinHeight(325.00);
		
			Scene scene = new Scene(pane);
		
			MainController maincontroller = loader.getController(); 
			
			
			
			
			
			
			maincontroller.setMain(this);
			
			primaryStage.setScene(scene);
		
			primaryStage.show();
		
		
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
	}  */
	 
	public static void main(String[] args)  {
		//System.out.println(startExamol.class.getResource("Users/garethbilaney/Desktop/bla/eXamine-eXamol-/src/main/java/org/cwi/examine/internal/ui/view/view.fxml "));
		
		
		//Application.launch(Main.class, null);
    	
    	
    	//Thread thread = new Thread();
    	//thread.main(args);
    	launch(args);
    	
    	
    	
		
		
	} 
	
	
	
	/*

    public static void main(String[] args) throws IOException {
    
    	Application.launch(Main.class, null);
    	Option option = new Option();
    	option.setScel(args[0]);
    	System.out.println( option.getScel() );
    	
    	//Thread thread = new Thread();
    	//thread.main(args);
    	
    	DataRead dataread = new DataRead();
        final DataSet dataSet = new DataSet();
        final Model model = new Model(dataSet);
        final Visualization visualization = new Visualization(model);
        dataSet.load(); // Load from filesystem.
    } */
}
