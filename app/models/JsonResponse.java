package models;

import com.google.gson.JsonElement;

public class JsonResponse {

	public String session;
	public String status;
	public String message;
	public JsonElement body;
	
	public JsonResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}
	
    public JsonResponse(String status, String message, JsonElement body) {
        this.status = status;
        this.message = message;
    }
	
	public JsonResponse(String session, String status, String message) {
		this.session = session;
		this.status = status;
		this.message = message;
	}
	public JsonResponse(String session, String status, String message, JsonElement body) {
		this.session = session;
		this.status = status;
		this.message = message;
		this.body = body;
	}
}
