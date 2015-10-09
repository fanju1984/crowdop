package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;

import play.Play;


import java.io.*;
import sg.edu.nus.comp.importer.*;
import sg.edu.nus.comp.datastore.*;
import sg.edu.nus.comp.datastore.plugin.*;
import sg.edu.nus.comp.datastore.schema.*;

@Entity
public class DataSource extends Model {
    public static final String TYPE_DATA = "data";
    public static final String TYPE_TEMP = "tmp";
    @Id
    public Long id;

    public String name; // the name of the data source
    public String htmlTemplate; // the actual table storing the data
    public String type; // the type of the data source

    @ManyToOne
    public User owner; // the owner of the data soruce

    public DataSource(String name, String type, User owner) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        htmlTemplate = "";
    }

    public static Model.Finder<Long,DataSource> find = new Model.Finder(Long.class, DataSource.class);

    // Initialize three data stores for storing different kinds of data
    public static DataStore dataStore = new CSVDataStore(
        Play.application().path() + "/" + 
        Play.application().configuration().getString("datasource.datastore.data")); // for user-uploaded data
    public static DataStore summaryStore = new CSVDataStore(
        Play.application().path() + "/" + 
        Play.application().configuration().getString("datasource.datastore.summary")); // for temporary data
    public static DataStore tmpStore = new CSVDataStore (
        Play.application().path() + "/" + 
        Play.application().configuration().getString("datasource.datastore.tmp")); // for temporary data

    /*
     * Create an object of DataSource
     */
    public static DataSource create(String name, String type, String owner) {
        DataSource dataSource = new DataSource(name, type, User.find.ref(owner));
        dataSource.save();
        try {
            if (type.equals(TYPE_TEMP)) {
                DataTable tmpTable = tmpStore.getTable(dataSource.id + "");
                if (tmpTable != null) tmpStore.dropTable(dataSource.id + "");
                tmpStore.createTable(dataSource.id + "", new ArrayList<Field>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }



    /*
     * Delete an object of DataSource
     */
    public static boolean deleteDataSource(Long datasourceId) throws Exception {
        DataSource datasource = DataSource.find.ref(datasourceId);
        dataStore.dropTable(datasource.name);
        datasource.delete();
        return true;
    }

    public static List<DataSource> findInvolving(String user) {
        return find.where()
            .eq("owner.email", user)
            .findList();
    }

    /**
     * Retrieve datasources by name.
     */
    public static DataSource findByName(String name) {
        return find.where().eq("name", name).findUnique();
    }

    /**
     * Retrieve datasources by its type and owner.
     */
    public static List<DataSource> findByTypeAndOwner(String type, String user) {
        return find.where().
                    eq("owner.email", user)
                    .eq("type", type).
                    findList();
    }

    /**
     * Check if the data source belongs to the user
     */
    public static boolean isOwner(Long datasource, String user) {
        return find.where()
            .eq("owner.email", user)
            .eq("id", datasource)
            .findRowCount() > 0;
    }

    /*
     * Rename the datasource
     */
    public static String rename(Long datasourceID, String newName) {
        DataSource datasource = find.ref(datasourceID);
        datasource.name = newName;
        datasource.update();
        return newName;
    }

    /*
     * Update the HTML template of the data source
     */
    public static String updateHtmlTemplate(Long datasourceID, String htmlTemplate) {
        DataSource datasource = find.ref(datasourceID);
        datasource.htmlTemplate = htmlTemplate;
        datasource.update();
        return htmlTemplate;
    }

    /*
        Import data from uploaded source file
    */
    public static void importDataFromFile (Long datasourceId, File srcFile) throws Exception {
        DataSource datasource = find.ref(datasourceId);
        DataTable dataTable = dataStore.getTable(datasource.name);
        if (dataTable == null) {
            dataStore.createTable(datasource.name, new ArrayList<Field>());
            dataTable = dataStore.getTable(datasource.name);
        }
        dataTable.importFromFile(srcFile.getAbsolutePath());
        // CSVImporter importer = new CSVImporter ();
        
        // importer.run (srcFile, datasource.name + "", destPath);
    }

    public static void importSummaryFromFile (Long datasourceId, File srcFile) throws Exception {
        DataSource datasource = find.ref(datasourceId);
        DataTable dataTable = summaryStore.getTable(datasource.name);
        if (dataTable == null) {
            summaryStore.createTable(datasource.name, new ArrayList<Field>());
            dataTable = summaryStore.getTable(datasource.name);
        }
        dataTable.importFromFile(srcFile.getAbsolutePath());
    }

    public DataTable getDataTable () {
        if (type.equals(TYPE_DATA)) {
            return dataStore.getTable(this.name);
        } else if (type.equals(TYPE_TEMP)) {
            return tmpStore.getTable(this.id + "");
        }
        return null;
    }

    public void createDataTable () throws Exception{
        if (type.equals(TYPE_DATA)) {
            dataStore.createTable(this.name, new ArrayList<Field>());
        } else if (type.equals(TYPE_TEMP)) {
            tmpStore.createTable(this.id + "", new ArrayList<Field>());
        }
    }

    public List<String> getHeaders () throws Exception {
        DataTable dataTable = null;
        if (this.type.equals(TYPE_DATA)) {
            dataTable = dataStore.getTable(this.name);
        }
        else if (this.type.equals(TYPE_TEMP)) {
            dataTable = tmpStore.getTable(String.valueOf(this.id));
        }

        if (dataTable == null) return new ArrayList<String>();
        List<String> headers = dataTable.getFieldNames(); 
        return headers; 
        // DataSource datasource = find.ref(datasourceId);
        // DataStore store = new CSVDataStore (storePath);
        // DataTable dataTable = store.getTable (datasource.name + "");
        // if (dataTable == null) return headers;
        // List<Field> fields = dataTable.getFields ();
        // for (Field field : fields) {
        //     headers.add (field.getName ());
        // }
        // return headers;
    }

    public List<List<String>> getDataRecords (int page) throws Exception {
        DataTable dataTable = dataStore.getTable(this.name);
        if (dataTable == null) return new ArrayList<List<String>>();
        int pageSize = Play.application().configuration().getInt("datasource.pagesize");
        int start = page * pageSize;
        int end = (page + 1) * pageSize;       
        List<List<String>> records = dataTable.fetch(start, end);
        // call the fetch operation of data table
        return records;

        // DataSource datasource = find.ref(datasourceId);
        // List<List<String>> records = new ArrayList<List<String>> ();
        // DataStore store = new CSVDataStore (storePath);
        // DataTable dataTable = store.getTable (datasource.name + "");
        // if (dataTable == null) return records;
        // dataTable.openRowScan(store);
        
        // sg.edu.nus.comp.datastore.schema.Tuple tuple = dataTable.getNextRow ();
        // int row = 0;
        
        // while (tuple != null) {
        //     if (row >= start && row < end) {
        //         List<String> record = new ArrayList<String> ();
        //         String[] tmps = tuple.getValues ();
        //         for (String tmp : tmps) {
        //             record.add (tmp);
        //         }
        //         records.add (record);
        //     }
        //     tuple = dataTable.getNextRow();
        //     row ++;
        // }
        // dataTable.closeRowScan ();
        
    }

    public List<List<String>> getDataSummary (int page) throws Exception {
        DataTable dataTable = summaryStore.getTable(this.name);
        if (dataTable == null) return new ArrayList<List<String>>();
        int pageSize = Play.application().configuration().getInt("datasource.pagesize");
        int start = page * pageSize;
        int end = (page + 1) * pageSize;       
        List<List<String>> records = dataTable.fetch(start, end); 
        // call the fetch operation of data table
        return records;
        // DataSource datasource = find.ref(datasourceId);
        // List<List<String>> records = new ArrayList<List<String>> ();
        // DataStore store = new CSVDataStore (storePath);
        // DataTable dataTable = store.getTable (datasource.name + "");
        // if (dataTable == null) return records;
        // dataTable.openRowScan(store);
        
        // sg.edu.nus.comp.datastore.schema.Tuple tuple = dataTable.getNextRow ();
        // int row = 0;
        // int start = page * pageSize;
        // int end = (page + 1) * pageSize;
        // while (tuple != null) {
        //     if (row >= start && row < end) {
        //         List<String> record = new ArrayList<String> ();
        //         String[] tmps = tuple.getValues ();
        //         for (String tmp : tmps) {
        //             record.add (tmp);
        //         }
        //         records.add (record);
        //     }
        //     tuple = dataTable.getNextRow();
        //     row ++;
        // }
        // dataTable.closeRowScan ();
        // return records;
    }
}