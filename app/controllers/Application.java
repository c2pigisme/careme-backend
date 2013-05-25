package controllers;

import java.io.IOException;
import java.net.UnknownHostException;



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
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import models.CMR;
import models.Clinic;
import models.JsonRequest;
import models.JsonResponse;
import models.Reminder;
import models.Session;
import models.Transaction;
import models.User;
import static utils.PayloadBuilder.*;
import static utils.Constant.*;
import static utils.PermissionBase.*;
import static utils.ReminderBase.*;
import play.Play;
import play.libs.Crypto;
import play.libs.WS;
import play.modules.morphia.Model.MorphiaQuery;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Header;
import play.mvc.Util;


public class Application extends Controller {

	private static Gson gson = new Gson();
	private static MongoClient mongoClient;
	private static DB db;
	private static DBCollection coll;
	
	@Before
	static void CORS() {
	    if(request.headers.containsKey("origin")){
	        response.headers.put("Access-Control-Allow-Origin", new Header("Access-Control-Allow-Origin", "*"));
	        response.headers.put("Access-Control-Allow-Methods", new Header("Access-Control-Allow-Methods", "POST, DELETE, GET, PUT"));
	        response.headers.put("Access-Control-Allow-Headers", new Header("Access-Control-Allow-Headers", "X-Requested-With"));
	    }
	}
	
	@Before(unless={"login", "register", "session", "options", "gcm", "logout", "nearByClinic"})
	static void checkSession(String email, String session) {
		
		System.out.println("=== checking session ======");
		
		Session s = Session.find("byEmail", email).first();
		if(s == null || (!s.session.equals(session))) {
			System.out.println("!!!! Session Not Matched !!!!");
			System.out.println(String.format("Email(%s) - Session(%s) - DBSession(%s)", email, session, s.session));
			renderJSON(new JsonResponse(NOK, "Session Not Matched"));	
		}		
		System.out.println(String.format("Email(%s) - Session(%s) - DBSession(%s)", email, session, s.session));
	}
	
	public static void register(User u) {
		
		String originPassword = u.password;
		User existUser = User.find("byEmail", u.email).first();
		
		if(existUser != null) {
			renderJSON(new JsonResponse(NOK, "User already exist"));
		} else {
			u.password = Crypto.encryptAES(u.password);
			
			u.save();		
			registerLogin(u.email, originPassword);
		}
	}
	
