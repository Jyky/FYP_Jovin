package Controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Boundaries.InitalizeGuardianAngel;
import Entity.Obstacle;

public class ObstacleController {
	Obstacle newObstacle;
	private ArrayList<Obstacle> obstaclelist = new ArrayList<Obstacle>();
	
	
	
	public void addObstacle(double x, double y, double width, double height, double angle){
		newObstacle = new Obstacle(x, y, width, height, angle);
		obstaclelist.add(newObstacle);	
		
	}
	
	public ArrayList<Obstacle> getObstaclelist() {
		return obstaclelist;
	}
	public void printobsList(){
		System.out.println("==========Toilets location==========");
		for(int i=0;i<obstaclelist.size();i++){
			 System.out.println(obstaclelist.get(i));}
	}
	public void mapObstacle(InitalizeGuardianAngel ma, Connection con){ 
		URL url;
		HttpURLConnection conn;
		//Setting Toilet's location
		try {
			url = new URL("http://127.0.0.1:8889/toilet");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());}
			 BufferedReader br2 = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			 JSONParser parser2 = new JSONParser();
			 String temp_TOILET[] = new String[4];
			 Object obj2 = parser2.parse(br2);
			 JSONObject jsonObject2 = (JSONObject) obj2;
			 //For toilet
			 for(int i = 1; i<=jsonObject2.keySet().size();i++){	
				 int tmpcount = 0;
				 JSONArray ID = (JSONArray)jsonObject2.get(String.valueOf(i));
				 Iterator<Double> iterator = ID.iterator();  
				 while (iterator.hasNext()) {    
					 temp_TOILET[tmpcount] = "" + iterator.next();
					 tmpcount++;	
					 }
				 addObstacle(Double.parseDouble(temp_TOILET[0]),Double.parseDouble(temp_TOILET[1]),Double.parseDouble(temp_TOILET[3]) , Double.parseDouble(temp_TOILET[2]), 0);		 
		}  
		}
			 catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
			 }		  
		}


