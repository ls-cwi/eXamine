package org.cwi.examine.internal.ui.controller;

import java.io.File;
import java.text.DateFormat.Field;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import  org.cwi.examine.internal.startExamol;

public class MainController {
	
	//VIEW Window
	Stage primaryStage;
	public boolean skeletal; 
	
	@FXML 
	private Button start_eXamole;
	private Button choose_button;
	private Button create_button;
	

	private CheckBox show_c;
	private CheckBox show_h;
	
	private startExamol main;
	
	public void setMain(startExamol main) {
		this.main = main;
		
	}
	
	@FXML
	public void handleButtonChoose() {
		
	
		
		//label.setText("H Off");
		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
		
		  FileChooser chooser = new FileChooser();
          File file = chooser.showOpenDialog(primaryStage);
          if (file != null) {
              String path = file.toString();
              System.out.println(path);
          }
              
       
           
	}
	
	@FXML
	public void handleButtonStart() {
		
	
		System.out.println("test button_start");
		//label.setText("H Off");
		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
	
	}
	
	@FXML
	public void handleButtonSkeletal() {
		
	
		System.out.println("test button_skeletal");
	
		//show_h.setText("H Off");

		//if(hydrogen == true) {
		// start_eXamole.setText("Remove Hydrogen");
	
	}

}
