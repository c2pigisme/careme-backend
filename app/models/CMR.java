package models;

import java.util.Date;

import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;

//CMR = CareMeRecord
@Entity
public class CMR extends Model {
	public String phone;
	public String status;
	public Date requestDate = new Date();
	
	public CMR(String phone, String status) {
		this.phone = phone;
		this.status = status;
	}
}
