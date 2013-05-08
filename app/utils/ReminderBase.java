package utils;

import static utils.Constant.OK;

import java.util.ArrayList;

import models.JsonResponse;
import models.Reminder;
import models.User;

public class ReminderBase {

	public static JsonResponse addReminder(String email, Reminder reminder) {
		
		User user = User.find("byEmail", email).first();
		
		if(user.reminders  == null) {
			user.reminders = new ArrayList<Reminder>();
		}
		user.reminders.add(reminder);
		user.save();
		return new JsonResponse(OK, "");
	}
	
	public static JsonResponse shareReminder(Reminder reminder) {
		return null;
	}
}
