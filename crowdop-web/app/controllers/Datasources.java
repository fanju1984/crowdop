package controllers;

import java.util.*;
import java.io.*;
import java.nio.file.Files;


import static play.data.Form.form;
import play.Play;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.datasources.*;
import models.*;

/*
 * The actions related to the model "DataSource"
 * Author: Ju Fan
 */


@Security.Authenticated(Secured.class)
public class Datasources extends Controller {
	// public static final String datafolder = Play.application().path() + "/.datasources/.data/";
 //  public static final String summaryfolder = Play.application().path() + "/.datasources/.summary/";
 //  public static final String tmpfolder = Play.application().path() + "/.datasources/.tmp/";
	   /*
      *   Display the data of `datasource` at the `page` page. 
      *   This is the index page of the Datasources
      */
  public static Result index(Long datasourceId, Integer page) {
    if (Secured.isOwnerOfDatasource(datasourceId)) {
      try {
      DataSource datasource = DataSource.find.ref(datasourceId);
      List<String> headers = datasource.getHeaders(); // get table headers
      //DataSource.getHeaders (datasource, datafolder);
      List<List<String>> records = datasource.getDataRecords(page);
      // get table record at `page` with the configured `PAGE_SIZE`
      //  DataSource.getRecords (datasource, page, PAGE_SIZE, datafolder);
      return ok(
        index.render(
          datasource,
          headers, 
          records,
          page
        )
      );
      } catch(Exception e) {
        e.printStackTrace(); 
        return forbidden();
      }
    } else {
      return forbidden();
    }
  }

  public static Result summary(Long datasourceId, Integer page) {
    if (Secured.isOwnerOfDatasource(datasourceId)) {
      try {
      DataSource datasource = DataSource.find.ref(datasourceId);
      List<List<String>> records = datasource.getDataSummary (page);//DataSource.getDataSummary (datasource, page, PAGE_SIZE, summaryfolder);
      return ok(
        summary.render(
          datasource,   
          records,
          page
        )
      );
      } catch(Exception e) {
        e.printStackTrace(); 
        return forbidden();
      }
    } else {
      return forbidden();
    }
  }

	public static Result add() {
	    DataSource newDataSource = DataSource.create(
	    	"New Data Table", DataSource.TYPE_DATA,
        request().username()
	    );
	    return ok(item.render(newDataSource));
	}

	public static Result rename(Long datasourceId) {
	    if (Secured.isOwnerOfDatasource(datasourceId)) {
	        return ok(
	            DataSource.rename(
	                datasourceId,
	                form().bindFromRequest().get("name")
	            )
	        );
	    } else {
	        return forbidden();
	    }
	}

	public static Result delete(Long datasourceId) {
	    if(Secured.isOwnerOfDatasource(datasourceId)) {
        try {
	        DataSource.deleteDataSource(datasourceId);
	        return ok();
        } catch(Exception e) {
          e.printStackTrace();
          return forbidden();
        }
	    } else {
	        return forbidden();
	    }
	}

  public static Result updateHtmlTemplate () {
      Long datasource = Long.parseLong (form().bindFromRequest().get("datasource"));
      if(Secured.isOwnerOfDatasource(datasource)) {
        String htmlTemplate = form().bindFromRequest().get("template");
          DataSource.updateHtmlTemplate(datasource, htmlTemplate);
          return redirect(
              "/#" + routes.Datasources.index(datasource, 0)
        );
      } else {
          return forbidden();
      }
    }

	public static Result upload() {
		MultipartFormData body = request().body().asMultipartFormData();
		Long datasource = Long.parseLong (form().bindFromRequest().get("datasource"));
		if(!Secured.isOwnerOfDatasource(datasource)) {
			return forbidden();
		}
  		FilePart upfile = body.getFile("upfile");
  		try {
  			if (upfile != null) {
    			File file = upfile.getFile();
    			DataSource.importDataFromFile (datasource, file);
  			} else {
    			flash("error", "Missing file"); 
  			} 
  		} catch (Exception e) {
        e.printStackTrace();
  			flash("error", e.toString()); 
  		}
		return redirect(
	            "/#" + routes.Datasources.index(datasource, 0)
	    );
		//return ok("File uploaded");
    }

    public static Result uploadSummary() {
		MultipartFormData body = request().body().asMultipartFormData();
		Long datasource = Long.parseLong (form().bindFromRequest().get("datasource"));
		if(!Secured.isOwnerOfDatasource(datasource)) {
			return forbidden();
		}
  		FilePart upfile = body.getFile("upfile");
  		try {
  			if (upfile != null) {
    			File file = upfile.getFile();
    			DataSource.importSummaryFromFile (datasource, file);
  			} else {
    			flash("error", "Missing file"); 
  			} 
  		} catch (Exception e) {
  			System.out.println (e);
  			flash("error", e.toString()); 
  		}
		return redirect(
	            "/#" + routes.Datasources.summary(datasource, 0)
	    );
		//return ok("File uploaded");
    }

    
}