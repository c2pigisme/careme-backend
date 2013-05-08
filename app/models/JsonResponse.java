package models;

import com.google.gson.JsonElement;

public class JsonResponse {

	public String session;
	public int status;
	public String message;
	public JsonElement body;
	
	public JsonResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}
	
    public JsonResponse(int status, String message, JsonElement body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }
	
	public JsonResponse(int status, String session, String message) {
		this.session = session;
		this.status = status;
		this.message = message;
	}
	public JsonResponse(int status, String session, String message, JsonElement body) {
		this.session = session;
		this.status = status;
		this.message = message;
		this.body = body;
	}
}
