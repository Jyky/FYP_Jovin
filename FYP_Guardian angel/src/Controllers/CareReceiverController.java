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

public class CareReceiverController {
	private ArrayList<CareReceiver> CrList = new ArrayList<CareReceiver>(); 
	private ArrayList<CareReceiver> UnAvailCrList = new ArrayList<CareReceiver>(); 

	CareReceiver newUser;
	public void addCareReceiver(int id, String name, int serial, String mac, String status, String classification) {
	    newUser = new CareReceiver(id, name, serial, mac, status, classification);
	     
	    if(status.equals("false"))
	    	CrList.add(newUser);
	
	    else
	    	UnAvailCrList.add(newUser);
	}
	
	public void mapCareReceivever(InitalizeGuardianAngel ma, Connection con){
		URL url;
		HttpURLConnection conn;
		ArrayList<CareReceiver> crList = new ArrayList<CareReceiver>();
		crList = getCrList();
		double[][] updateArray = new double [crList.size()][2];
		String[] updateClassArray = new String [crList.size()];
		String [] updateNameArray = new String[crList.size()];
		Statement state;
		
		try {
			state = con.createStatement();
			url = new URL("http://127.0.0.1:8889/location");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());}
			//Care-receiver
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			JSONParser parser = new JSONParser();  
			String temp_CR[]= new String[3];
			Object obj = parser.parse(br);
			JSONObject jsonObject = (JSONObject) obj;
			Set<String> keys = jsonObject.keySet();
			List<String> list = new ArrayList<String>(keys);
			for(int i = 0; i<jsonObject.keySet().size();i++){	//Care-receiver 1 to 10
				if(i > crList.size() - 1) {
					break;
					}

				int tmpcount = 0;
				int jsonget = crList.get(i).getCareReceiver_id() - 1;
				JSONArray ID = (JSONArray)jsonObject.get(String.valueOf(jsonget));
				Iterator<Double> iterator = ID.iterator();  
				while (iterator.hasNext()) {    
					temp_CR[tmpcount] = "" + iterator.next();
					tmpcount++;	
					}  
				if(crList.get(i).getCareReceiver_id() == jsonget+1){
					int id = crList.get(i).getCareReceiver_id();
					crList.get(i).setXY(Double.parseDouble(temp_CR[0]), Double.parseDouble(temp_CR[1]));
					crList.get(i).setPrevXY(Double.parseDouble(temp_CR[0]), Double.parseDouble(temp_CR[1]));
					crList.get(i).setTracking(temp_CR[2]);
					state.execute("update carereceivers set curX =' " + Double.parseDouble(temp_CR[0]) + "'" +"  where id =' " + id + "'"  );	
					state.execute("update carereceivers set cury =' " + Double.parseDouble(temp_CR[1]) + "'" +"  where id =' " + id + "'"  );	
					String classification = crList.get(i).getClassification();
					String crName = crList.get(i).getCareReceiver_name();
					updateArray [i][0] = Double.parseDouble(temp_CR[0]); //populate Coordinate x to update array
					updateArray [i][1] = Double.parseDouble(temp_CR[1]); //populate Coordinate y to update array	
					updateClassArray[i] = classification;
					updateNameArray[i] = crName;
					}
				}
				ma.updateCR(updateArray, updateClassArray,updateNameArray,0);
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


public void mapCareReceivever2(InitalizeGuardianAngel ma, Connection con){
	ArrayList<CareReceiver> unavailcrList = new ArrayList<CareReceiver>();
	unavailcrList = getUnavailCrList();
	ResultSet res;
	double[][] updateArray = new double [unavailcrList.size()][2];
	String [] updateClassArray = new String [unavailcrList.size()];
	String [] updateNameArray = new String[unavailcrList.size()];
	Statement state;
	if(unavailcrList.size() > 0){	
		try {
			state = con.createStatement();
			res = state.executeQuery("SELECT * FROM Carereceivers WHERE status = 'true'");
			int i = 0;
			while(res.next()){
				if(i > unavailcrList.size() - 1) {
					break;
					}
				int id = res.getInt("id");
				double prevX = res.getInt("curX");
				double prevY = res.getInt("curY");
				if(unavailcrList.get(i).getCareReceiver_id() == id){
					unavailcrList.get(i).setPrevXY(prevX, prevY);
					String classification = unavailcrList.get(i).getClassification();
					String crName = unavailcrList.get(i).getCareReceiver_name();
					updateArray [i][0] = prevX; //populate Coordinate x to update array
					updateArray [i][1] = prevY; //populate Coordinate y to update array
					updateClassArray [i] = classification;
					updateNameArray[i] = crName;
					
				}	 
		
			i++;}
			if(updateArray.length != 0  && updateClassArray.length !=0){
				ma.updateCR(updateArray, updateClassArray,updateNameArray,1);
			}
		}	
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	else{
		//System.out.println("Nothing in CR2");
		return;
		
	}
		
}

public ArrayList<CareReceiver> getCrList(){
	return CrList;
}
public ArrayList<CareReceiver> getUnavailCrList(){
	return UnAvailCrList;
}





public void printAvailableCarereceiver(){
System.out.println("==========Available CareReceivers==========");
for (int i=0; i<CrList.size();i++){
	System.out.println(CrList.get(i));
}
}

}
