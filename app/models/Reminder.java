package models;

import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;

@Entity
public class Reminder extends Model {

	public String css;
	public String dosage;
	public long eventms;
	public String medicine;
	public String note;
	public String unit;
	public String fromPhoneNumber;
	
}
