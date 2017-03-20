package Entity;

public class Obstacle {
	private double [][] StartXY = new double[1][2];
	private double width;
	private double height;
	private double [][] XY1 = new double[1][2];
	private double [][] XY2 = new double[1][2];
	private double [][] XY3 = new double[1][2];
	private double [][] XY4 = new double[1][2];
	private double [][] center = new double[1][2];
	private double angle;

	


public Obstacle(double x, double y, double width, double height, double angle){
	super();
	angle = Math.toRadians(angle);
	/*Before rotation*/
	//top left corner of rectangle (Start point)
	this.StartXY[0][0] = x - width/2;
	this.StartXY[0][1] = y + height/2;
	// btm left corner
	this.XY1[0][0] = x - width/2;
	this.XY1[0][1] = y - height/2;
	//top right corner 
	this.XY2[0][0] = x + width/2;
	this.XY2[0][1] = y + height/2;
	//btm right corner 
	this.XY3[0][0] = x + width/2;
	this.XY3[0][1] = y - height/2;
	//center of rectangle
	this.center[0][0] = x;
	this.center[0][1]  = y;
	
	//translate point to origin
	double tempX1 = x - this.center[0][0]; //start x
	double tempY1 = y - this.center[0][1]; //start y
	double tempX2 = this.XY1[0][0]  - this.center[0][0]; //btm left x
	double tempY2 = this.XY1[0][1] - this.center[0][1]; //btm left y
	double tempX3 = this.XY2[0][0] - this.center[0][0]; //top right x
	double tempY3 = this.XY2[0][1] - this.center[0][1]; //top right y
	double tempX4 = this.XY3[0][0] - this.center[0][0]; //btm right x
	double tempY4 = this.XY3[0][1] - this.center[0][1]; //btm right y
	this.height = height;
	this.width = width;
	this.setAngle(angle);
	
	//Apply rotation
	double rotatedX1 = tempX1*Math.cos(angle) - tempY1*Math.sin(angle);
	double rotatedY1 = tempX1*Math.sin(angle) + tempY1*Math.cos(angle);
	double rotatedX2 = tempX2*Math.cos(angle) - tempY2*Math.sin(angle);
	double rotatedY2 = tempX2*Math.sin(angle) + tempY2*Math.cos(angle);
	double rotatedX3 = tempX3*Math.cos(angle) - tempY3*Math.sin(angle);
	double rotatedY3 = tempX3*Math.sin(angle) + tempY3*Math.cos(angle);
	double rotatedX4 = tempX4*Math.cos(angle) - tempY4*Math.sin(angle);
	double rotatedY4 = tempX4*Math.sin(angle) + tempY4*Math.cos(angle);
	
	//translate back (After rotation)
	this.XY1[0][0] = rotatedX1 + this.center[0][0];
	this.XY1[0][1] = rotatedY1 + this.center[0][1];
	this.XY2[0][0] = rotatedX2 + this.center[0][0];
	this.XY2[0][1] = rotatedY2 + this.center[0][1];
	this.XY3[0][0] = rotatedX3 + this.center[0][0];
	this.XY3[0][1] = rotatedY3 + this.center[0][1];
	this.XY4[0][0] = rotatedX4 + this.center[0][0];
	this.XY4[0][1] = rotatedY4 + this.center[0][1];
	
}

public double[][] getStartXY(){
	
	return StartXY;
}
public double[][] getXY1(){
	
	return XY1;
}
public double[][] getXY2(){
	
	return XY2;
}
public double[][] getXY3(){
	
	return XY3;
}
public double[][] getXY4(){
	
	return XY4;
}

public double[][] getCenter(){
	
	return center;
}

public double getWidth(){
	
	return width;
}

public double getHeight(){
	
	return height;
}

public double getAngle() {
	return angle;
}

public void setAngle(double angle) {
	this.angle = angle;
}

public String toString() {
	
    return "Start:" + StartXY[0][0] +","+StartXY[0][1] + " Point 1:" + XY1[0][0] + "," + XY1[0][1]
    		+ " Point 2:" + XY2[0][0] + "," + XY2[0][1] + " Point 3:" + XY3[0][0] + "," + XY3[0][1] +
    		 " Point 4:" + XY4[0][0] + "," + XY4[0][1] + "," + " Center:" + center[0][0] + "," + center[0][1]; 
}


}