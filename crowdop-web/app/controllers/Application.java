package controllers;

import play.*;
import play.mvc.*;
import play.data.*;
import static play.data.Form.*;

import java.io.*;

import models.*;
import views.html.*;

/*
 * The general actions for the application
 * Author: Ju Fan
 */
public class Application extends Controller {

	/*
	 *	The index of the web service
	 */
	@Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(index.render(
            Project.findInvolving(request().username()),
            DataSource.findByTypeAndOwner("data", request().username()),
            User.find.byId(request().username())
        )); 
    }

    /*
     * The action for user login
     */
    public static Result login() {
    	return ok(
        	login.render(Form.form(Login.class))
    	);
	}


	public static class Login {
    	public String email;
    	public String password;

    	public String validate() {
	    	if (User.authenticate(email, password) == null) {
	    	  return "Invalid user or password";
	    	}
    		return null;
		}
	}

	/*
	 * The action for authenticating each request
	 */
	public static Result authenticate() {
	    Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
	    if (loginForm.hasErrors()) {
	        return badRequest(login.render(loginForm));
	    } else {
	        session().clear();
	        session("email", loginForm.get().email);
	        return redirect(
	            routes.Application.index()
	        );
	    }
	}

	/*
	 * The action for user logout
	 */
	public static Result logout() {
	    session().clear();
	    flash("success", "You've been logged out");
	    return redirect(
	        routes.Application.login()
	    );
	}

	/*
	 * The action for javascript routing
	 */
	public static Result javascriptRoutes() {
	    response().setContentType("text/javascript");
	    return ok(
	        Routes.javascriptRouter("jsRoutes",
	        	// Routes for Projects
	            controllers.routes.javascript.Projects.add(),
	            controllers.routes.javascript.Projects.delete(),
	            controllers.routes.javascript.Projects.rename(),

	            // Routes for Datasourcecs
	            controllers.routes.javascript.Datasources.add(),
	            controllers.routes.javascript.Datasources.delete(),
	            controllers.routes.javascript.Datasources.rename()

	            // Routes
	        ));
	}
}