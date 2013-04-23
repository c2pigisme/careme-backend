package models;

import java.util.List;


import com.google.code.morphia.annotations.Entity;

import play.modules.morphia.Model;

@Entity("profiles")
public class User extends Model {
	public String name;
	public String email;
	public String password;
}
