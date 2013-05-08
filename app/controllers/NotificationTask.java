package controllers;

import java.io.IOException;


import models.JsonResponse;
import models.Transaction;
import models.User;
import static utils.Constant.*;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import play.jobs.Job;
import play.mvc.Util;



public class NotificationTask extends Job<JsonResponse> {

	private String source, target, action, sourceMsg, targetMsg, payload;
	
	public NotificationTask(String s, String t, 
			String action, String sourceMsg, String targetMsg, 
			String payload) {
		this.source = s;
		this.target = t;
		this.action = action;
		this.sourceMsg = sourceMsg;
		this.targetMsg = targetMsg;
		this.payload = payload;
	}
	
	private Result sendGCM(String regKey) throws IOException {

		Sender sender = new Sender("AIzaSyCL2UN3dmT2nnOvUhANS296yUygKdjiyM8");
    	Builder messageBuilder = new Message.Builder();
    	messageBuilder.addData("title", "CareMe Notification");
    	messageBuilder.addData("message", targetMsg);
    	messageBuilder.addData("payload", payload);
    	Message message = messageBuilder.build();
    	return sender.send(message, regKey, 1);		
	}	
	
	private JsonResponse sendSMS() {
		return new JsonResponse(OK, "SEND SMS");
	}
	
	public JsonResponse doJobWithResult() {
		
		User tUser = User.find("byEmail", this.target).first();
		Transaction txn = null;
		JsonResponse resp = null;
		try {
			if(tUser != null) {
				//SEACH regKey for target
				sendGCM(tUser.gcmId);
				resp = new JsonResponse(OK, this.sourceMsg);
			} else {
				resp = sendSMS();
			}
			//log it to db					
			txn = new Transaction(this.source, this.target, this.action);
		} catch (IOException e) {
			e.printStackTrace();
			resp = new JsonResponse(NOK, "Error with add care");
		} catch(Exception e) {
			e.printStackTrace();
			txn = new Transaction(this.source, this.target, this.action);
			txn.setMsg("error:" + e.getMessage());
			resp = new JsonResponse(NOK, "Error with add care");
		}
		txn.save();
		return resp;
	}
	

	
}
