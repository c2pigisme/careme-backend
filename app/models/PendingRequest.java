package models;

import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;


@Entity("pendingreq")
public class PendingRequest extends Model {
	public CMR cmr;
	public String phoneNumber;
	public PendingRequest(String phoneNumber, CMR cmr) {
		this.cmr = cmr;
		this.phoneNumber = phoneNumber;
	}
}
