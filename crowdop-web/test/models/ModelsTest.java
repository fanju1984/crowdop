package models;

import models.*;
import org.junit.*;
import static org.junit.Assert.*;
import play.test.WithApplication;
import static play.test.Helpers.*;
import java.util.*;
import com.avaje.ebean.Ebean;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;

import play.mvc.*;
import play.libs.*;
import play.test.*;
import static play.test.Helpers.*;
import com.avaje.ebean.Ebean;
import com.google.common.collect.ImmutableMap;
public class ModelsTest extends WithApplication {
    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
        Ebean.save((List) Yaml.load("test-data.yml"));
    }

	@Test
    public void countRowCounts () {
        assertEquals(3, User.find.findRowCount());
        
        //assertEquals(4, DataSource.find.findRowCount());

        //assertEquals(1, PriceModel.find.findRowCount());

        //assertEquals(1, Project.find.findRowCount());
    }

/*
    @Test
    public void tryAuthenticateUser() {
        assertNotNull(User.authenticate("bob@example.com", "bob"));
        assertNotNull(User.authenticate("jane@example.com", "jane"));
        assertNull(User.authenticate("jeff@example.com", "badpassword"));
        assertNull(User.authenticate("tom@example.com", "secret"));
    }

    @Test
    public void findDataSourcesInvolving() {
        List<DataSource> results = DataSource.findInvolving("bob@example.com");
        assertEquals(2, results.size());
        assertEquals("image", results.get(0).name);
        assertEquals("vehicle", results.get(1).name);
    }

    @Test
    public void findProjectsInvolving () {
        List<Project> projects = Project.findInvolving ("bob@example.com");
        assertEquals(1, projects.size());
        Project project = projects.get(0);
        assertEquals("CSelect Query 1", project.name);
        assertEquals("select * from image where image.make = 'audi' and image.body_style = 'suvs' and image.year = 2014", project.queryStat);
        assertEquals(new Double(10), project.budget);
        assertEquals(1, project.dataSources.size());
        assertEquals("image", project.dataSources.get(0).name);
        DataSource vehicleDS = DataSource.findByName ("vehicle");
        Project.addDataSource (project, "vehicle");
        assertEquals(2, project.dataSources.size());
        assertEquals("vehicle", project.dataSources.get(1).name);
    }
    */
}
