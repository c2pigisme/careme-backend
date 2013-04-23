package models;

import java.util.Date;

import com.google.code.morphia.annotations.Entity;


import play.modules.morphia.Model;

@Entity("sessions")
public class Session extends Model {

	public String session;
	public String email;
	public Date loginDate;
	
	public Session(String session, String email) {
		this.session = session;
		this.email = email;
		this.loginDate = new Date();
	}
	
}
