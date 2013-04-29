package models;

import java.util.Date;

import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;


@Entity("txn")
public class Transaction extends Model {
	public String source;
	public String target;
	public String type;
	public String msg;
	public Date logDate;
	
	public Transaction(String source, String target, String type) {
		this.source = source;
		this.target = target;
		this.type = type;
		logDate = new Date();
	}
	public void setMsg(String msg)  {
		this.msg = msg;
	}
}
