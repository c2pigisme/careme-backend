package controllers;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jamonapi.utils.Logger;

import models.CMR;
import models.JsonRequest;
import models.JsonResponse;
import models.Session;
import models.User;
import static utils.PayloadBuilder.*;
import play.Play;
import play.libs.Crypto;
import play.libs.WS;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;
import play.mvc.Util;

public class Application extends Controller {

	private static Gson gson = new Gson();
	
	@Before
	static void CORS() {
	    if(request.headers.containsKey("origin")){
	        response.headers.put("Access-Control-Allow-Origin", new Header("Access-Control-Allow-Origin", "*"));
	        response.headers.put("Access-Control-Allow-Headers", new Header("Access-Control-Allow-Headers", "X-Requested-With"));
	    }
	}
	
	@Before(unless={"login", "register", "session", "options", "gcm"})
	static void checkSession(String email, String session) {
		Session s = Session.find("byEmail", email).first();
		if(s == null || (!s.session.equals(session))) {
			renderJSON(new JsonResponse("NOK", "Session Not Matched"));	
		}
	}
	
	public static void register(User u) {
		
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
	
	public static void session(String email, String session, String gcmId) {
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
	
	public static void login(String email, String password, String gcmId) {
		
		User u = User.find("byEmail", email).first();
		
		
		if(u != null) {
			
			String userPass = Crypto.decryptAES(u.password);	
			
			if(u.gcmId == null || !u.gcmId.equals(gcmId)) {
				u.gcmId = gcmId;
				u.save();
			}
			
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
	
	public static void options() {}

	public static void careAdd(String json) {
		System.out.println(json);
		JsonRequest req = gson.fromJson(json, JsonRequest.class);
		
		CMR cmr = new CMR(req.targetPhoneNumber,"pending");
		//OR by email
		User u = User.find("byPhoneNumber", req.sourcePhoneNumber).first();
		
		if(u.cares == null) {
			u.cares = new HashMap<String, CMR>();
		}
		if(!u.cares.containsKey(req.targetPhoneNumber)) {
			u.cares.put(req.targetPhoneNumber, cmr);	
		} else {
			renderJSON(new JsonResponse("NOK", String.format("Care request has been sent for number(%s)", req.targetPhoneNumber)));
		}

		
		JsonResponse resp = await(new NotificationTask(req.sourcePhoneNumber, 
			    req.targetPhoneNumber, "careAdd", "", 
				buildAddCareMessage(req.sourcePhoneNumber, req.targetPhoneNumber)).now());
		
		u.save();
		
		renderJSON(resp);
	}
	
	public static void shareAdd(String json) {
		System.out.println(json);
		JsonRequest req = gson.fromJson(json, JsonRequest.class);
		
		CMR cmr = new CMR(req.targetPhoneNumber);
		//OR by email
		User u = User.find("byPhoneNumber", req.sourcePhoneNumber).first();
		
		if(u.shares == null) {
			u.shares = new HashMap<String, CMR>();
		}
		if(!u.shares.containsKey(req.targetPhoneNumber)) {
			u.shares.put(req.targetPhoneNumber, cmr);	
		} else {
			renderJSON(new JsonResponse("NOK", String.format("Share request has been sent for number(%s)", req.targetPhoneNumber)));
		}
		
		JsonResponse resp = await(new NotificationTask(req.sourcePhoneNumber, 
			    req.targetPhoneNumber, "shareAdd", "", 
				buildAddCareMessage(req.sourcePhoneNumber, req.targetPhoneNumber)).now());
		
		u.save();
		
		renderJSON(resp);		
	}

	public static void sharelist(String phoneNumber, String session, String callback) {
		User u = User.find("byPhoneNumber", phoneNumber).first();
		
		JsonElement body = null;
		
		System.out.println("u.shars : " + u.shares);
		
		if(u.shares != null ) {
			body = gson.toJsonTree(u.shares.values());
		}
		String json = gson.toJson(new JsonResponse(session, "OK", "list of contact", body));
		System.out.println(json);
		String resp = String.format("%s(%s)", callback, json);
		renderJSON(resp);		
	}

	public static void carelist(String phoneNumber, String session, String callback) {
		User u = User.find("byPhoneNumber", phoneNumber).first();
		
		JsonElement body = null;
		
		if(u.cares != null ) {
			body = gson.toJsonTree(u.cares.values());
		}
		String json = gson.toJson(new JsonResponse(session, "OK", "list of contact", body));
		System.out.println(json);
		String resp = String.format("%s(%s)", callback, json);
		renderJSON(resp);
		
	}
	//TODO: REMOVE LATER
	public static void gcm(String msg, String regKey, String type) throws IOException {
		
		//title , message , payload
		Sender sender = new Sender("AIzaSyCL2UN3dmT2nnOvUhANS296yUygKdjiyM8");
    	Builder messageBuilder = new Message.Builder();
    	messageBuilder.addData("message", msg);
    	messageBuilder.addData("type", type);
    	Message message = messageBuilder.build();
    	Result result = sender.send(message, regKey, 1);
    	renderText(result.getMessageId() + " -- " + result.getErrorCodeName() + " --- " + result.getCanonicalRegistrationId());
	}
}
