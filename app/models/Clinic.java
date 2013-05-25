package models;

import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;

@Entity("clinics")
public class Clinic extends Model {
	public double[] loc;
	//public double lat;
	//public double lng;
	public String name;
	public String addr;
	public String phone;
	
	public Clinic(double[]loc, String name, String addr, String phone) {
		this.loc = loc;
		this.name = name;
		this.addr = addr;
		this.phone = phone;
	}
}
