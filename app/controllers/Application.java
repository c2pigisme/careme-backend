package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jamonapi.utils.Logger;

import models.JsonResponse;
import models.Session;
import models.User;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;

public class Application extends Controller {

	@Before
	static void CORS() {
	    if(request.headers.containsKey("origin")){
	        response.headers.put("Access-Control-Allow-Origin", new Header("Access-Control-Allow-Origin", "*"));
	        response.headers.put("Access-Control-Allow-Headers", new Header("Access-Control-Allow-Headers", "X-Requested-With"));
	    }
	}
	
	public static void register(User u) {
		System.out.println(u.name);
		System.out.println(u.email);
		System.out.println(u.password);
		
		String originPassword = u.password;
		User existUser = User.find("byEmail", u.email).first();
		
		if(existUser != null) {
			renderJSON(new JsonResponse("NOK", "User already exist"));
		} else {
			u.password = Crypto.encryptAES(u.password);
			u.save();		
			registerLogin(u.email, originPassword);
		}
		
	}
	
	public static void session(String email, String session) {
		
		Session s = Session.find("byEmail", email).first();
		if(s != null && s.session.equals(session)) {
			JsonResponse sess = new JsonResponse(s.session, "OK", "Session Matched");
			renderJSON(sess);
		}
		renderJSON(new JsonResponse("NOK", "Session Not Matched"));
	}
	
	static void registerLogin(String email, String password) {
		
		User u = User.find("byEmail", email).first();
		
		
		if(u != null) {
			
			String userPass = Crypto.decryptAES(u.password);	
			
			System.out.println("UserPass : " + userPass + " - " + password);
			if(userPass.equals(password)) {
			
				List<Session>existSesssion = Session.find("byEmail", u.email).asList();
				for(Session s : existSesssion) {
					s.delete();
					Logger.log(String.format("Session exist and delete now, user(%s)", u.email));
				}
				
				Session session = new Session(UUID.randomUUID().toString(), u.email);
				session.save();
				
				JsonResponse login = new JsonResponse(session.session, "OK", "Login Successfully");
				
				renderJSON(login);
			}
		}
		renderJSON(new JsonResponse("NOK", "Invalid Username or Password"));		
	}
	
	public static void login(String email, String password) {
		
		User u = User.find("byEmail", email).first();
		
		
		if(u != null) {
			
			String userPass = Crypto.decryptAES(u.password);	
			
			System.out.println("UserPass : " + userPass + " - " + password);
			if(userPass.equals(password)) {
			
				List<Session>existSesssion = Session.find("byEmail", u.email).asList();
				for(Session s : existSesssion) {
					s.delete();
					Logger.log(String.format("Session exist and delete now, user(%s)", u.email));
				}
				
				Session session = new Session(UUID.randomUUID().toString(), u.email);
				session.save();
				
				JsonResponse login = new JsonResponse(session.session, "OK", "Login Successfully");
				
				renderJSON(login);
			}
		}
		renderJSON(new JsonResponse("NOK", "Invalid Username or Password"));
	}
	
	public static void options() {
	}
	
}
