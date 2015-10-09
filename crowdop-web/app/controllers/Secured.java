package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }

    public static boolean isMemberOf(Long project) {
	    return Project.isMember(
	        project,
	        Context.current().request().username()
	    );
	}

    public static boolean isOwnerOfJob (Long jobId) {
        Job job = Job.find.ref(jobId);
        return Project.isMember(job.assignedProject.id, 
            Context.current().request().username());
    }

    public static boolean isOwnerOfDatasource(Long datasource) {
        return DataSource.isOwner(
            datasource,
            Context.current().request().username()
        );
    }
}