package Boundaries;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import Controllers.AnomaliesCheckAndTaskController;
import Controllers.CareReceiverController;
import Controllers.CaregiverController;
import Controllers.ObstacleController;

public class GuardianAngel extends TimerTask  {
	private InitalizeGuardianAngel init;	
	private Connection con;
	private static int count = 0;
	private int duration;
	private Timer timer;
	ObstacleController obc;
	AnomaliesCheckAndTaskController an;
	CaregiverController cgc;
	CareReceiverController crc;
	
	
	public GuardianAngel(InitalizeGuardianAngel init, Connection con, Timer timer, int duration){
		this.init = init;
		this.con = con;
		this.timer = timer;
		this.duration = duration;
	}
	
	
	@Override
    public void run()
    {	 
		count++;
    if (count >= duration) { /*count value decided by the settings*/
    	timer.cancel();
        init.showResults(con);
       // System.out.println(mainApp.getNumCG());
        return;
    }

		obc = new ObstacleController();
		an = new AnomaliesCheckAndTaskController();
		cgc = new CaregiverController();
		crc = new CareReceiverController();
		URL url2;
		HttpURLConnection conn2;
		//Retrieve from DB
		ResultSet res,res2;
		String algoSelect = init.getRadio_algo1();
		if(algoSelect == null){
			algoSelect = "Algorithm 1";
		}

		try {
			//Instantiate Care-givers and Care-receivers
			Statement state = con.createStatement();
			Statement state3 = con.createStatement();
			res = state.executeQuery("SELECT * FROM Caregivers");	
			int loopCount = 0;
			int trCount = 1;
			int numCG = init.getNumCG();
			int numTr = init.getNumTr();
			while(res.next()){
				if(numCG==loopCount){
					break;
				}
				Statement state2 = con.createStatement();
				int id = res.getInt("id");
				/*To add number of skilled (tr) and unskilled caregivers*/
				if(trCount <= numTr){
					state2.execute("update caregivers set expertise ='skilled' where id =' " + id + "'"  );	
				}
				else{
					state2.execute("update caregivers set expertise ='unskilled' where id =' " + id + "'"  );
				}
				
				int timeStampCount = res.getInt("timeInstance");
				int resetCount = res.getInt("resetCount");
				int maxed = res.getInt("maxed");
				if(timeStampCount != 0){ //cg that are already allocated a task
					timeStampCount = timeStampCount-1;
					state2.execute("update caregivers set timeInstance =' " + timeStampCount + "'" +"  where id =' " + id + "'"  );	
					if(timeStampCount == 0){ //for cases where timeInstance changes from 1 to 0 after subtracting
						state2.execute("update caregivers set status = 'true'  where id =' " + id + "'"  );
						cgc.addCaregiver(res.getInt("id"), res.getString("name"), "true", res.getString("expertise"),  res.getInt("serial"), res.getString("mac"), res.getInt("maxed"));
					}
					else{
						cgc.addCaregiver(res.getInt("id"), res.getString("name"), res.getString("status"), res.getString("expertise"),  res.getInt("serial"), res.getString("mac"), res.getInt("maxed"));								
						}
				}
				else if(timeStampCount == 0) { //available CG
					//if(resetCount == 0 && maxed == 1){
					//	state2.execute("update caregivers set maxed =' " + 0 + "'" +"  where id =' " + id + "'"  );
					//	cgc.setMaxed(0);
				//	}
					state2.execute("update caregivers set status = 'true'  where id =' " + id + "'"  );		
					cgc.addCaregiver(res.getInt("id"), res.getString("name"), "true", res.getString("expertise"),  res.getInt("serial"), res.getString("mac"), res.getInt("maxed"));								
				}
				
				loopCount++;
				trCount++;

				
			}

			state3 = con.createStatement();
			res2 =  state3.executeQuery("SELECT * FROM Carereceivers");
			loopCount = 0;
			int numCR = init.getNumCR();
			int numMob = init.getNumMob();
			int algoSel;
			int mobCount = 1;
			while(res2.next()){
				if(loopCount==numCR){
					break;
				}
				state = con.createStatement();
				Statement state2 = con.createStatement();
				int timeStampCount = res2.getInt("timeInstance");
				int id = res2.getInt("id");
				if(mobCount <= numMob){
					state.execute("update carereceivers set classification ='mobility problem' where id =' " + id + "'"  );	
				}
				else{
					state.execute("update carereceivers set classification ='dementia' where id =' " + id + "'"  );		
				}
	
				if(timeStampCount != 0){
					timeStampCount = timeStampCount-1;
					state.execute("update carereceivers set timeInstance =' " + timeStampCount + "'" +"  where id =' " + id + "'"  );
					if(timeStampCount == 0){ //for cases where timeInstance changes from 1 to 0 after subtracting
						state.execute("update carereceivers set status = 'false'  where id =' " + id + "'"  );
						crc.addCareReceiver(res2.getInt("id"),  res2.getString("name"), res2.getInt("serial"),  res2.getString("mac"),"false",res2.getString("classification")); 
					}
					else{
						crc.addCareReceiver(res2.getInt("id"),  res2.getString("name"), res2.getInt("serial"),  res2.getString("mac"),res2.getString("status"),res2.getString("classification")); 
						}
					
					
					
				}
				else{ //status will decide if CR is avail or not
					state.execute("update carereceivers set status = 'false'  where id =' " + id + "'"  );
					res = state2.executeQuery("SELECT * FROM Carereceivers where id =' " + id + "'");	
					crc.addCareReceiver(res.getInt("id"),  res.getString("name"), res.getInt("serial"),  res.getString("mac"),res.getString("status"),res.getString("classification")); 
					}
				
				loopCount++;
				mobCount++;
				}
			
			 obc.mapObstacle(init, con);		
			 cgc.mapCaregiver(init, con);
			 cgc.mapCaregiver2(init,con);
			 crc.mapCareReceivever(init, con);
			 crc.mapCareReceivever2(init,con);
			 cgc.printAvailableCaregiver();
			 crc.printAvailableCarereceiver();
			 obc.printobsList();
			 an.checkAnomalies(crc, obc, cgc, con, init,algoSelect);	
			 Statement state2 = con.createStatement();
			 Statement state4 = con.createStatement();
			 ResultSet res3 = state2.executeQuery("SELECT * FROM Caregivers");
			 int availCg = 0;
			 while(res3.next()){ 
				 int id = res3.getInt("id");
				 int numofRun = res3.getInt("numOfRuns");
				 String status = res3.getString("status");
				 if(status.equals("true")){
					 availCg++;			 
				 }
				 int newNum = numofRun+1;
				 state4.execute("update caregivers set numOfRuns =' " + newNum + "'" +"  where id =' " + id + "'"  );
			 }
			 init.setCgText(String.valueOf(availCg));
			 
			 ResultSet res4 = state4.executeQuery("SELECT * FROM Carereceivers");
			 int availCr = 0;
			 while(res4.next()){ 
				 int id = res4.getInt("id");
				 String status = res4.getString("status");
				 if(status.equals("false")){
					 availCr++;			 
				 }
			 }
			 init.setCrText(String.valueOf(availCr));
			 
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
    }
}
