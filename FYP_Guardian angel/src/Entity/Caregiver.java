package Entity;
import java.util.concurrent.atomic.AtomicInteger;

public class Caregiver {
	private int id;
	private String beaconMAC;
	private double pos[][] = new double[1][2]; //get from server, while the rest get from DB.
	private String duty;
	private String name;
	private String classification;
	static AtomicInteger nextId = new AtomicInteger();
	private int timeInstance;
	private int maxed;
	private double prevPos[][] = new double[1][2]; 

	
	
	public Caregiver(int id, String name, String duty, String expert,  int serial, String mac, int maxed) {
		super();
		this.id = id;
		this.name = name;
		this.duty = duty;
		this.classification = expert;
		this.beaconMAC = mac;
		this.maxed = maxed;
		}


	public Caregiver(){
		
		
	}
	public int getCaregiver_id() {
		return id;
	}
	
	public String getCaregiver_name(){
		return name;
	}
	
	public double[][] getPos(){
		return pos;
		
	}
	public void setXY(double x, double y){
		this.pos[0][0] = x;
		this.pos[0][1] = y;
	}
	
	public void setPrevXY(double x, double y){
		this.prevPos[0][0] = x;
		this.prevPos[0][1] = y;
	}
	public void setStatus(String status){
		this.duty = status;
	}
	
	public String getduty(){
		return duty;
		
	}
	public String getMAC(){
		return beaconMAC;
	}
	
	public int getTimeInstance(){
		return timeInstance;
	}
	public void setTimeInstance(int timeInstance){
		this.timeInstance = timeInstance;
	}
	
	public String toString() {
		
	    return "ID: "+ id + " Name:" + name + " Position:"+  pos[0][0] +","+pos[0][1] + " Status:" + duty + " MAC:" + beaconMAC
	    		+ " Expertise: "+ classification;
	}
	

	public String getClassification(){
		return classification;
	}
	public void setTracking(String tracker){
	}
	
	public int getMaxed(){
		return maxed;
	}
	
	public void setMaxed(int maxed){
		this.maxed = maxed;
	}
	

}
