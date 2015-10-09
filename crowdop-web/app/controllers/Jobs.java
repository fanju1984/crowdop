package controllers;

import java.util.*;

import static play.data.Form.form;
import play.mvc.*;
import views.html.projects.*;
import models.*;

@Security.Authenticated(Secured.class)
public class Jobs extends Controller {
	public static Result execute () {
		Long projectId = Long.parseLong (form().bindFromRequest().get("project"));
		Long jobId = Long.parseLong (form().bindFromRequest().get("job"));
    	return doExecute(projectId, jobId, true);
    }

    public static Result doExecute (Long projectId, Long jobId, boolean doCrowdsource) {
        Job job = Job.find.ref(jobId);
        try {
            job.state = job.execute(doCrowdsource);
            job.update();
            
            // Update states of other jobs
            Job succJob = job.getSuccessor(); // find the successor job
            if (succJob != null) {
                List<Job> prevJobs = Job.findBySuccessor(succJob.id);
                boolean prevComp = true;
                for (Job prevJob : prevJobs) {
                    if (prevJob.state != Job.STATE_COMPLETE) {
                        prevComp = false;
                        break;
                    }
                }
                if (prevComp) {
                    succJob.state = Job.STATE_PENDING;
                    succJob.update();
                }
            }   
            // End of the update
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect(
            "/#" + routes.Projects.index(projectId)
        );
    }
}