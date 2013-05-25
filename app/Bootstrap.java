import models.Clinic;
import play.jobs.Job;
import play.jobs.OnApplicationStart;


 @OnApplicationStart
public class Bootstrap extends Job {
	 
    public void doJob() throws Exception {
    	
    	if(Clinic.count() == 0 ) {
    		Clinic a = new Clinic(new double[]{3.08423,101.682544}, "Clinic A", "Jalan ABC", "123456");
    		Clinic b = new Clinic(new double[]{3.08453,101.682286}, "Clinic E", "Jalan EFG", "654321");
    		Clinic c = new Clinic(new double[]{3.083909,101.681385}, "Clinic I", "Jalan IJK", "1111111");
    		Clinic d = new Clinic(new double[]{3.09655,101.678789}, "TDMC", "Taman Desa", "1012388177");
    		Clinic e = new Clinic(new double[]{3.11412,101.667159}, "Bangsar South Clinic", "Bangsar South", "0377108877");
    		a.save();
    		b.save();
    		c.save();
    		d.save();
    		e.save();
    	}
 
    }
 
}