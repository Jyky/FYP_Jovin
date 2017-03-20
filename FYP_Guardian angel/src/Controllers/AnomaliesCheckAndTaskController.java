package Controllers;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import Boundaries.InitalizeGuardianAngel;
import Entity.CareReceiver;
import Entity.Caregiver;
import Entity.Obstacle;

public class AnomaliesCheckAndTaskController {
	private ArrayList<CareReceiver> anomalieslist =  new ArrayList<CareReceiver>();
	String [][] taskAssignmentArr = new String [10][10]; //to store the task assignment
	
	public void checkAnomalies (CareReceiverController crc, ObstacleController ob, CaregiverController cg, Connection con, InitalizeGuardianAngel ma, String algoSelect){
		AnomaliesCheckAndTaskController an = this;
		ArrayList<Obstacle> oblist = ob.getObstaclelist();
		ArrayList<CareReceiver> crList = crc.getCrList();
		ResultSet res;
		double [][] crPos = new double[1][2];
		double [][] obPosCenter = new double[1][2];
		for (int i=0; i < crList.size();i++){
			for(int j=0; j <oblist.size(); j++){
				crPos = crList.get(i).getPos();
				obPosCenter = oblist.get(j).getCenter(); // center	
				if((crPos[0][0] <= obPosCenter[0][0] + oblist.get(j).getWidth()/2 && 
						crPos[0][0] >= obPosCenter[0][0] - oblist.get(j).getWidth()/2 &&
						crPos[0][1] <= obPosCenter[0][1] + oblist.get(j).getHeight()/2 && 
						crPos[0][1] >= obPosCenter[0][1] -  oblist.get(j).getHeight()/2)
						//check all 4 bounds
				){
					
					try {
						Statement state = con.createStatement();
						res = state.executeQuery("SELECT * FROM Carereceivers WHERE id =' " + crList.get(i).getCareReceiver_id() + "'" );
						int newcount = res.getInt("thresholdcount");
						int totalcount = res.getInt("totalcount");
						int countadd = newcount +1;
						totalcount = totalcount+1;
						state.execute("update carereceivers set thresholdcount =' " + countadd + "'" +"  where id =' " + crList.get(i).getCareReceiver_id() + "'"  );
						newcount++;
						System.out.println("anomaly detected! Threshold value:"+newcount);			
						if(newcount == 3){	
							state.execute("update carereceivers set totalcount =' " + totalcount + "'" +"  where id =' " + crList.get(i).getCareReceiver_id() + "'"  );
							state.execute("update carereceivers set thresholdcount ='0' where id =' " + crList.get(i).getCareReceiver_id() + "'"  );
							an.addAnomalies(crList.get(i));	
							System.out.println("added anomaly");
							ma.setActText("Anomaly Detected!");
							newcount = 0; //reset newcount
							}
						else if (newcount>3){
							System.out.println("newcount > 3 Late handling of anomaly! Threshold value:"+newcount);
							an.addAnomalies(crList.get(i));	
							System.out.println("added anomaly");
							System.out.println("size of ano list: " + anomalieslist.size());
							ma.setActText("Anomaly Detected!");
						}
						newcount = 0; //reset newcount
						} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						}
					
					}									
			}
			}
		showAnomalies();
		if(algoSelect.equalsIgnoreCase("Algorithm 1") || algoSelect == null){
			assignTask(cg,an,con,ob,crc, ma);	
		}
		else{
			assignTask2(cg,an,con,ob,crc, ma);
		}
		
		}	
	
	/*Task assignment based on euclidean distance*/
	public void assignTask(CaregiverController cg, AnomaliesCheckAndTaskController an, Connection con, ObstacleController ob, CareReceiverController cr, InitalizeGuardianAngel ma){
		ArrayList<CareReceiver> anomaliesList = an.getAnomalieslist();
		ArrayList<Caregiver> availcgList = cg.getCgList();
		//create a no.of caregiver by no. of anomalies matrix
		double [][] dist = new double [availcgList.size()][an.getAnomalieslist().size()];
		double [] classification = new double [availcgList.size()];
		int [] dutyArr = new int [availcgList.size()];
		List<Integer> missedArr = new ArrayList<Integer>();
		ResultSet res,res2;
		int count = 0;
		double maxDist = Math.sqrt((0-1335)*(0-1335)+ (0-542)*(0-542)); //max dist for a 1335x542 floorplan
		double [][] scoreArr = new double [availcgList.size()][anomaliesList.size()];
		try {
			Statement state4 = con.createStatement();
			res = state4.executeQuery("select * from caregivers");
			while(res.next()){
				count++;	}
			res.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		int [] timeArr = new int [count]; //initialize array by number of rows
		
		try {
			Statement state5 = con.createStatement();
			Statement state6 = con.createStatement();
			res2 = state6.executeQuery("select * from MissedCalls");
			while(res2.next()){
				missedArr.add(res2.getInt("CRID"));
			}
			
		}catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		if(anomaliesList.size() == 0 || availcgList.size()== 0){	
		Statement state5, state6;
		System.out.println("Before: " + Arrays.toString(missedArr.toArray()));
		try {
			state5 = con.createStatement();
			state6 = con.createStatement();
			for(int i = 0; i <anomaliesList.size();i++){
				int check = anomaliesList.get(i).getCareReceiver_id();
				if( missedArr.contains(check) ){
					res2 = state6.executeQuery("select * from MissedCalls where CRID =' " + check +"'");
					int prevCount = res2.getInt("Count");
					prevCount+=1;
					state5.execute("update MissedCalls set Count =' " + prevCount + "'" +" where CRID =' " + check + "'" );
					//return;
				}
					
					else{
						missedArr.add(check);
						System.out.println("Added: " + check);
						ma.setActText("Late handling of: " + anomaliesList.get(i).getCareReceiver_name());
						System.out.println("Late handling of: " + anomaliesList.get(i).getCareReceiver_name());
						state5.execute("insert into MissedCalls values(" + check + " , 1)");
					}
					
				}
			
			System.out.println("After: " + Arrays.toString(missedArr.toArray()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return;
			
		}
		for(int i =0; i<availcgList.size();i++){
			for(int j = 0; j<anomaliesList.size();j++){
				double x1 = availcgList.get(i).getPos()[0][0];
			    double x2 = anomaliesList.get(j).getPos()[0][0];
			    double y1 = availcgList.get(i).getPos()[0][1];
			    double y2 = anomaliesList.get(j).getPos()[0][1];
			    //min max normalize distance to 0 n 1
			    dist[i][j]= (Math.sqrt((x1-x2)*(x1-x2)+ (y1-y2)*(y1-y2)) - 0) / (maxDist - 0);		    
			}
		}
		//Populate Expertise and duty array for each care-giver
		for(int i=0;i<dist.length;i++){
			if(availcgList.get(i).getClassification().equals("skilled")){
				classification[i] = 1;
			}
			if(availcgList.get(i).getClassification().equals("unskilled")){
				classification[i] = 0.5;
			}
			if(availcgList.get(i).getduty().equals("true")){
				dutyArr[i] = 1;
			}
			if(availcgList.get(i).getduty().equals("false")){
				dutyArr[i] = 0;
			}
		}
			//Find max of the max among the list of anomalies	
			scoreArr = calculateScore(classification,dutyArr,dist,anomaliesList);	
			/*Make a copy of scoreArr*/
			double [][] scoreArrCopy = new double [availcgList.size()][anomaliesList.size()];
			for(int i = 0; i < availcgList.size(); i++)
			{
			  double[] aMatrix = scoreArr[i];
			  int   aLength = aMatrix.length;
			  scoreArrCopy[i] = new double[aLength];
			  System.arraycopy(aMatrix, 0, scoreArrCopy[i], 0, aLength);
			}

			/*Sort the array by Anomaly*/
			java.util.Arrays.sort(scoreArrCopy, new java.util.Comparator<double[]>() {
			    public int compare(double[] b, double[] a) {
			        return Double.compare(b[0], a[0]);
			    }
			});
			//find maximum score 
		double max = Integer.MIN_VALUE;
		int index, index_ArrayList, copy_index = 0, copy_index_ArrayList = 0; 
		for(int i =0; i<anomaliesList.size();i++){
			for(int j=0; j<availcgList.size();j++){
				if(scoreArrCopy[j][i] > max){
					max = scoreArrCopy[j][i];//update new max
					copy_index = j;
					copy_index_ArrayList = i;
				}
			}
		}	
		int [][] oriPos = findPosition(scoreArr, max, anomaliesList, availcgList); //find position of max in original
		index = oriPos[0][0]; //index of availcgList
		index_ArrayList = oriPos[0][1]; //index of anomaliesList
		int CrID = anomaliesList.get(index_ArrayList).getCareReceiver_id();
		int CgID = availcgList.get(index).getCaregiver_id();	
		int timeInstance, timeInstance2 = 0;
		String CGexpert = availcgList.get(index).getClassification();
		taskAssignmentArr[index][index_ArrayList] = String.valueOf(CgID) + "," +String.valueOf(CrID);
		System.out.println("Scores of caregiver to anomaly:");
	    String[][] split = new String[1][availcgList.size()];
	    split[0] = (Arrays.deepToString(scoreArr)).split(Pattern.quote("], [")); //split at the comma
		System.out.println("             Anomaly ID:" + CrID);
		
		int test = 0;
	    for(int row = 0; row < availcgList.size(); row++){
	    
	        System.out.println("CG ID:"+ availcgList.get(test).getCaregiver_id() + " " + split[0][row]);
	        test++;
	    }		
		System.out.println("Identified most suitable Caregiver for anomaly ID " + CrID + ":");
		String cgName = availcgList.get(index).getCaregiver_name();
		ma.setActText("Identified most suitable Caregiver for anomaly ID " + CrID + ": " + cgName);

		System.out.println(availcgList.get(index));	
		if(CGexpert.equalsIgnoreCase("skilled")){
			timeInstance = 10;
		}
		else{
			timeInstance = 15;
		}

		try {
			Statement state = con.createStatement();
			Statement state2 = con.createStatement();
			Statement state3 = con.createStatement();
			Statement state4 = con.createStatement();
			res = state4.executeQuery("SELECT * FROM Carereceivers WHERE id =' " + CrID + "'" ); //assign identified anomaly position as unavail CG position
			int prevX = res.getInt("curX");
			int prevY = res.getInt("curY");
			state.execute("update caregivers set prevX =' " + prevX + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set prevY =' " + prevY + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set status = 'false' where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
		    // get totalcount of selected CG and add    
			res = state.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			int totalcount = res.getInt("totalcount");
			int resetCount = res.getInt("resetCount");
			res.close();
			totalcount+=1;
			resetCount+=1;
			res = state2.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			state.execute("update caregivers set totalcount =' " + totalcount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set resetCount =' " + resetCount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			resetCount = res.getInt("totalcount"); //get updated resetCount
			
			if (resetCount >= 2){
				int newCount = resetCount-resetCount;
				state.execute("update caregivers set resetCount =' " + newCount + "'" + " where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );	
			}

			//update timeArr
			count = 0;
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
			}
	
			state.execute("update carereceivers set status = 'true' where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			state.execute("update caregivers set timeInstance = ' " + timeInstance + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set timeInstance = ' " + timeInstance + "'" + " where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			res.close();
			res = state3.executeQuery("select * from caregivers");
			//update timeArr
			count = 0;
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
			}
			res.close();
			System.out.println("Time array:" + Arrays.toString(timeArr));

	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		availcgList.get(index).setTimeInstance(timeInstance);
		anomaliesList.remove(index_ArrayList); //remove assigned anomaly from list 
		availcgList.get(index).setStatus("false"); //change status of CG to false after assignment of task
		cg.addUnavailCG(availcgList.get(index)); //it actually works
		dutyArr[index] = 0; //update duty array of the CG to 0 (unavailable)
		
		//re-calculate score if there is more than 1 anomaly
		int sizeTest = anomaliesList.size();
		while(anomaliesList.size() > 0){
			boolean containsAll0 = IntStream.of(dutyArr).allMatch(x -> x == 0); //check if duty array is all 0
			if(containsAll0){ 
				System.out.println("No. of Missing session:" + sizeTest);
				//sizeTest-=1;
				//if(sizeTest != 0){
				//	assignTaskUnavail(cg,an,con,timeArr);
					//continue;
				//}
			}
			else {
				scoreArr = calculateScore(classification,dutyArr,dist,anomaliesList);
			}
			/*Make a copy of scoreArr*/
			double [][] scoreArrCopy2 = new double [availcgList.size()][anomaliesList.size()];
			for(int i1 = 0; i1 < availcgList.size(); i1++)
			{
			  double[] aMatrix = scoreArr[i1];
			  int   aLength = aMatrix.length;
			  scoreArrCopy2[i1] = new double[aLength];
			  System.arraycopy(aMatrix, 0, scoreArrCopy2[i1], 0, aLength);
			}

			/*Sort the array by Anomaly*/
			java.util.Arrays.sort(scoreArrCopy2, new java.util.Comparator<double[]>() {
			    public int compare(double[] b, double[] a) {
			        return Double.compare(b[0], a[0]);
			    }
			});
			
			
			max = Integer.MIN_VALUE;
			for(int i1 =0; i1<anomaliesList.size();i1++){
				for(int j1=0; j1<availcgList.size();j1++){
					if(scoreArrCopy2[j1][i1] > max){
						max = scoreArrCopy2[j1][i1];		//update new max
						copy_index = j1;
						copy_index_ArrayList = i1;
				}
			}
		}
			oriPos = findPosition(scoreArr, max, anomaliesList, availcgList); //find the position of max in original
			index = oriPos[0][0];
			index_ArrayList = oriPos[0][1];			
		String CGexpert2 = availcgList.get(index).getClassification();	
		if(max != 0){ //"0" score will not be considered
		CrID = anomaliesList.get(index_ArrayList).getCareReceiver_id();
		taskAssignmentArr[index][index_ArrayList] = String.valueOf(availcgList.get(index).getCaregiver_id()) + "," +String.valueOf(anomaliesList.get(index_ArrayList).getCareReceiver_id());
		System.out.println("Scores of caregiver to anomaly:" + Arrays.deepToString(scoreArr));
		System.out.println("Identified most suitable Caregiver for anomaly ID " + CrID + ":");
		System.out.println(availcgList.get(index));
		availcgList.get(index).setStatus("false");
		dutyArr[index] = 0; //set to unavailable
		availcgList.get(index).setMaxed(1);
		if(CGexpert2.equalsIgnoreCase("skilled")){
			timeInstance2 = 10;
			}
		else{
			timeInstance2 = 15;
		}
		/*DB update*/
		try {
			 // get totalcount of selected CG and add  
			Statement state = con.createStatement();
			Statement state2 = con.createStatement();
			Statement state3 = con.createStatement();
			Statement state4 = con.createStatement();
			res = state4.executeQuery("SELECT * FROM Carereceivers WHERE id =' " + CrID + "'" );
			int prevX = res.getInt("curX");
			int prevY = res.getInt("curY");
			state.execute("update caregivers set prevX =' " + prevX + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set prevY =' " + prevY + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			res = state.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			int totalcount = res.getInt("totalcount");
			int setMax = 1;
			totalcount+=1;
			state.execute("update caregivers set maxed =' " + setMax + "'" + " where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );		
			state.execute("update caregivers set totalcount =' " + totalcount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			res.close();
			state.execute("update caregivers set status = 'false' where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set status = 'true' where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			state.execute("update caregivers set timeInstance = ' " + timeInstance2 + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set timeInstance = ' " + timeInstance2 + "'" + " where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			res.close();
			res = state3.executeQuery("select * from caregivers");
			count = 0;
			//update timeArr
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
				
			}
			System.out.println("Time array2:" + Arrays.toString(timeArr));
		
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		anomaliesList.remove(index_ArrayList);
		System.out.println("Size of anomaly list: " + anomaliesList.size());

				}
		
		}
	}

	/*Task assignment based on euclidean distance*/
	public void assignTask2(CaregiverController cg, AnomaliesCheckAndTaskController an, Connection con, ObstacleController ob, CareReceiverController cr, InitalizeGuardianAngel ma){
		ArrayList<CareReceiver> anomaliesList = an.getAnomalieslist();
		ArrayList<Caregiver> availcgList = cg.getCgList();
		//create a no.of caregiver by no. of anomalies matrix
		double [][] dist = new double [availcgList.size()][an.getAnomalieslist().size()];
		double [] classification = new double [availcgList.size()];
		int [] dutyArr = new int [availcgList.size()];
		List<Integer> missedArr = new ArrayList<Integer>();
		ResultSet res,res2;
		int count = 0;
		double maxDist = Math.sqrt((0-1335)*(0-1335)+ (0-542)*(0-542)); //max dist for a 1335x542 floorplan
		double [][] scoreArr = new double [availcgList.size()][anomaliesList.size()];
		try {
			Statement state4 = con.createStatement();
			res = state4.executeQuery("select * from caregivers");
			while(res.next()){
				count++;	}
			res.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		int [] timeArr = new int [count]; //initialize array by number of rows
		
		try {
			Statement state6 = con.createStatement();
			res2 = state6.executeQuery("select * from MissedCalls");
			while(res2.next()){
				missedArr.add(res2.getInt("CRID"));
			}
			
		}catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		
		if(anomaliesList.size() == 0 || availcgList.size()== 0){	
		Statement state5, state6;
		System.out.println("Before: " + Arrays.toString(missedArr.toArray()));
		try {
			state5 = con.createStatement();
			state6 = con.createStatement();
			for(int i = 0; i <anomaliesList.size();i++){
				int check = anomaliesList.get(i).getCareReceiver_id();
				if( missedArr.contains(check) ){
					res2 = state6.executeQuery("select * from MissedCalls where CRID =' " + check +"'");
					int prevCount = res2.getInt("Count");
					prevCount+=1;
					state5.execute("update MissedCalls set Count =' " + prevCount + "'" +" where CRID =' " + check + "'" );
					//return;
				}
					
					else{
						missedArr.add(check);
						System.out.println("Added: " + check);
						ma.setActText("Late handling of: " + anomaliesList.get(i).getCareReceiver_name());
						System.out.println("Late handling of: " + anomaliesList.get(i).getCareReceiver_name());
						state5.execute("insert into MissedCalls values(" + check + " , 1)");
					}
					
				}
			
			System.out.println("After: " + Arrays.toString(missedArr.toArray()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return;
			
		}
		for(int i =0; i<availcgList.size();i++){
			for(int j = 0; j<anomaliesList.size();j++){
				double x1 = availcgList.get(i).getPos()[0][0];
			    double x2 = anomaliesList.get(j).getPos()[0][0];
			    double y1 = availcgList.get(i).getPos()[0][1];
			    double y2 = anomaliesList.get(j).getPos()[0][1];
			    //min max normalize distance to 0 n 1
			    dist[i][j]= (Math.sqrt((x1-x2)*(x1-x2)+ (y1-y2)*(y1-y2)) - 0) / (maxDist - 0);		    
			}
		}
		//Populate Expertise and duty array for each care-giver
		for(int i=0;i<dist.length;i++){
			
			if(availcgList.get(i).getduty().equals("true")){
				dutyArr[i] = 1;
			}
			if(availcgList.get(i).getduty().equals("false")){
				dutyArr[i] = 0;
			}
		}
			//Find max of the max among the list of anomalies	
			scoreArr = calculateScore_dist(dutyArr,dist,anomaliesList);	
			/*Make a copy of scoreArr*/
			double [][] scoreArrCopy = new double [availcgList.size()][anomaliesList.size()];
			for(int i = 0; i < availcgList.size(); i++)
			{
			  double[] aMatrix = scoreArr[i];
			  int   aLength = aMatrix.length;
			  scoreArrCopy[i] = new double[aLength];
			  System.arraycopy(aMatrix, 0, scoreArrCopy[i], 0, aLength);
			}

			/*Sort the array by Anomaly*/
			java.util.Arrays.sort(scoreArrCopy, new java.util.Comparator<double[]>() {
			    public int compare(double[] b, double[] a) {
			        return Double.compare(b[0], a[0]);
			    }
			});
			//find maximum score 
		double max = Integer.MIN_VALUE;
		int index, index_ArrayList, copy_index = 0, copy_index_ArrayList = 0; 
		for(int i =0; i<anomaliesList.size();i++){
			for(int j=0; j<availcgList.size();j++){
				if(scoreArrCopy[j][i] > max){
					max = scoreArrCopy[j][i];//update new max
					copy_index = j;
					copy_index_ArrayList = i;
				}
			}
		}	
		int [][] oriPos = findPosition(scoreArr, max, anomaliesList, availcgList); //find position of max in original
		index = oriPos[0][0]; //index of availcgList
		index_ArrayList = oriPos[0][1]; //index of anomaliesList
		int CrID = anomaliesList.get(index_ArrayList).getCareReceiver_id();
		int CgID = availcgList.get(index).getCaregiver_id();	
		int timeInstance, timeInstance2 = 0;
		String CGexpert = availcgList.get(index).getClassification();
		taskAssignmentArr[index][index_ArrayList] = String.valueOf(CgID) + "," +String.valueOf(CrID);
		System.out.println("Scores of caregiver to anomaly:");
	    String[][] split = new String[1][availcgList.size()];
	    split[0] = (Arrays.deepToString(scoreArr)).split(Pattern.quote("], [")); //split at the comma
		System.out.println("             Anomaly ID:" + CrID);
		
		int test = 0;
	    for(int row = 0; row < availcgList.size(); row++){
	    
	        System.out.println("CG ID:"+ availcgList.get(test).getCaregiver_id() + " " + split[0][row]);
	        test++;
	    }		
		System.out.println("Identified most suitable Caregiver for anomaly ID " + CrID + ":");
		String cgName = availcgList.get(index).getCaregiver_name();
		ma.setActText("Identified most suitable Caregiver for anomaly ID " + CrID + ": " + cgName);

		System.out.println(availcgList.get(index));	
		if(CGexpert.equalsIgnoreCase("skilled")){
			timeInstance = 10;
		}
		else{
			timeInstance = 15;
		}

		try {
			Statement state = con.createStatement();
			Statement state2 = con.createStatement();
			Statement state3 = con.createStatement();
			Statement state4 = con.createStatement();
			res = state4.executeQuery("SELECT * FROM Carereceivers WHERE id =' " + CrID + "'" ); //assign identified anomaly position as unavail CG position
			int prevX = res.getInt("curX");
			int prevY = res.getInt("curY");
			state.execute("update caregivers set prevX =' " + prevX + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set prevY =' " + prevY + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set status = 'false' where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
		    // get totalcount of selected CG and add    
			res = state.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			int totalcount = res.getInt("totalcount");
			int resetCount = res.getInt("resetCount");
			res.close();
			totalcount+=1;
			resetCount+=1;
			res = state2.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			state.execute("update caregivers set totalcount =' " + totalcount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set resetCount =' " + resetCount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			resetCount = res.getInt("totalcount"); //get updated resetCount
			
			if (resetCount >= 2){
				int newCount = resetCount-resetCount;
				state.execute("update caregivers set resetCount =' " + newCount + "'" + " where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );	
			}

			//update timeArr
			count = 0;
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
			}
	
			state.execute("update carereceivers set status = 'true' where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			state.execute("update caregivers set timeInstance = ' " + timeInstance + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set timeInstance = ' " + timeInstance + "'" + " where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			res.close();
			res = state3.executeQuery("select * from caregivers");
			//update timeArr
			count = 0;
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
			}
			res.close();
			System.out.println("Time array:" + Arrays.toString(timeArr));

	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		availcgList.get(index).setTimeInstance(timeInstance);
		anomaliesList.remove(index_ArrayList); //remove assigned anomaly from list 
		availcgList.get(index).setStatus("false"); //change status of CG to false after assignment of task
		cg.addUnavailCG(availcgList.get(index)); //it actually works
		dutyArr[index] = 0; //update duty array of the CG to 0 (unavailable)
		
		//re-calculate score if there is more than 1 anomaly
		int sizeTest = anomaliesList.size();
		while(anomaliesList.size() > 0){
			boolean containsAll0 = IntStream.of(dutyArr).allMatch(x -> x == 0); //check if duty array is all 0
			if(containsAll0){ 
				System.out.println("No. of Missing session:" + sizeTest);
				//sizeTest-=1;
				//if(sizeTest != 0){
				//	assignTaskUnavail(cg,an,con,timeArr);
					//continue;
				//}
			}
			else {
				scoreArr = calculateScore_dist(dutyArr,dist,anomaliesList);
			}
			/*Make a copy of scoreArr*/
			double [][] scoreArrCopy2 = new double [availcgList.size()][anomaliesList.size()];
			for(int i1 = 0; i1 < availcgList.size(); i1++)
			{
			  double[] aMatrix = scoreArr[i1];
			  int   aLength = aMatrix.length;
			  scoreArrCopy2[i1] = new double[aLength];
			  System.arraycopy(aMatrix, 0, scoreArrCopy2[i1], 0, aLength);
			}

			/*Sort the array by Anomaly*/
			java.util.Arrays.sort(scoreArrCopy2, new java.util.Comparator<double[]>() {
			    public int compare(double[] b, double[] a) {
			        return Double.compare(b[0], a[0]);
			    }
			});
			
			
			max = Integer.MIN_VALUE;
			for(int i1 =0; i1<anomaliesList.size();i1++){
				for(int j1=0; j1<availcgList.size();j1++){
					if(scoreArrCopy2[j1][i1] > max){
						max = scoreArrCopy2[j1][i1];		//update new max
						copy_index = j1;
						copy_index_ArrayList = i1;
				}
			}
		}
			oriPos = findPosition(scoreArr, max, anomaliesList, availcgList); //find the position of max in original
			index = oriPos[0][0];
			index_ArrayList = oriPos[0][1];			
		String CGexpert2 = availcgList.get(index).getClassification();	
		if(max != 0){ //"0" score will not be considered
		CrID = anomaliesList.get(index_ArrayList).getCareReceiver_id();
		taskAssignmentArr[index][index_ArrayList] = String.valueOf(availcgList.get(index).getCaregiver_id()) + "," +String.valueOf(anomaliesList.get(index_ArrayList).getCareReceiver_id());
		System.out.println("Scores of caregiver to anomaly:" + Arrays.deepToString(scoreArr));
		System.out.println("Identified most suitable Caregiver for anomaly ID " + CrID + ":");
		System.out.println(availcgList.get(index));
		availcgList.get(index).setStatus("false");
		dutyArr[index] = 0; //set to unavailable
		availcgList.get(index).setMaxed(1);
		if(CGexpert2.equalsIgnoreCase("skilled")){
			timeInstance2 = 10;
			}
		else{
			timeInstance2 = 15;
		}
		/*DB update*/
		try {
			 // get totalcount of selected CG and add  
			Statement state = con.createStatement();
			Statement state2 = con.createStatement();
			Statement state3 = con.createStatement();
			Statement state4 = con.createStatement();
			res = state4.executeQuery("SELECT * FROM Carereceivers WHERE id =' " + CrID + "'" );
			int prevX = res.getInt("curX");
			int prevY = res.getInt("curY");
			state.execute("update caregivers set prevX =' " + prevX + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update caregivers set prevY =' " + prevY + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			res = state.executeQuery("SELECT * FROM Caregivers WHERE id =' " + availcgList.get(index).getCaregiver_id() + "'" );
			int totalcount = res.getInt("totalcount");
			int setMax = 1;
			totalcount+=1;
			state.execute("update caregivers set maxed =' " + setMax + "'" + " where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );		
			state.execute("update caregivers set totalcount =' " + totalcount + "'" +"  where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			res.close();
			state.execute("update caregivers set status = 'false' where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set status = 'true' where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			state.execute("update caregivers set timeInstance = ' " + timeInstance2 + "'" +" where id =' " + availcgList.get(index).getCaregiver_id() + "'"  );
			state.execute("update carereceivers set timeInstance = ' " + timeInstance2 + "'" + " where id =' " + anomaliesList.get(index_ArrayList).getCareReceiver_id() + "'"  );
			res.close();
			res = state3.executeQuery("select * from caregivers");
			count = 0;
			//update timeArr
			while(res.next()){
				int timeStampCount = res.getInt("timeInstance");
				timeArr[count] = timeStampCount;
				count++;
				
			}
			System.out.println("Time array2:" + Arrays.toString(timeArr));
		
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		anomaliesList.remove(index_ArrayList);
		System.out.println("Size of anomaly list: " + anomaliesList.size());

				}
		
		}
	}
	
	
	public double[][] calculateScore(double[] classification,int[] dutyArr, double[][] dist, ArrayList<CareReceiver> anomaliesList ){
		double [][] scoreArr = new double [classification.length][anomaliesList.size()];
		//formula for score is expertise x duty x 1/distance
		for(int i =0; i<classification.length;i++){
			for(int j = 0; j<anomaliesList.size();j++){
				scoreArr[i][j] = 1/dist[i][j] * dutyArr[i] * classification[i] ;	
			}
			
		}
		return scoreArr;	
	}
	
	/*Will use calculateScore_2 to determine score when all care-givers are busy */
	public double[][] calculateScore_2(double[] classification,int[] timeArr, double[][] dist, ArrayList<CareReceiver> anomaliesList ){
		double [][] scoreArr = new double [classification.length][anomaliesList.size()];
		//formula for score is expertise x duty x distance
		for(int i =0; i<classification.length;i++){
			for(int j = 0; j<anomaliesList.size();j++){
				scoreArr[i][j] = 1/dist[i][j] * 1/timeArr[i] * classification[i] ;	
			}
			
		}
		return scoreArr;	
	}
	
	public double[][] calculateScore_dist(int[] dutyArr, double[][] dist, ArrayList<CareReceiver> anomaliesList ){
		double [][] scoreArr = new double [dist.length][anomaliesList.size()];
		//formula for score is expertise x duty x distance
		for(int i =0; i<dist.length;i++){
			for(int j = 0; j<anomaliesList.size();j++){
				scoreArr[i][j] = 1/dist[i][j] * dutyArr[i];	
			//* dutyArr[i] * classification[i] //investigate duty and classification
			}
			
		}
		return scoreArr;	
	}
	
	public double[][] calculateScore_dist_2(int[] timeArr, double[][] dist, ArrayList<CareReceiver> anomaliesList ){
		double [][] scoreArr = new double [dist.length][anomaliesList.size()];
		//formula for score is expertise x duty x distance
		for(int i =0; i<dist.length;i++){
			for(int j = 0; j<anomaliesList.size();j++){
				scoreArr[i][j] = 1/dist[i][j] * timeArr[i];	
			//* dutyArr[i] * classification[i] //investigate duty and classification
			}
			
		}
		return scoreArr;	
	}
	
	public void showAnomalies(){
		if(anomalieslist.size() != 0)
		{
			for(int i = 0; i<anomalieslist.size();i++){
				System.out.println(anomalieslist.get(i));
		}
			System.out.println("============================================================================");
		}
		else
			System.out.println("No anomalies");
	}
	
	public ArrayList<CareReceiver> getAnomalieslist(){
	
		return anomalieslist;
	}
	
	public void addAnomalies(CareReceiver cr) {
		anomalieslist.add(cr);

	}
	
	public int [][] findPosition(double [][] scoreArr, double target,ArrayList<CareReceiver> anomaliesList, ArrayList<Caregiver> availcgList){
		int [][] pos = new int[1][2];
		/*return position of value*/
		for ( int i = 0; i < anomaliesList.size(); i++ ) {
		    for ( int j = 0; j < availcgList.size(); j++ ) {
		        if ( scoreArr[j][i] == target ) {
		        	pos[0][0] = j;
		        	pos[0][1] = i;
		        } 
		     
		    }
		}
		return pos;
	
	}
	
	
	
}
	

