package Controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Boundaries.InitalizeGuardianAngel;
import Entity.CareReceiver;
import Entity.Caregiver;

public class CaregiverController {
	private ArrayList<Caregiver> caregiverlist =  new ArrayList<Caregiver>();
	private ArrayList<Caregiver> unavailcaregiverlist =  new ArrayList<Caregiver>();
	private Caregiver newUser;

	
	
	public void addCaregiver(int id, String name, String duty, String expert,  int serial, String mac, int maxed) { 
	    newUser = new  Caregiver(id, name, duty, expert,  serial, mac, maxed);
	    if(duty.equalsIgnoreCase("true")){
	    	caregiverlist.add(newUser);
	    }
	    else
	    	unavailcaregiverlist.add(newUser);
	}
	
	public ArrayList<Caregiver> getCgList(){
		return caregiverlist;
	}
	
	public void printAvailableCaregiver(){
		System.out.println("==========Available Caregivers==========");
		for (int i=0; i<caregiverlist.size();i++){
			System.out.println(caregiverlist.get(i));
		}
		}
	

	
	public ArrayList<Caregiver> getUnAvailCgList(){
		return unavailcaregiverlist;
	}
	


	public void addUnavailCG(Caregiver cg) { 
		unavailcaregiverlist.add(cg);
		//System.out.println("testing");
	}
	
	
	public void mapCaregiver(InitalizeGuardianAngel ma, Connection con){ //for available CGs
		URL url;
		HttpURLConnection conn;
		ArrayList<Caregiver> cgList = new ArrayList<Caregiver>();
		cgList = getCgList();
		double[][] updateArray = new double [cgList.size()][2];
		String [] updateClassArray = new String [cgList.size()];
		String [] updateNameArray = new String[cgList.size()];
		Statement state;
		
		
		
		
		try {
			state = con.createStatement();
			url = new URL("http://127.0.0.1:8889/cglocation");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());}
			//Care-giver
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			JSONParser parser = new JSONParser();  
			String temp_CG[]= new String[3];
			Object obj = parser.parse(br);
			JSONObject jsonObject = (JSONObject) obj;
			Set<String> keys = jsonObject.keySet();
			List<String> list = new ArrayList<String>(keys);
			for(int i = 0; i<jsonObject.keySet().size();i++){	//Care-giver 1 to 6
				if(i > cgList.size() - 1) {
					break;
					}
		
				int tmpcount = 0;
				int jsonget = cgList.get(i).getCaregiver_id() - 1;
				JSONArray ID = (JSONArray)jsonObject.get(String.valueOf(jsonget));
				Iterator<Double> iterator = ID.iterator();  
				while (iterator.hasNext()) {    
					temp_CG[tmpcount] = "" + iterator.next();
					tmpcount++;	
					}  
				
				if(cgList.get(i).getCaregiver_id() == jsonget+1){
					int id = cgList.get(i).getCaregiver_id();
					cgList.get(i).setXY(Double.parseDouble(temp_CG[0]), Double.parseDouble(temp_CG[1]));
					cgList.get(i).setPrevXY(Double.parseDouble(temp_CG[0]), Double.parseDouble(temp_CG[1]));
					cgList.get(i).setTracking(temp_CG[2]);
					state.execute("update caregivers set prevX =' " + Double.parseDouble(temp_CG[0]) + "'" +"  where id =' " + id + "'"  );	
					state.execute("update caregivers set prevy =' " + Double.parseDouble(temp_CG[1]) + "'" +"  where id =' " + id + "'"  );	
					
					/*For calculation of distance*/
					Statement state2 = con.createStatement();
					Statement state3 = con.createStatement();
					ResultSet res = state2.executeQuery("SELECT * FROM Caregivers " +  "where id =' " + id + "'");
					int numRuns = res.getInt("numOfRuns");
					if(numRuns==0){
						state.execute("update caregivers set curX =' " + Double.parseDouble(temp_CG[0])+ "'" +"  where id =' " + id + "'"  );	
						state.execute("update caregivers set cury =' " + Double.parseDouble(temp_CG[1]) + "'" +"  where id =' " + id + "'"  );	
					}
					else if(numRuns >0){
						double curX =  Double.parseDouble(temp_CG[0]);
						double curY = Double.parseDouble(temp_CG[1]);
						ResultSet res2 = state3.executeQuery("SELECT * FROM Caregivers " +  "where id =' " + id + "'");
						double oldX = res2.getDouble("curX");
						double oldY = res2.getDouble("curY");
						double oldDistance = res2.getDouble("distance");
						double distance = Math.sqrt(Math.pow((curX-oldX), 2)+ Math.pow((curY-oldY), 2));
						double updateDist = oldDistance + distance;
						state.execute("update caregivers set distance =' " + updateDist + "'" +"  where id =' " + id + "'"  );	
						
					}
						
					
					
					/*For updating of canvas*/
					String classification = cgList.get(i).getClassification();
					String cgName = cgList.get(i).getCaregiver_name();
					updateArray [i][0] = Double.parseDouble(temp_CG[0]); //populate Coordinate x to update array
					updateArray [i][1] = Double.parseDouble(temp_CG[1]); //populate Coordinate y to update array	
					updateClassArray [i] = classification;
					updateNameArray[i] = cgName;
					//System.out.println("normal mapping");
				}
				}
				ma.updateCG(updateArray, updateClassArray, updateNameArray, 0);
			}
		catch (MalformedURLException e) {
			e.printStackTrace();
			} 
		catch (IOException e) {
			e.printStackTrace();
			} 
		catch (ParseException e) {
			e.printStackTrace();
			} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	  
		}

	/*for unavailCGs*/
	public void mapCaregiver2(InitalizeGuardianAngel ma, Connection con){
		ArrayList<Caregiver> unavailcgList = new ArrayList<Caregiver>();
		ArrayList<Caregiver> availcgList = new ArrayList<Caregiver>();
		unavailcgList = getUnAvailCgList();
		availcgList = getCgList();
		ResultSet res;
		double[][] updateArray = new double [unavailcgList.size()][2];
		String [] updateClassArray = new String [unavailcgList.size()];
		String [] updateNameArray = new String[unavailcgList.size()];
		Statement state,state2;
		
		if(unavailcgList.size() > 0){	
			try {
				state = con.createStatement();
				res = state.executeQuery("SELECT * FROM Caregivers WHERE status = 'false'");
				int i = 0;
				while(res.next()){
					if(i > unavailcgList.size() - 1) {
						break;
						}
					int id = res.getInt("id");
					double prevX = res.getInt("prevX"); //allocated anomaly's position X
					double prevY = res.getInt("prevY"); //allocated anomaly's position Y
					double curX = res.getDouble("curX");
					double curY = res.getDouble("curY");
					double oldDistance = res.getDouble("distance");
					
					if(unavailcgList.get(i).getCaregiver_id() == id){
						state2 = con.createStatement();
						double distance = Math.sqrt(Math.pow((curX-prevX), 2)+ Math.pow((curY-prevY), 2));
						double updateDist = oldDistance + distance;
						state2.execute("update caregivers set distance =' " + updateDist + "'" +"  where id =' " + id + "'"  );
						unavailcgList.get(i).setPrevXY(prevX, prevY);
						String classification = unavailcgList.get(i).getClassification();
						updateArray [i][0] = prevX + 10; //populate Coordinate x to update array
						updateArray [i][1] = prevY; //populate Coordinate y to update array
						updateClassArray [i] = classification;
						String cgName = unavailcgList.get(i).getCaregiver_name();
						updateNameArray[i] = cgName;
						System.out.println("mapping anomaly!");
					}	 
			i++;
				}				
				if(updateArray.length !=0 && updateClassArray.length !=0){
					System.out.println("prevX: " + updateArray [0][0]);
					System.out.println("prevY: " + updateArray [0][1]);
					System.out.println("Classification: " + updateClassArray [0]); 
					System.out.println("Length: " + updateArray.length);
					System.out.println("Length of updateClass: " + updateClassArray.length);
					if(availcgList.size()>0 && unavailcgList.size() > 0){
						ma.updateCG(updateArray, updateClassArray, updateNameArray, 1);
						
					}
					else{
						ma.updateCG(updateArray, updateClassArray, updateNameArray, 0);
						
					}
				
						
					
					
				}
			}	
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			//System.out.println("Nothing in CG2");
			return;
		}
			
	}

	public int getMaxed(){
		return newUser.getMaxed();
	}
	
	public void setMaxed(int maxed){
		newUser.setMaxed(maxed); 
	}
	
}
