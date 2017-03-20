package Boundaries;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InitalizeGuardianAngel extends Application{
	private Canvas canvas = new Canvas(1335, 542);
    private Canvas canvas2 = new Canvas(1335, 542);
    private Canvas canvas3 = new Canvas(300,125);
    private Canvas canvas4 = new Canvas(1335,680);
    private Pane root = new Pane();
    private Pane root2 = new Pane();
    private Pane root3 = new Pane(); //settings page
	int x,y;
    GraphicsContext gc = canvas.getGraphicsContext2D();
    GraphicsContext gc2 = canvas2.getGraphicsContext2D();
    GraphicsContext gc3 = canvas3.getGraphicsContext2D();
    GraphicsContext gc4 = canvas4.getGraphicsContext2D();
	private StackPane holder = new StackPane();
	private StackPane holder2 = new StackPane();
	private StackPane holder3 = new StackPane();
	ImageView floorplan,siren;
    Button btnLoad = new Button("Load floorplan");
    Button seeResults = new Button("See results");
    Button save = new Button("Save and exit");
    Image image,image2,mobility,dementia,trained,untrained,mobility_64,dementia_64,untrained_64,trained_64;
    TextField cgText,crText;
    TextArea actLog;
    MenuBar menuBar = new MenuBar();
    Menu menu = new Menu("Options");
    MenuItem settings = new MenuItem("Settings", null);
    MenuItem exit = new MenuItem("Exit", null);
	ChoiceBox numCG, numCR, numMob, numDem, numTr, numUntr, simDur;
    Stage stageSetting = new Stage();
    String numCGdata,numTrData,numUntrData,numCRdata,numMobData,numDemData,durData,radio_algo1; /*Contains value from the settings page*/
    RadioButton Algo1,Algo2; 
    final ToggleGroup group = new ToggleGroup();	

	public static void main(String[] args) throws ClassNotFoundException, SQLException
	  {
		launch(args);
	  }

	@Override
    public void start(Stage primaryStage) throws SQLException, ClassNotFoundException{
		image = new Image(getClass().getResourceAsStream("siren_red.png"));
		image2 = new Image(getClass().getResourceAsStream("siren.png"));
		mobility = new Image(getClass().getResourceAsStream("mobility.png"));
		dementia = new Image(getClass().getResourceAsStream("dementia.png"));
		trained = new Image(getClass().getResourceAsStream("trained.png"));
		untrained = new Image(getClass().getResourceAsStream("untrained.png"));	
		mobility_64 = new Image(getClass().getResourceAsStream("mobility_64.png"));
		dementia_64 = new Image(getClass().getResourceAsStream("dementia_64.png"));
		trained_64 = new Image(getClass().getResourceAsStream("trained_64.png"));
		untrained_64 = new Image(getClass().getResourceAsStream("Untrained_nurse_64.png"));
		primaryStage = new Stage();
		primaryStage.setWidth(1335);
		primaryStage.setHeight(542);
        primaryStage.setTitle("Guardian Angel Simulator");
        primaryStage.setMaximized(true);
        /*Load button*/
        btnLoad.setOnAction(btnLoadEventListener);
        btnLoad.setLayoutX(holder.getWidth()/2);
        btnLoad.setLayoutY(holder.getHeight()/2);
        /*results button*/
        seeResults.setOnAction(seeResultsEventListener);
        seeResults.setLayoutX(holder.getWidth()/2);
        seeResults.setLayoutY(holder.getHeight()/2);
        /*imageView for floorplan*/
        floorplan = new ImageView();   
        
        
        
        
        settings.setOnAction(settingsEventListener);
        exit.setOnAction(exitEventListener);
        menu.getItems().add(settings);
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(exit);
        menuBar.getMenus().add(menu);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        
       
        
        holder.getChildren().add(floorplan);
        root.getChildren().add(menuBar);
        holder.getChildren().add(canvas);
        holder.getChildren().add(canvas2);
       
        holder.getChildren().add(btnLoad);
        holder.getChildren().add(seeResults);
        seeResults.setVisible(false);
        holder.setLayoutX(12);
        holder.setLayoutY(26);
        root.getChildren().add(holder);
        
        Label mobLabel = new Label("Mobility problem");
        mobLabel.setStyle("-fx-font-weight: bold");
        mobLabel.setLayoutX(1080);
        mobLabel.setLayoutY(606);
        mobLabel.setFont(Font.font ("Verdana", 12));
        Label demLabel = new Label("Dementia");
        demLabel.setStyle("-fx-font-weight: bold");
        demLabel.setLayoutX(1080);
        demLabel.setLayoutY(658);
        demLabel.setFont(Font.font ("Verdana", 12));
        
        Label trLabel = new Label("Trained");
        trLabel.setStyle("-fx-font-weight: bold");
        trLabel.setLayoutX(1250);
        trLabel.setLayoutY(606);
        trLabel.setFont(Font.font ("Verdana", 12));
        
        Label untrLabel = new Label("Untrained");
        untrLabel.setStyle("-fx-font-weight: bold");
        untrLabel.setLayoutX(1250);
        untrLabel.setLayoutY(658);
        untrLabel.setFont(Font.font ("Verdana", 12));
        
   
        root.getChildren().add(mobLabel);
        root.getChildren().add(demLabel);
        root.getChildren().add(trLabel);
        root.getChildren().add(untrLabel);
        gc3.setFill(Color.CORNFLOWERBLUE);
       
        gc3.fillRect(3, 20, 25, 25);
        gc3.setFill(Color.BLUE);
      
        gc3.fillRect(3, 70, 25, 25);
        //gc3.drawImage(mobility, 0, 20);
        //gc3.drawImage(dementia, 0, 70);
        gc3.setFill(Color.DARKSLATEGREY);
        gc3.fillRect(170, 20, 25, 25);
        gc3.setFill(Color.DARKGREY);
        gc3.fillRect(170, 70, 25, 25);
        //gc3.drawImage(trained, 170, 20);
        //gc3.drawImage(untrained, 170, 70);

   
        
        
        holder2.getChildren().add(canvas3);
        holder2.setLayoutX(1047);
        holder2.setLayoutY(573);

        
        root.getChildren().add(holder2);
        
        
        holder.setStyle("-fx-border-color: black");
        holder2.setStyle("-fx-border-color: black");
        
 
        cgText = new TextField();
        cgText.setEditable(false);
        
        
        cgText.setPrefColumnCount(5);
        
        HBox hBox = new HBox();
        hBox.setSpacing(29);
        Label cgLabel = new Label("No. of available Caregivers:");
        cgLabel.setStyle("-fx-font-weight: bold");
        hBox.getChildren().add(cgLabel);
        hBox.getChildren().add(cgText);
        root.getChildren().add(hBox);
        hBox.setLayoutX(720);
        hBox.setLayoutY(580);
        
        crText = new TextField();
        crText.setEditable(false);
        //titleTextField2.setDisable(true);
        crText.setPrefColumnCount(5);
        
        HBox hBox2 = new HBox();
        hBox2.setSpacing(5);
        Label crLabel = new Label("No. of available Care-receivers:");
        crLabel.setStyle("-fx-font-weight: bold");
        hBox2.getChildren().add(crLabel);
        hBox2.getChildren().add(crText);
        root.getChildren().add(hBox2);
        hBox2.setLayoutX(720);
        hBox2.setLayoutY(640);
        
        HBox hBox3 = new HBox();
        actLog = new TextArea();
        actLog.setPrefColumnCount(50);
        actLog.setPrefRowCount(6);
        hBox3.setSpacing(5);
        Label actLabel = new Label("Activity Log:");
        actLabel.setStyle("-fx-font-weight: bold");
        hBox3.getChildren().add(actLabel);
        hBox3.getChildren().add(actLog);
        root.getChildren().add(hBox3);
        hBox3.setLayoutX(11);
        hBox3.setLayoutY(580); 
        
        
    
        
        
        Scene scene = new Scene(root, 1335, 542);
        primaryStage.setScene(scene);
        primaryStage.show(); 
        
        
        

    }
	
	public int getNumCG(){
		if(numCGdata == null){
			return 6;
		}
		else{
			return Integer.valueOf(numCGdata);
		}
		
	}
	
	public int getNumCR(){
		if(numCRdata == null){
			return 10;
		}
		else {
			return Integer.valueOf(numCRdata);
		}
	}
	public int getNumDem(){
		if(numDemData == null){
			return 5;
		}
		else{
			return Integer.valueOf(numDemData);
		}
		
	}
	
	public int getNumMob(){
		if(numMobData == null){
			return 5;
		}
		else{
			return Integer.valueOf(numMobData);
		}
		
	}
	
	public int getNumTr(){
		if(numTrData == null){
			return 3;
		}
		else{
			return Integer.valueOf(numTrData);	
		}
		
	}
	
	public int getNumUntr(){
		if(numUntrData == null){
			return 3;
		}
		else{
			return Integer.valueOf(numUntrData);	
		}
		
	}
	
	public int getSimDur(){
		if(durData == null){
			return 100;
		}
		else{
			return Integer.valueOf(durData);	
		}
		
	}
	
	public String getRadio_algo1(){
		if(radio_algo1 == null){
			return "Algorithm 1";
		}
		else{
			return radio_algo1;		
		} 
			
		
	}

	
	public void setNumCG(String cg){
		this.numCGdata = cg;
	}
	
	  
    protected void setSimDur(String simDur) {
		this.durData = simDur;
		
	}

	protected void setNumDem(String numDem) {
		this.numDemData = numDem;
		
	}

	protected void setNumMob(String numMob) {
		this.numMobData = numMob;
		
	}

	protected void setNumCR(String numCR) {
		this.numCRdata = numCR;
		
		
	}

	protected void setNumTr(String numTr) {
		this.numTrData = numTr;
		
	}

	protected void setNumUntr(String numUntr) {
		this.numUntrData = numUntr;
		
	}
	
	protected void setRadio_algo1(String radSelect) {
		this.radio_algo1 = radSelect;
		
	}



	
	
    public void updateCG(double[][] update, String [] updateClass, String[] updateName, int choice){
    	if(update.length == 0 || updateClass.length == 0){
    		return;
    	}
    	int loopCount = 0;
    	if (choice == 0){
    		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());	
    	}
		  while(loopCount<update.length){
			  String expertise = updateClass[loopCount];
			  if(expertise.equalsIgnoreCase("skilled")){
				  
				
				 
				  fillOvalCG(update[loopCount][0],update[loopCount][1]);
				  //gc.drawImage(trained,update[loopCount][0],update[loopCount][1]);
				//  gc.drawImage(trained, update[loopCount][0], update[loopCount][1], 20, 20,  update[loopCount][0]+10,  update[loopCount][1]+10, 20, 20);
				    
				  //gc.drawImage(trained,update[loopCount][0],update[loopCount][1]+32 , 10, 10);
				  //gc.fillOval(update[loopCount][0],update[loopCount][1], 1, 1);
				  //gc.setFill(Color.AZURE);
				  gc.fillText(updateName[loopCount], update[loopCount][0], update[loopCount][1]);
				  }
			  else{
				
				  fillOvalUnskCG(update[loopCount][0],update[loopCount][1]);
				  //gc.drawImage(untrained, update[loopCount][0]-5,update[loopCount][1]-5);		
				  //gc.fillOval(update[loopCount][0],update[loopCount][1], 1, 1);
				 // gc.setFill(Color.RED);
				  gc.fillText(updateName[loopCount], update[loopCount][0], update[loopCount][1]);
			  }  
			loopCount++;
		  }
    }
    
	public void updateCR(double[][] update, String [] updateClass, String [] updateName, int choice){
		if(update.length == 0 || updateClass.length == 0){
    		return;
    	}
    	int loopCount = 0;
    	if (choice == 0){
    		gc2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());		
    	}
		while(loopCount<update.length){
			String classification = updateClass[loopCount];
			//System.out.println(classification);
			if(classification.equalsIgnoreCase("dementia")){
				fillOvalCR(update[loopCount][0],update[loopCount][1]);
				//gc2.fillOval(update[loopCount][0],update[loopCount][1], 1, 1);
				//gc2.setFill(Color.RED);
				//gc2.fillRect(20, 20, 100, 100);
				//gc2.drawImage(dementia,update[loopCount][0],update[loopCount][1]);	
				gc2.fillText(updateName[loopCount], update[loopCount][0], update[loopCount][1]);
			}
			else{
				fillOvalMobCR(update[loopCount][0],update[loopCount][1]);
				//gc2.fillOval(update[loopCount][0],update[loopCount][1], 1, 1);
				//gc2.setFill(Color.BLUE);
				//gc2.drawImage(mobility, update[loopCount][0],update[loopCount][1]);
				gc2.fillText(updateName[loopCount], update[loopCount][0], update[loopCount][1]);
			}
		loopCount++;
		  }
    }
	
	
    
	public void fillOvalCG(double x, double y){
		 gc.setFill(Color.DARKSLATEGREY);
		gc.fillOval(x, y, 5, 5);
        
    }
	

    public void fillOvalUnskCG(double x, double y) {
    	gc.setFill(Color.DARKGREY);
    	gc.fillOval(x, y, 5, 5);

	}
    

    public void fillOvalCR(double x, double y){
    	gc2.setFill(Color.BLUE);
    	 gc2.fillOval(x, y, 5, 5);

    }
    
    
    public void fillOvalMobCR(double x, double y) {
    	gc2.setFill(Color.CORNFLOWERBLUE);
    	gc2.fillOval(x, y, 5, 5) ;
	}
    
    public void setCgText(String text){
    	javafx.application.Platform.runLater( () ->cgText.setText(text) );
    }
    
    public void setCrText(String text){
    	javafx.application.Platform.runLater( () ->crText.setText(text) );
    	
    }
    
    public void setActText(String text){
    	javafx.application.Platform.runLater( () -> actLog.appendText(text + "\n") );
    }
    
    
    /*For settings page*/
    EventHandler<ActionEvent> settingsEventListener
    = new EventHandler<ActionEvent>(){
    	
    	
		@SuppressWarnings("unchecked")
		@Override
		public void handle(ActionEvent event) {
			
			if (stageSetting.getScene() == null){
		        stageSetting.setTitle("Guardian Angel Simulator settings");
				stageSetting.setWidth(820);
				stageSetting.setHeight(400);
				stageSetting.setResizable(false);
						
			    Scene scene = new Scene(root3, 1335, 542);
			    stageSetting.setScene(scene);
			    stageSetting.show();
			} else {
				numCGdata = (String) numCG.getUserData();
				numTrData = (String) numTr.getUserData();
				numUntrData = (String) numUntr.getUserData();
				numCRdata = (String) numCR.getUserData();
				numMobData = (String) numMob.getUserData();
				numDemData = (String) numDem.getUserData();
				durData = (String) simDur.getUserData();
			//	System.out.println(radio_algo1);
				root3.getChildren().clear();
			    stageSetting.show();
			    
				
			    
			}
			
			//Scene scene = new Scene(root3, 1335, 542);
			//stage.setScene(scene);
			
		    /*Settings page*/
	        numCG = new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        numCR = new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        
	        numTr = new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "0","1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        numUntr =  new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "0","1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        numDem =  new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "0","1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        numMob =  new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "0","1", "2", "3","4","5","6","7","8","9","10","11","12")
	        		);
	        simDur =  new ChoiceBox<String>(FXCollections.observableArrayList(
	        	    "100","200", "300","400","500","600","700","800","900","1000")
	        		);
	        
	        Algo1 = new RadioButton("Algorithm 1");
	        
	        Algo2 = new RadioButton("Algorithm 2");
	        
	        
	        Label cgNumLabel = new Label("Number of caregivers:");
	        cgNumLabel.setStyle("-fx-font-weight: bold");
	        cgNumLabel.setLayoutX(10);
	        cgNumLabel.setLayoutY(20);
	        cgNumLabel.setFont(Font.font ("Verdana", 12));
	        numCG.setLayoutX(235);
	        numCG.setLayoutY(16);
	        if(numCGdata == null){
	        	numCG.setValue("6");
	        }
	        else{
	        	numCG.setValue(numCGdata);
	        }
	        
	        numCG.setOnAction(cgChoiceEventListener);
	        
	        Label trNumLabel = new Label("Trained caregivers:");
	        trNumLabel.setStyle("-fx-font-weight: bold");
	        trNumLabel.setLayoutX(300);
	        trNumLabel.setLayoutY(20);
	        trNumLabel.setFont(Font.font ("Verdana", 12));
	        numTr.setLayoutX(435);
	        numTr.setLayoutY(16);
	        if(numTrData == null){
	        	numTr.setValue("3");	
	        }
	        else{
	        	numTr.setValue(numTrData);
	        } 
	        //numTr.setOnShown(cgTrEventListener);
	        Label untrNumLabel = new Label("Untrained caregivers:");
	        untrNumLabel.setStyle("-fx-font-weight: bold");
	        untrNumLabel.setLayoutX(490);
	        untrNumLabel.setLayoutY(20);
	        untrNumLabel.setFont(Font.font ("Verdana", 12));
	        numUntr.setLayoutX(643);
	        numUntr.setLayoutY(16);
	        if(numUntrData == null){
	        	numUntr.setValue("3");	
	        }
	        else{
	        	numUntr.setValue(numUntrData);
	        }
	        Label crNumLabel = new Label("Number of carereceivers:");
	        crNumLabel.setStyle("-fx-font-weight: bold");
	        crNumLabel.setLayoutX(10);
	        crNumLabel.setLayoutY(120);
	        crNumLabel.setFont(Font.font ("Verdana", 12));
	        numCR.setLayoutX(235);
	        numCR.setLayoutY(116);
	        numCR.setOnAction(crChoiceEventListener);
	        if(numCRdata == null){
	        	numCR.setValue("10");
	        }
	        else{
	        	numCR.setValue(numCRdata);
	        }
	        Label mobNumLabel = new Label("Elderlies with mobility issue :");
	        mobNumLabel.setStyle("-fx-font-weight: bold");
	        mobNumLabel.setLayoutX(300);
	        mobNumLabel.setLayoutY(120);
	        mobNumLabel.setFont(Font.font ("Verdana", 12));
	        numMob.setLayoutX(505);
	        numMob.setLayoutY(116);
	        if(numMobData == null){
	        	 numMob.setValue("5");      	
	        }
	        else{
	        	numMob.setValue(numMobData);
	        }
	       
	        //Add a eventListener to add up both values and fil up the other automatically
	        Label demNumLabel = new Label("Elderlies with dementia:");
	        demNumLabel.setStyle("-fx-font-weight: bold");
	        demNumLabel.setLayoutX(570);
	        demNumLabel.setLayoutY(120);
	        demNumLabel.setFont(Font.font ("Verdana", 12));
	        numDem.setLayoutX(743);
	        numDem.setLayoutY(116);
	        if(numDemData == null){
	        	numDem.setValue("5");
	        }
	        else{
	        	numDem.setValue(numDemData);
	        }
	       
	        
	        Label durLabel = new Label("Duration of simulation in seconds(s):");
	        durLabel.setStyle("-fx-font-weight: bold");
	        durLabel.setLayoutX(10);
	        durLabel.setLayoutY(220);
	        durLabel.setFont(Font.font ("Verdana", 12));
	        simDur.setLayoutX(265);
	        simDur.setLayoutY(216);
	        
	        
	        Algo1.setLayoutX(400);
	        Algo1.setLayoutY(216);
	        Algo1.setStyle("-fx-font-weight: bold");
	        Algo1.setFont(Font.font ("Verdana", 12));
	        Algo1.setToggleGroup(group);
	        
	        if(radio_algo1== null || radio_algo1.equalsIgnoreCase("Algorithm 1")){
	        	Algo1.setSelected(true);
	        }
	        
	        else{
	        	Algo2.setSelected(true);
	        }

	        Algo2.setLayoutX(510);
	        Algo2.setLayoutY(216);
	        Algo2.setStyle("-fx-font-weight: bold");
	        Algo2.setFont(Font.font ("Verdana", 12));
	        Algo2.setToggleGroup(group);
	        
	        /*Listener for radio buttons*/
	        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
	            public void changed(ObservableValue<? extends Toggle> ov,
	                Toggle old_toggle, Toggle new_toggle) {
	                    if (group.getSelectedToggle() != null) {
	                        RadioButton chk = (RadioButton)old_toggle.getToggleGroup().getSelectedToggle();
	                        setRadio_algo1(chk.getText());
	                        //System.out.println("Selected Radio Button - "+chk.getText());
	                    }   
	                    else{
	                    	RadioButton chk = (RadioButton)new_toggle.getToggleGroup().getSelectedToggle();
	                    	setRadio_algo1(chk.getText());
	                    }
	                }

				
	        });
	        
	        
	        
	        if(durData == null){
	        	simDur.setValue("100");
	        }
	        else{
	        	simDur.setValue(durData);
	        }
	       
	        
	        save.setLayoutX(670);
	        save.setLayoutY(310);
	        save.setOnAction(saveExitEventListener);

	        
	        root3.getChildren().addAll(numCG,numCR,cgNumLabel,crNumLabel,trNumLabel,numTr,numUntr,untrNumLabel,mobNumLabel,demNumLabel,numMob,numDem
	        		,simDur,durLabel,save,Algo1,Algo2);
			
		}
    	
    };
    
    /*For Exit*/
    EventHandler<ActionEvent> exitEventListener
    = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			Platform.exit();
		}
    	
    };
    
    /*For CG choiceBox*/
    EventHandler<ActionEvent> cgChoiceEventListener
    = new EventHandler<ActionEvent>(){
		@SuppressWarnings("unchecked")
		@Override
		public void handle(ActionEvent event) {
			int cgTotalvalue = Integer.valueOf((String) numCG.getValue());
			if((cgTotalvalue % 2) == 0){
				numTr.setValue(Integer.toString((cgTotalvalue/2)));
				numUntr.setValue(Integer.toString(cgTotalvalue/2));
			}
			else{
				numTr.setValue(Integer.toString((int)Math.ceil(cgTotalvalue/2)));
				numUntr.setValue(Integer.toString(cgTotalvalue - (int)Math.ceil(cgTotalvalue/2)));
			}
		}
    	
    };
    
    /*For CRchoiceBox*/
    EventHandler<ActionEvent> crChoiceEventListener
    = new EventHandler<ActionEvent>(){
		@SuppressWarnings("unchecked")
		@Override
		public void handle(ActionEvent event) {
			int cgTotalvalue = Integer.valueOf((String) numCR.getValue());
			if((cgTotalvalue % 2) == 0){
				numMob.setValue(Integer.toString((cgTotalvalue/2)));
				numDem.setValue(Integer.toString(cgTotalvalue/2));
			}
			else{
				numMob.setValue(Integer.toString((int)Math.ceil(cgTotalvalue/2)));
				numDem.setValue(Integer.toString(cgTotalvalue - (int)Math.ceil(cgTotalvalue/2)));
			}
		}
    	
    };
    
    /*For save and exit choicebox*/
    EventHandler<ActionEvent> saveExitEventListener
    = new EventHandler<ActionEvent>(){
		@Override
		public void handle(ActionEvent event) {
			int cgTotalvalue = Integer.valueOf((String) numCG.getValue());
			int UntrTotalvalue = Integer.valueOf((String) numUntr.getValue());
			int TrTotalvalue = Integer.valueOf((String) numTr.getValue());
			int crTotalvalue = Integer.valueOf((String) numCR.getValue());
			int mobTotalvalue = Integer.valueOf((String) numMob.getValue());
			int demTotalvalue = Integer.valueOf((String) numDem.getValue());
			int dur = Integer.valueOf((String) simDur.getValue());
			
			if((UntrTotalvalue + TrTotalvalue > cgTotalvalue) || (UntrTotalvalue + TrTotalvalue < cgTotalvalue) ){
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Settings error");
				String s = "Total number of trained and untrained caregivers should be equal to the total number of caregivers.";
				alert.setContentText(s);
				alert.showAndWait();
			}
			else if((mobTotalvalue + demTotalvalue > crTotalvalue) || (mobTotalvalue + demTotalvalue < crTotalvalue) ) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Settings error");
				String s = "Total number care-receivers with dementia and mobility problem should be equal to total number of care-receivers.";
				alert.setContentText(s);
				alert.showAndWait();
				
			}
			else{
				numCG.setUserData(Integer.toString(cgTotalvalue));
				setNumCG(Integer.toString(cgTotalvalue));
				numUntr.setUserData(Integer.toString(UntrTotalvalue));
				setNumUntr(Integer.toString(UntrTotalvalue));
				numTr.setUserData(Integer.toString(TrTotalvalue));
				setNumTr(Integer.toString(TrTotalvalue));
				
				numCR.setUserData(Integer.toString(crTotalvalue));
				setNumCR(Integer.toString(crTotalvalue));
				numMob.setUserData(Integer.toString(mobTotalvalue));
				setNumMob(Integer.toString(mobTotalvalue));
				numDem.setUserData(Integer.toString(demTotalvalue));
				setNumDem(Integer.toString(demTotalvalue));
				
				simDur.setUserData(Integer.toString(dur));
				setSimDur(Integer.toString(dur));
				
		       
				
				Stage curStage = (Stage) numCG.getScene().getWindow();
	    		curStage.close();
			
			}
			
			 /*Listener for radio buttons*/
	        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
	            public void changed(ObservableValue<? extends Toggle> ov,
	                Toggle old_toggle, Toggle new_toggle) {
	                    if (group.getSelectedToggle() != null) {
	                        RadioButton chk = (RadioButton)old_toggle.getToggleGroup().getSelectedToggle();
	                        //Algo1.setUserData(chk.getText());
	                        setRadio_algo1(chk.getText());              
	                    }   
	                    else{
	                    	RadioButton chk = (RadioButton)new_toggle.getToggleGroup().getSelectedToggle();
	                    	setRadio_algo1(chk.getText());
	                    }
	                }

				
	        });
			
				
			
		}
    	
    };
    
   
    
    EventHandler<ActionEvent> btnLoadEventListener
    = new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent t) {
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);    
            //Show open file dialog
            File file = fileChooser.showOpenDialog(null);              
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                floorplan.setImage(image);
                floorplan.setPreserveRatio(true);
                startMain();
            } catch (IOException | ClassNotFoundException | SQLException ex) {
                Logger.getLogger(InitalizeGuardianAngel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    public void createTables() throws ClassNotFoundException, SQLException{
    	Class.forName("org.sqlite.JDBC");
		Connection con = DriverManager.getConnection("jdbc:sqlite:GuardianAngel.db");
		Statement state = con.createStatement();
		
		state.execute("create table Caregivers(id integer primary key autoincrement,name varchar(60), status varchar(60), mac varchar(60), serial integer, expertise varchar(60), timeInstance integer, maxed integer, totalcount integer, resetCount integer, prevX integer, prevY integer, curX integer, curY integer, distance integer, numOfRuns integer)");
		state.execute("create table Carereceivers(id integer primary key autoincrement,name varchar(60), status varchar(60), mac varchar(60), serial integer, classification varchar(60), thresholdcount integer, totalcount integer, timeInstance integer, curX integer, curY integer)");
		state.execute("create table MissedCalls(CRID integer, Count integer)");
		System.out.println("Caregivers and CareReceivers table created!");
		//insert record into table
		state.execute("insert into Caregivers values(null,'Caregiver A','false','D0B5C28DBB39', 12345, 'NIL', 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver B','false','D0B5C28DBB84', 12346, 'NIL', 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver C','false','D0B5C28E4087', 12347, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver D','false','D0B5C28E4369', 12348, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver E','false','D0B5C28E4401', 12349, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver F','false','D0B5C28E4402', 12350, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver G','false','D0B5C28DBB39', 12345, 'NIL', 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver H','false','D0B5C28DBB84', 12346, 'NIL', 0, 0, 0, 0, 0 ,0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver I','false','D0B5C28E4087', 12347, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver J','false','D0B5C28E4369', 12348, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver K','false','D0B5C28E4401', 12349, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
		state.execute("insert into Caregivers values(null,'Caregiver L','false','D0B5C28E4402', 12350, 'NIL', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
			
				
				
		state.execute("insert into Carereceivers values(null,'Carereceiver A','true','D0B5C28DBB39', 12345, 'NIL', 0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver B','true','D0B5C28DBB84', 12346, 'NIL',0 ,0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver C','true','D0B5C28E4087', 12347, 'NIL',0,0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver D','true','D0B5C28E4369', 12348, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver E','true','D0B5C28E4401', 12349, 'NIL', 0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver F','true','D0B5C28E4402', 12350, 'NIL', 0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver G','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver H','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver I','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver J','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver K','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)");
		state.execute("insert into Carereceivers values(null,'Carereceiver L','true','D0B5C28E4402', 12350, 'NIL',0, 0, 0, 0, 0)"); 	
    }
    
   public void startMain() throws SQLException, ClassNotFoundException {
    	/*canvas: For moving red and green dots
    	 * canvas2: For lines and squares
    	 * */
		//establish connection to database
		Class.forName("org.sqlite.JDBC");
		Connection con = DriverManager.getConnection("jdbc:sqlite:GuardianAngel.db");
		System.out.println("Database connected!");
		Statement state = con.createStatement();
		ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'Caregivers'");
		try{
			if(!res.next()){
				createTables();
			}
			else{
				state.execute("DROP TABLE 'Caregivers'");
				state.execute("DROP TABLE 'Carereceivers'");
				state.execute("DROP TABLE 'MissedCalls'");
				createTables();
				
			}
		btnLoad.setVisible(false);	
		}
		
		catch (SQLException e) { 
			e.printStackTrace();
		}
		finally {
			Timer time = new Timer(); // Instantiate Timer Object
			GuardianAngel ga = new GuardianAngel(this,con, time,3600);
			//GuardianAngel ga = new GuardianAngel(this,con, time,getSimDur());// Instantiate Scheduled Task class
			time.schedule(ga, 0, 1000); // Create Repetitively task for every 1 sec
			}
    }
  

	EventHandler<ActionEvent> seeResultsEventListener
    = new EventHandler<ActionEvent>(){ 
    	Stage stage, prevStage;
    	@Override
    	public void handle(ActionEvent arg0){
        prevStage = (Stage) btnLoad.getScene().getWindow();
    	prevStage.close();
    	stage = new Stage();
        stage.setTitle("Guardian Angel Simulator Results");
		stage.setWidth(1335);
		stage.setHeight(542);
        stage.setMaximized(true);
        holder3.getChildren().add(canvas4);
        holder3.setStyle("-fx-border-color: black");
        root2.getChildren().add(holder3);
    	 
    	holder3.setLayoutX(10);
    	holder3.setLayoutY(10);
    	 
    	Scene scene = new Scene(root2, 1335, 542);
    	stage.setScene(scene);
    	stage.show();	
    	 

    	 
    	 
    	 

    	}
    };
    
    @SuppressWarnings("resource")
	public void showResults(Connection con){
    	int numCG = getNumCG();
 
    	seeResults.setVisible(true);
    	floorplan.setImage(null);
    	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	gc2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	holder.setStyle("-fx-padding: 1px");	
    	int totalCg = 0;
    	gc4.strokeLine(667.5, 0, 667.5, 842);
    	Label CGtitle = new Label("Caregivers");
    	Label CRtitle = new Label("Care-receivers");
    	
       	CGtitle.setStyle("-fx-font-weight: bold");
       	CGtitle.setUnderline(true);
    	CGtitle.setLayoutX(270);
    	CGtitle.setLayoutY(10);
    	CGtitle.setFont(Font.font ("Verdana", 12));
    	
       	CRtitle.setStyle("-fx-font-weight: bold");
       	CRtitle.setUnderline(true);
    	CRtitle.setLayoutX(970);
    	CRtitle.setLayoutY(10);
    	CRtitle.setFont(Font.font ("Verdana", 12));

    	root2.getChildren().addAll(CGtitle,CRtitle);
    	
    	
    	
    	
    	
    	/*Get CG distance from DB*/
    	
    	try {
    		
			Statement state = con.createStatement();
			Statement state2 = con.createStatement();
			ResultSet res = state.executeQuery("select * from caregivers order by rowid asc limit " + numCG + "");
			while(res.next()){
				totalCg++;
			}
			String [] distArr = new String[totalCg];
			String [] expertise = new String[totalCg];
			String [] nameArr = new String[totalCg];
			int [] countArr = new int[totalCg];
			ResultSet res2 = state2.executeQuery("select * from caregivers order by rowid asc limit " + numCG + "" );
			int index = 0;
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			while(res2.next()){
				double distance = res2.getDouble("distance");
				distance = (distance*2.54/96)/100;
				String dist = df.format(distance);
				String ex = res2.getString("expertise");
				String name = res2.getString("name");
				int totalCount = res2.getInt("totalcount");
				distArr[index] = dist; //convert pixels to meters
				expertise[index] = ex; //stores all the expertise of the CG
				nameArr[index] = name; //stores all the name
				countArr[index] = totalCount; //stores the count
				index++;
			}
			
			int yInc = 0;
			int yInc2 = 0;
			for(int i =0; i<totalCg;i++){
				if(expertise[i].isEmpty()){
					return;
				}
				if(expertise[i].equals("skilled") && i <= 5){
					gc4.drawImage(trained_64, 0, 35+yInc);	
					createLabel(67,45+yInc,"Name: " + nameArr[i]);
					createLabel(67,65+yInc,"Distance moved: " + distArr[i] + "m");
					createLabel(67,85+yInc,"Cases handled: " + countArr[i]);
					
				}
				else if(expertise[i].equals("skilled") && i >= 5){
					gc4.drawImage(trained_64, 325, 35+yInc2);	
					createLabel(392,45+yInc2,"Name: " + nameArr[i]);
					createLabel(392,65+yInc2,"Distance moved: " + distArr[i] + "m");
					createLabel(392,85+yInc2,"Cases handled: " + countArr[i]);
				}
				else if(expertise[i].equals("unskilled") && i <= 5)
				{
					gc4.drawImage(untrained_64, 0, 35+yInc);
					createLabel(67,45+yInc,"Name: " + nameArr[i]);
					createLabel(67,65+yInc,"Distance moved: " + distArr[i] + "m");
					createLabel(67,85+yInc,"Cases handled: " + countArr[i]);
				}
				else{
					gc4.drawImage(untrained_64, 325, 35+yInc2);
					createLabel(392,45+yInc2,"Name: " + nameArr[i]);
					createLabel(392,65+yInc2,"Distance moved: " + distArr[i] + "m");
					createLabel(392,85+yInc2,"Cases handled: " + countArr[i]);
					
				}
				
				if(i>5){
					yInc2+=115;
				}
				
				yInc+=115;
			}
			createCRLabel(con);
    	} 
    	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @SuppressWarnings("resource")
	public void createCRLabel(Connection con){
       	int numCR = getNumCR();
		
/*Care-receivers label generation*/
		Statement state3;
		try {
			state3 = con.createStatement();
		
		Statement state4 = con.createStatement();
		Statement state5 = con.createStatement();
		
		ResultSet res3 = state3.executeQuery("select * from carereceivers order by rowid asc limit " + numCR + "");
		
		int totalCr = 0;
		while(res3.next()){		
			totalCr ++;
		}
		String [] classification = new String[totalCr];
		String [] crNameArr = new String[totalCr];
		int [] numCountArr = new int[totalCr];
		String []numMissedArr = new String[totalCr];
		int [] crIntArr = new int[totalCr];
		ResultSet res4 = state4.executeQuery("select * from carereceivers left outer join Missedcalls on "
				+ "carereceivers.id = MissedCalls.CRID order by id asc limit " + numCR + "");
		int crIndex = 0;
		while(res4.next()){
			String classify = res4.getString("classification");
			String name = res4.getString("name");
			int totalCount = res4.getInt("totalcount");
			int id = res4.getInt("id");
			String missedCount = res4.getString("count");
			classification[crIndex] = classify; //stores all the classification of the Cr
			crNameArr[crIndex] = name; //stores all the name
			numCountArr[crIndex] = totalCount; //stores the count
			crIntArr[crIndex] = id; // stores the id;
			if(missedCount == null){
				numMissedArr[crIndex] = "0"; 
			}
			else{
				numMissedArr[crIndex] = missedCount; //stores the missedcount	
			}
			
			crIndex++;
		}
		

		int crInc = 0;
		int crInc2 = 0;
		for(int i =0; i<totalCr;i++){
			if(classification[i].isEmpty()){
				return;
			}
			if(classification[i].equals("mobility problem") && i <= 5){
				gc4.drawImage(mobility_64, 665, 35+crInc);	
				createLabel(732,45+crInc,"Name: " + crNameArr[i]);
				createLabel(732,65+crInc,"Late/Missed: " + numMissedArr[i]);
				createLabel(732,85+crInc,"Anomaly count: " + numCountArr[i]);
				
			}
			
			else if(classification[i].equals("mobility problem") && i >= 5){
				gc4.drawImage(mobility_64, 990, 35+crInc2);	
				createLabel(1057,45+crInc2,"Name: " + crNameArr[i]);
				createLabel(1057,65+crInc2,"Late/Missed: " + numMissedArr[i]);
				createLabel(1057,85+crInc2,"Anomaly count: " + numCountArr[i]);
			}
			else if(classification[i].equals("dementia") && i <= 5)
			{
				gc4.drawImage(dementia_64, 665, 35+crInc);
				createLabel(732,45+crInc,"Name: " + crNameArr[i]);
				createLabel(732,65+crInc,"Late/Missed: " + numMissedArr[i]);
				createLabel(732,85+crInc,"Anomaly count: " +  numCountArr[i]);
			}
			else{
				gc4.drawImage(dementia_64, 990, 35+crInc2);
				createLabel(1057,45+crInc2,"Name: " + crNameArr[i]);
				createLabel(1057,65+crInc2,"Late/Missed: " + numMissedArr[i]);
				createLabel(1057,85+crInc2,"Anomaly count: " + numCountArr[i]);
				
			}
			
			if(i>5){
				crInc2+=115;
			}
			
			crInc = crInc + 115;
		}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }
    
    public void createLabel(double x, double y, String text){
    	Label cgLabel = new Label(text);
    	cgLabel.setStyle("-fx-font-weight: bold");
    	cgLabel.setLayoutX(x);
    	cgLabel.setLayoutY(y);
    	cgLabel.setFont(Font.font ("Verdana", 12));
    	root2.getChildren().add(cgLabel);
    	
    }

}
	
	


