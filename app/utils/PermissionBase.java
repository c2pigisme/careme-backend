package utils;

import static utils.Constant.*;
import static utils.PayloadBuilder.buildAddCareMessage;

import java.util.HashMap;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import models.CMR;
import models.JsonResponse;
import models.PendingRequest;
import models.Transaction;
import models.User;
import controllers.NotificationTask;

public class PermissionBase {
	
	private static final Gson gson = new Gson();
	
	public static JsonResponse addCare(String src, String target, String displayName) {
		JsonResponse resp = new JsonResponse(NOK, String.format("Error: Invalid request"));
		
		//OR by email
		User srcUser = User.find("byPhoneNumber", src).first();
		//OR by email
		User targetUser = User.find("byPhoneNumber", target).first();
		CMR careCmr = new CMR(target, REQ_PENDING);
		CMR shareCmr = new CMR(src, REQ_PENDING);	
		
        if(srcUser == null) {
            return resp;
        }   
                
		if(srcUser.cares == null) {
			srcUser.cares = new HashMap<String, CMR>();
		}
		
		//TODO this should move to another method
	    if(targetUser == null) {
	       JsonObject obj = new JsonObject();
	       obj.addProperty("action", "sms");
	       obj.addProperty("message", "I\'m using CareMe healthcare app, please download it from google playstore: http://bit.ly/13qv128 so that i can add you to my list");
	       if(srcUser.cares.containsKey(target)) {
	    	   return new JsonResponse(NOK, "Selected user has been added to care list");   
	       }
	       srcUser.cares.put(target, careCmr);
	       PendingRequest pr = new PendingRequest(target, shareCmr);
	       srcUser.save();
	       pr.save();
	       return new JsonResponse(OK, "User add to care list", gson.toJson(obj));
	    }
		if(targetUser.shares == null) {
			targetUser.shares = new HashMap<String, CMR>();
		}
		
		if(srcUser.cares.containsKey(target) 
				//prevent user send multiple time
		    || (targetUser.shares.containsKey(src) && targetUser.shares.get(src).status == REQ_PENDING)) {
			return new JsonResponse(NOK, String.format("Care request has been sent for number(%s)", target));
		}
	
		//keep request pending in src user table
		srcUser.cares.put(target, careCmr);	
		//keep approval pending in target user table		
		targetUser.shares.put(src, shareCmr);
		
		JsonObject obj = new JsonObject();
		//TODO
		//obj.add("", "")s;
//		NotificationTask notifTaks = new NotificationTask(srcUser.email, 
//				targetUser.email, ACTION_ADDCARE, "", "", );
//		
		srcUser.save();
		targetUser.save();
		resp = new JsonResponse(OK, String.format("Add to care list"));
		return resp;
	}
	
	public static JsonResponse delCare(String src, String target) {
		 
		JsonResponse resp = new JsonResponse(NOK, String.format("Error: Invalid request"));
	        
	        //OR by email
	        User srcUser = User.find("byPhoneNumber", src).first();
	        //OR by email
	        User targetUser = User.find("byPhoneNumber", target).first();
	        
	        if(srcUser == null || srcUser == null) {
	            return resp;
	        }   
	        
	        if(srcUser.cares.containsKey(target)) {
	        	srcUser.cares.remove(target);
	            resp = new JsonResponse(OK, String.format("You just uncare somebody"));
	        } else {
	            //should not trigger this line, unless some problem on front end
	            return new JsonResponse(NOK, String.format("%s is not in care list", target));
	        }

	        //if somebody who has approved the care request from source, we delete it
	        if(targetUser.shares.containsKey(src) && targetUser.shares.get(src).status == REQ_APPROVE) {
	        	targetUser.shares.remove(src);
	        }
	        
	        srcUser.save();
	        targetUser.save();
	        Transaction txn = new Transaction(src, target, "careDel");
	        txn.save();
	        return resp;
	}

	public static JsonResponse addShare(String src, String target, String displayName) {
	
		JsonResponse resp = new JsonResponse(NOK, String.format("Error: Invalid request"));
		CMR srcCmr = new CMR(target, REQ_APPROVE);
		CMR targetCmr = new CMR(src, REQ_APPROVE);
		//OR by email
		User srcUser = User.find("byPhoneNumber", src).first();
		User targetUser = User.find("byPhoneNumber", target).first();
		
		if(srcUser.shares == null) {
			srcUser.shares = new HashMap<String, CMR>();
			resp = new JsonResponse(OK, String.format("Add to share list"));
		}

		if(targetUser.cares == null) {
			targetUser.cares = new HashMap<String, CMR>();
		}
		
		if(!srcUser.shares.containsKey(target)) {
			srcUser.shares.put(target, srcCmr);	
		} else {
			return new JsonResponse(NOK, String.format("Share request has been sent for number(%s)", target));
		}
		
		
		if(!targetUser.cares.containsKey(src)) {
			targetUser.cares.put(src, targetCmr);	
		} else {
			//Should not happen
			return new JsonResponse(NOK, String.format("Share request has been sent for number(%s)", target));
		}
		
		
		srcUser.save();		
		targetUser.save();
		return resp;
	}
	
	public static JsonResponse delShare(String src, String target) {
		JsonResponse resp = new JsonResponse(NOK, String.format("Error: Invalid request"));
        
        //OR by email
        User srcUser = User.find("byPhoneNumber", src).first();
        User targetUser = User.find("byPhoneNumber", target).first();
        
        if(srcUser == null || targetUser == null) {
            return resp;
        }   
        
        if(srcUser.shares != null && srcUser.shares.containsKey(target)) {
            srcUser.shares.remove(target);
            resp = new JsonResponse(OK, String.format("You just unshare somebody"));
        } else {
            //should not trigger this line, unless some problem on front end
            return new JsonResponse(NOK, String.format("%s is not in share list", target));
        }
        
        if(targetUser.cares != null && targetUser.cares.containsKey(src)) {
        	targetUser.cares.remove(src);
        }
        
        srcUser.save();
        targetUser.save();
        
        Transaction txn = new Transaction(src, target, "shareDel");
        txn.save();
        
        return resp;
	}
	
	public static JsonResponse approveShare(String src, String target) {
		
		JsonResponse resp = new JsonResponse(NOK, String.format("Error: Invalid request"));
        //OR by email
        User srcUser = User.find("byPhoneNumber", src).first();
        User targetUser = User.find("byPhoneNumber", target).first();
        
        if(srcUser == null || targetUser == null) {
            return resp;
        }   
        
        if(srcUser.shares != null && srcUser.shares.containsKey(target)) {
        	srcUser.shares.get(target).status = REQ_APPROVE;
        }
        
        if(targetUser.cares != null && targetUser.cares.containsKey(src)) {
        	targetUser.cares.get(src).status = REQ_APPROVE;
        }
        
        srcUser.save();
        targetUser.save();
        
        return  new JsonResponse(OK, String.format("Share approved"));
	}
}
