package Entity;
import java.util.concurrent.atomic.AtomicInteger;

public class CareReceiver {
	private static final String DEMENTIA = "Dementia";
	private static final String MOBILITY = "Mobility-problem";
	private double pos[][] = new double[1][2];
	private double prevPos[][] = new double[1][2];
	private int id;
	private String beaconMAC;
	private String name;
	static AtomicInteger nextId = new AtomicInteger();
	private String status;
	private static final String[] CARE_CLASS = {DEMENTIA,MOBILITY};
	private String classification;
	private String tracking;


	
	public CareReceiver(int id, String name, int serial, String mac, String status, String classification) {
		super();
	    this.id = id;
	    this.name = name;
	    this.beaconMAC = mac;
	    this.status =status;
	    this.classification = classification;
		
	}
	
	public CareReceiver(){
		
	}
	
	public String getMAC(){
		return beaconMAC;
	}
	public void setXY(double x, double y){
		this.pos[0][0] = x;
		this.pos[0][1] = y;
	}
	
	public void setPrevXY(double x, double y){
		this.prevPos[0][0] = x;
		this.prevPos[0][1] = y;
	}
	
	public void setTracking(String tracker){
		this.tracking = tracker;
	}
	
	public void setStatus(String status){
		this.tracking = status;
	}
	
	public String getCareReceiver_name(){
		return name;
	}
	
	public int getCareReceiver_id() {
		return id;
	}
	
	public double[][] getPos(){
		return pos;
		
	}
	public String[] getCareClass(){
		return CARE_CLASS;
	}
	
	
	public void setClass(String cgClass){
		this.classification = cgClass;
	}
	
	public String getClassification(){
		return classification;
	}
	
public String toString() {
		
	    return "ID: "+ id + " Name:" + name + " Position:"+ pos[0][0] +","+pos[0][1] + " MAC: " + beaconMAC
	    		+" Anomaly: " + status+ " Classifcation: " + classification +" Status: " + tracking;
	}





}

