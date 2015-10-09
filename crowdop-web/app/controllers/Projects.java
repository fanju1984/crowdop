package controllers;

import java.util.*;

import static play.data.Form.form;
import play.mvc.*;
import views.html.projects.*;
import models.*;

@Security.Authenticated(Secured.class)
public class Projects extends Controller {

	/**
     * Display the datasource panel for the current user.
     */
    public static Result index(Long project) {
        if (Secured.isMemberOf(project)) {
        	List<Job> involvedJobs = Job.findInvolving(project);
        	return ok(
                index.render(
                    Project.find.byId(project),
                    involvedJobs,
                    DataSource.findByTypeAndOwner("data", 
                    	request().username())
                )
            );
        } else {
            return forbidden();
        }
    }

	public static Result add() {
	    Project newProject = Project.create(
	        "New project",
	        "SELECT *",
	        request().username()
	    );
	    return ok(item.render(newProject));
	}

	public static Result rename(Long project) {
	    if (Secured.isMemberOf(project)) {
	        return ok(
	            Project.rename(
	                project,
	                form().bindFromRequest().get("name")
	            )
	        );
	    } else {
	        return forbidden();
	    }
	}

	public static Result delete(Long project) {
	    if(Secured.isMemberOf(project)) {
	        Project.find.ref(project).delete();
	        return ok();
	    } else {
	        return forbidden();
	    }
	}

	public static Result updateQueryStat () {
    	Long project = Long.parseLong (form().bindFromRequest().get("project"));
    	if(Secured.isMemberOf(project)) {
    		Long destDataId = Long.parseLong (form().bindFromRequest().get("destination"));

    		String queryStat = form().bindFromRequest().get("querystat");
	        Project.updateQueryStat(project, queryStat, destDataId);
	        return redirect(
	            "/#" + routes.Projects.index(project)
	    	);
	    } else {
	        return forbidden();
	    }
    }

    public static Result updatePrices () {
    	Long project = Long.parseLong (form().bindFromRequest().get("project"));
    	if(Secured.isMemberOf(project)) {
    		Double cselectBase = Double.parseDouble(form().bindFromRequest().get("cselectBase"));
    		Double cselectInc = Double.parseDouble(form().bindFromRequest().get("cselectInc"));
    		Double cjoinBase = Double.parseDouble(form().bindFromRequest().get("cjoinBase"));
    		Double cjoinInc = Double.parseDouble(form().bindFromRequest().get("cjoinInc"));
    		Double cfillBase = Double.parseDouble(form().bindFromRequest().get("cfillBase"));
    		Double cfillInc = Double.parseDouble(form().bindFromRequest().get("cfillInc"));

	        Project.updatePriceModel(project, cselectBase, cselectInc,
	        	cjoinBase, cjoinInc, cfillBase, cfillInc);
	        return redirect(
	            "/#" + routes.Projects.index(project)
	    	);
	    } else {
	        return forbidden();
	    }
    }

    public static Result genQueryPlan (Long projectId) {
    	if (Secured.isMemberOf(projectId)) {
    		try {
    		Project.createJobs(projectId);
    		return redirect(
	            "/#" + routes.Projects.index(projectId)
	    	);
    		} catch (Exception e) {
    			e.printStackTrace();
    			return forbidden();
    		}
    	} else {
    		return forbidden();
    	}
    }
}