	public static void session(String email, String session, String gcmId) {
		Session s = Session.find("byEmail", email).first();
		
		if(s == null || !s.session.equals(session)) {
			renderJSON(new JsonResponse(NOK, "Session Not Matched"));
		}		
		
		User u = User.find("byEmail", email).first();
		if(u != null) {
			if(u.gcmId == null || !u.gcmId.equals(gcmId)) {
				u.gcmId = gcmId;
				u.save();
			}
			
		}
		System.out.println("Session : " + session);
		System.out.println("Email : " + email);
		System.out.println("GCM : " + gcmId);
		JsonResponse sess = new JsonResponse(OK, s.session, "Session Matched");
		renderJSON(sess);
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
				Logger.log(String.format("Save new session(%s)", session.session));
				JsonResponse login = new JsonResponse(OK, session.session, "Login Successfully");
				
				renderJSON(login);
			}
		}
		renderJSON(new JsonResponse(NOK, "Invalid Username or Password"));		
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
				
				JsonResponse login = new JsonResponse(OK, session.session, "Login Successfully");
				
				renderJSON(login);
			}
		}
		renderJSON(new JsonResponse(NOK, "Invalid Username or Password"));
	}
	
	public static void logout(String email, String s) {
	    Session session = Session.find("byEmail", email).findBy("session", s).first();
	    session.delete();
	    renderJSON(new JsonResponse(OK, "Logout Successfully"));
	}
	
	public static void options() {}

	public static void careAdd(String json) {
		System.out.println(" --------- careAdd : " + json);
		JsonRequest req = gson.fromJson(json, JsonRequest.class);
		
		JsonResponse resp = addCare(req.sourcePhoneNumber, req.targetPhoneNumber, req.displayName);

		//TODO: APPEND JSON Element
		JsonResponse resp1 = await(new NotificationTask(req.srcEmail, 
			    req.targetEmail, "shareAdd", "Add to Share list", "One share to you", 
				buildAddCareMessage(req)).now());
		
		
		renderJSON(resp);
	}
	
    public static void careDel(String json) {
 
		JsonRequest req = gson.fromJson(json, JsonRequest.class);
		JsonResponse resp = delCare(req.sourcePhoneNumber, req.targetPhoneNumber);
		
        Transaction txn = new Transaction(req.sourcePhoneNumber, req.targetPhoneNumber, "careDel");
        txn.save();
        System.out.println("  --------  careDel resp : " + json);
        renderJSON(resp);        
    }
	
	public static void shareAdd(String json) {
		System.out.println("------- shareAdd : " + json);
		JsonRequest req = gson.fromJson(json, JsonRequest.class);
		
		JsonResponse resp = addShare(req.sourcePhoneNumber, req.targetPhoneNumber, req.displayName);
		//TODO: APPEND JSON Element
		JsonResponse resp1 = await(new NotificationTask(req.sourcePhoneNumber, 
			    req.targetPhoneNumber, "shareAdd", "Add to Share list", "One share to you", 
				buildAddCareMessage(req)).now());
		
		renderJSON(resp);		
	}

    public static void shareDel(String json) {
        System.out.println(" -------- shareDel : " + json);
        JsonRequest req = gson.fromJson(json, JsonRequest.class);
        JsonResponse resp = delShare(req.sourcePhoneNumber, req.targetPhoneNumber);
        renderJSON(resp);        
    }
	
    public static void shareApprove(String json) {
        System.out.println(" -------- shareApprove : " + json);
        JsonRequest req = gson.fromJson(json, JsonRequest.class);

        JsonResponse resp = approveShare(req.sourcePhoneNumber, req.targetPhoneNumber);
        
        renderJSON(resp);        
    }    
    
	public static void sharelist(String phoneNumber, String callback) {
		
		System.out.println("sharelist() - Phone number : " + phoneNumber);
		User u = User.find("byPhoneNumber", phoneNumber).first();
		
		JsonElement body = null;
		
		if(u.shares != null ) {
			body = gson.toJsonTree(u.shares.values());
		} else {
			u.shares = new HashMap<String, CMR>();
			u.save();
		} 
		System.out.println("u.shares : " + u.shares);
		String json = gson.toJson(new JsonResponse(OK, "list of contact", body));

		String resp = String.format("%s(%s)", callback, json);
		System.out.println("sharelist json: " + json);
		renderJSON(resp);		
	}

	public static void carelist(String phoneNumber, String callback) {
		System.out.println("carelist() - Phone number : " + phoneNumber);
		User u = User.find("byPhoneNumber", phoneNumber).first();
		
		JsonElement body = null;
		if(u.cares != null ) {
			System.out.println(u.cares.keySet());
			body = gson.toJsonTree(u.cares.values());
			System.out.println(body);
		} else {
			u.cares = new HashMap<String, CMR>();
			u.save();			
		}
		String json = gson.toJson(new JsonResponse(OK, "list of contact", body));
		String resp = String.format("%s(%s)", callback, json);
		renderJSON(resp);
		
	}
	//TODO: REMOVE LATER
	public static void gcm(String regKey, String payload, String title, String subtitle, String nid) throws IOException {
		
		//title , message , payload
		Sender sender = new Sender("AIzaSyCL2UN3dmT2nnOvUhANS296yUygKdjiyM8");
    	Builder messageBuilder = new Message.Builder();
    	messageBuilder.addData("payload", payload);
    	messageBuilder.addData("title", title);
    	messageBuilder.addData("subtitle", subtitle);
    	messageBuilder.addData("notificationId", nid);
    	Message message = messageBuilder.build();
    	Result result = sender.send(message, regKey, 1);
    	renderText(result.getMessageId() + " -- " + result.getErrorCodeName() + " --- " + result.getCanonicalRegistrationId());
	}

	public static void addOwnEvent(String email, String json) {
		System.out.println("Add Event : " + json);
		Reminder reminder = gson.fromJson(json, Reminder.class);
		JsonResponse resp = addReminder(email, reminder);
		renderJSON(resp);
	}

	public static void careEvent(String json) {
		System.out.println("Care Event : " + json);
		JsonResponse resp = addReminder("", null);
		renderJSON(new JsonResponse(OK, ""));
	}

	public static void nearByClinic(double lat, double lng, double btwKm1,double  btwKm2) throws InterruptedException {
		System.out.println("Latitude : " + lat);
		System.out.println("Longitude : " + lng);
		Thread.sleep(3000);
		try {
			if(mongoClient == null) {
				mongoClient = new MongoClient();
				db = mongoClient.getDB("careme");
				coll = db.getCollection("clinics");
			}
			
			List<Clinic> fromClinics = MorphiaQuery.ds()
					.createQuery(Clinic.class)
					.field("loc").near(lat, lng, btwKm1/111.12).asList();

			List<Clinic> toClinics = MorphiaQuery.ds()
					.createQuery(Clinic.class)
					.field("loc").near(lat, lng, btwKm2/111.12).asList();			
			
			List<Clinic> clinics = new ArrayList<Clinic>();
			
			for(Clinic c : toClinics) {
				if(! fromClinics.contains(c)) {
					clinics.add(c);
				}
			}
			
			renderJSON(clinics);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
