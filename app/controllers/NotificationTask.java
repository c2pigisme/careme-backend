package controllers;

import java.io.IOException;

import models.JsonResponse;
import models.Transaction;
import models.User;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;

import play.jobs.Job;
import play.mvc.Util;



public class NotificationTask extends Job<JsonResponse> {

	private String source, target, action, msgTemplate, payload;
	
	public NotificationTask(String s, String t, 
			String action, String msgTemplate, 
			String payload) {
		this.source = s;
		this.target = t;
		this.action = action;
		this.msgTemplate = msgTemplate;
		this.payload = payload;
	}
	
	private Result sendGCM(String regKey) throws IOException {

		Sender sender = new Sender("AIzaSyCL2UN3dmT2nnOvUhANS296yUygKdjiyM8");
    	Builder messageBuilder = new Message.Builder();
    	messageBuilder.addData("title", "CareMe Notification");
    	messageBuilder.addData("message", "Your have new request");
    	messageBuilder.addData("payload", payload);
    	Message message = messageBuilder.build();
    	return sender.send(message, regKey, 1);		
	}	
	
	private JsonResponse sendSMS() {
		return new JsonResponse("NEXT", "SEND SMS");
	}
	
	public JsonResponse doJobWithResult() {
		
		User tUser = User.find("byPhoneNumber", this.target).first();
		Transaction txn = null;
		JsonResponse resp = null;
		try {
			if(tUser != null) {
				//SEACH regKey for target
				sendGCM(tUser.gcmId);
				resp = new JsonResponse("OK", "Add to care list");
			} else {
				resp = sendSMS();
			}
			//log it to db					
			txn = new Transaction(this.source, this.target, this.action);
		} catch (IOException e) {
			e.printStackTrace();
			resp = new JsonResponse("NOK", "Error with add care");
		} catch(Exception e) {
			e.printStackTrace();
			txn = new Transaction(this.source, this.target, this.action);
			txn.setMsg("error:" + e.getMessage());
			resp = new JsonResponse("NOK", "Error with add care");
		}
		txn.save();
		return resp;
	}
	

	
}
