package models;

import java.util.Date;

import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;

//CMR = CareMeRecord
@Entity
public class CMR extends Model {
	public String targetPhoneNumber;
	public int status;
	public String displayName;
	public String email;
	public Date requestDate = new Date();
	
	public CMR(String phone, int status) {
		this.targetPhoneNumber = phone;
		this.status = status;
	}
	public CMR(String phone, String email, int status) {
		this.email = email;
		this.targetPhoneNumber = phone;
		this.status = status;
	}
}
