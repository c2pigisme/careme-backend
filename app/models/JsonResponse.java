package models;

public class JsonResponse {

	public String session;
	public String status;
	public String message;
	
	public JsonResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public JsonResponse(String session, String status, String message) {
		this.session = session;
		this.status = status;
		this.message = message;
	}
}
