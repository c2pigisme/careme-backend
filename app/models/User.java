package models;

import java.util.Date;
import java.util.List;
import java.util.Map;


import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;

@Entity("profiles")
public class User extends Model {
	public String name;
	public String email;
	public String password;
	public String gender;
	public String age;
	public String height;
	public String weight;
	public String bloodType;
	public String phoneNumber;
	public Date registrationDate;
	
	@Embedded
	public List<String> bmiRecords;
	@Embedded
	public Map<String, CMR> cares;
	@Embedded
	public List<CMR> shares;
	@Embedded
	public List<CMR> pending;
}
