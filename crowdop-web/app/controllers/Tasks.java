package controllers;

import java.util.*;

import static play.data.Form.form;
import play.mvc.*;
import views.html.tasks.*;
import models.*;

import org.json.*;


@Security.Authenticated(Secured.class)
public class Tasks extends Controller {

	/**
     * Display the content of the specified task
     */
    public static Result index(Long taskId) {
    	Task task = Task.find.ref(taskId);
    	Job job = task.assignedJob;
        String req = job.getRequirement(task);
    	if (job.opType().equals("CSelect")) {
    		return ok(
                cselect.render(
                    req,
                    task
                )
            );
    	} else if (job.opType().equals("CJoin")) {
            String token = task.token;
            if (token.contains("_")) {
                return ok(
                    cjoin.render(
                    req,
                    task
                    )
                );
            } else {
                return ok(
                    multicjoin.render(
                        req,
                        task
                    )
                );
            }
    	} else if (job.opType().equals("CFill")) {
    		return ok(
                cfill.render(
                    req,
                    task
                )
            );
    	} 
        return forbidden();
        
    }

	public static Result list(Long jobId) {
	    if (Secured.isOwnerOfJob(jobId)) {
	    	Job job = Job.find.ref(jobId);
	    	List<Task> tasks = Task.findInvolving(jobId);
	        return ok(
	            list.render(
	                job,
	                tasks
	            )
	        );
	    } else {
	        return forbidden();
	    }
	}

    public static Result assignment (Long taskId) {
        Task task = Task.find.ref(taskId);
        List<Assignment> assignments = Assignment.findInvolving(taskId);
        return ok(
            assignment.render(
                task,
                assignments
            )
        );
    }



    public static Result submitAnswer () {
        String workerId = form().bindFromRequest().get("workerId");
        String assignmentID = "example";
        Long taskId = Long.parseLong (form().bindFromRequest().get("taskId"));
        Task task = Task.find.ref(taskId);

        JSONArray jresult = new JSONArray();
        int optionSize = 1;
        if (form().bindFromRequest().get("checksize") != null) {
            optionSize = Integer.parseInt(
                form().bindFromRequest().get("checksize"));
        }
        for (int num = 0; num < optionSize; num ++) {
            String result = form().bindFromRequest().get("result" + num);
            if (result != null) {
                jresult.put(result);
            }
        }
        System.out.println(jresult);

        
        String comments = form().bindFromRequest().get("comments");

        if (workerId == null || workerId.trim().isEmpty()) {
            return forbidden();
        }
        Date acceptDateTime = new Date();
        Date submitDataTime = new Date();
        Assignment assignment = Assignment.create(taskId, workerId, assignmentID, 
        acceptDateTime, submitDataTime, comments, jresult.toString());
        boolean aggregated = 
            Task.allFinished (task.assignedJob.id);

        if (aggregated) { // If the answers have been aggregated
            Jobs.doExecute (task.assignedJob.assignedProject.id, 
                task.assignedJob.id, false); // execute the job
        }        
        return redirect(
                "/#" + routes.Tasks.list(task.assignedJob.id)
        );
        //return ok("File uploaded");
    }


}