package models;

import java.util.*;
import javax.persistence.*;
import java.io.*;
import play.db.ebean.*;

import org.json.*;
import sg.edu.nus.comp.crowdop.core.op.*;
import sg.edu.nus.comp.crowdop.core.op.machineop.*;
import sg.edu.nus.comp.crowdop.core.op.crowdop.*;

import sg.edu.nus.comp.datastore.*;
import sg.edu.nus.comp.datastore.schema.*;
import sg.edu.nus.comp.datastore.plugin.*;
import sg.edu.nus.comp.crowdagg.*;
import sg.edu.nus.comp.annotation.*;


@Entity
public class Job extends Model {
    public static String STATE_WAITING = "waiting";
    public static String STATE_PENDING = "pending";
    public static String STATE_RUNING = "running";
    public static String STATE_COMPLETE = "completed";

    @Id
    public Long id;

    public String folder; // the folder containing the job, e.g., batch number
    public String name;
    
    @Column(columnDefinition = "TEXT")
    public String opJsonStr; // the JSON string of the corresponding operator
    public Double price; // the price for crowdsourcing
    public String state;
    public String tmp; // the downstream job
    public int assignNum;

    @ManyToOne
    public Project assignedProject; // the project assigned with the job

    //public String successor; // the downstream job

    @ManyToOne
    public DataSource outputSource;
    // the output data source

    public Job(String folder, String name, String opJsonStr, 
        Double price, Project assignedProject, DataSource outsource, String state) {
        this.folder = folder;
        this.name = name;
        this.opJsonStr = opJsonStr;
        this.price = price;
        this.assignedProject = assignedProject;
        this.outputSource = outsource;
        this.state = state;
        this.assignNum = 1;
    }

    public String getJobExplain() {
        JSONObject opJson = new JSONObject(opJsonStr);
        Op op = Op.parse(opJson);
        return op.getExplain();
    }

    public String opType() {
        JSONObject opJson = new JSONObject(opJsonStr);
        Op op = Op.parse(opJson);
        return op.getType();
    }

    public String getRequirement (Task task) {
        JSONObject opJson = new JSONObject(opJsonStr);
        Op op = Op.parse(opJson);
        if (op instanceof CrowdSelect) {
            CrowdSelect sop = (CrowdSelect) op;
            return Predicate.printPredicates(sop.getPredicates());
        } else if (op instanceof CrowdJoin) {
            CrowdJoin jop = (CrowdJoin) op;
            return Predicate.printPredicates(jop.getConditions());
        } else if (op instanceof CrowdFill){
            CrowdFill fop = (CrowdFill) op;
            String[] tmps = task.token.split("_");
            int fieldIndex = Integer.parseInt(tmps[0]);
            String attr = fop.getAttributeNames().get(fieldIndex);
            return attr;
        }
        return null;
    }

    public Job getSuccessor () {
        if (tmp != null) {
            Long successorId = Long.parseLong(tmp);
            Job succ = Job.find.ref(successorId);
            return succ;
        }
        return null;
    }

    public static Job create(String folder, String name, String opJsonStr, 
        Double price, Long projectID, Long outsourceId, String state) {
        Project project = Project.find.ref(projectID);
        DataSource outsource = DataSource.find.ref(outsourceId);
        Job job = new Job (folder, name, opJsonStr, price, project, outsource, state);
        job.save();
        return job;
    }

    public static void setOutputSource (Long jobId, DataSource datasource) {
        Job job = Job.find.ref(jobId);
        job.outputSource = datasource;
        job.update ();
    }


    public static void setSuccessor (Long jobId, Long successorId) {
        Job job = Job.find.ref(jobId);
        job.tmp = "" + successorId;
        job.update ();
        job.save();
        
    }

    public static Model.Finder<Long,Job> find = new Model.Finder(Long.class, Job.class);

    public static List<Job> findInvolving(Long projectID) {
        return find.where()
            .eq("assignedProject.id", projectID)
            .findList();
    }

    /**
     * Retrieve datasources.
     */
    public static List<Job> findBySuccessor(Long succJobId) {
        return find.where().eq("tmp", succJobId + "").findList();
    }

    public String execute (boolean doCrowdsource) throws Exception {
        JSONObject opJson = new JSONObject(opJsonStr);
        // Prepare the input data sources
        Op op = Op.parse(opJson);
        List<DataTable> inTables = new ArrayList<DataTable>();
        List<DataSource> inDataSources = new ArrayList<DataSource>();
        if (op instanceof Relation) { // the first machine-based job
            String inName = ((Relation)op).tableName;
            DataTable inTable = DataSource.dataStore.getTable(inName);
            inDataSources.add(DataSource.findByName(inName));
            inTables.add(inTable);
        }
        List<Job> prevJobs = Job.findBySuccessor(this.id);
        for (Job prevJob : prevJobs) {
            DataTable inTable = prevJob.outputSource.getDataTable();
            if (inTable == null) {
                prevJob.outputSource.createDataTable();
                inTable = prevJob.outputSource.getDataTable();
            }
            inTables.add(inTable);
            inDataSources.add(prevJob.outputSource);
        }

        
        if (doCrowdsource) {
            List<Annotation> annos = op.seekForAnnotation(inTables);
            if (annos == null) { // no need to do crowdsourcing
                return execute(false);
            }
            createJobTasks(inDataSources, annos, this.id);
            return STATE_RUNING;
        } else {
            DataTable outTable = outputSource.getDataTable();
            List<Annotation> annos = collectAnnotations(this.id);
            
            op.execute(inTables, outTable, annos);
            // Update the front-end properties of output table
            DataSource.updateHtmlTemplate (outputSource.id, 
                inDataSources.get(0).htmlTemplate);
            return STATE_COMPLETE;
        }
        
        
        // List<String> inputTables = new ArrayList<String> ();
        

        // File folder = new File(tmpfolder);
        //     if (!folder.exists()) folder.mkdir();
        // DataStore dataStore = new CSVDataStore(datafolder);
        // DataStore tmpStore = new CSVDataStore(tmpfolder);

        
        // if (op instanceof Relation) {
        //     Relation rop = (Relation) op;
        //     rop.execute(dataStore, tmpStore, "" + outputSource.id);
        //     return STATE_COMPLETE;
        // } else if (op instanceof CrowdOp) {
        //     CrowdOp cop = (CrowdOp) op;
        //     List<String> dataTables = cop.getDataTables();
        //     List<DataSource> dataSources = new ArrayList<DataSource>();
        //     for (String dataTable : dataTables) {
        //         DataSource datasource = DataSource.findByName(dataTable);
        //         dataSources.add(datasource);
        //     }
        //     if (doCrowdsource) {
        //         Map<String, sg.edu.nus.comp.datastore.schema.Tuple> rowToTuple = 
        //             cop.crowdsource (tmpStore, inputTables);
        //         List<Long> taskOptions = new ArrayList<Long>();
        //         if (cop instanceof CrowdSelect) {
        //             taskOptions.add(TaskOption.create("1", "Yes, it is").id);
        //             taskOptions.add(TaskOption.create("0", "No, it isn't").id);
        //         } else if (cop instanceof CrowdJoin) {
        //             taskOptions.add(TaskOption.create("1", "Yes, they are").id);
        //             taskOptions.add(TaskOption.create("0", "No, they aren't").id);
        //         } else if (cop instanceof CrowdFill) {
        //             Map<String, Set<String>> attrToDomain = 
        //                 this.getAttributeDomains();
        //             for (String attr : attrToDomain.keySet()) {
        //                 Set<String> domain = attrToDomain.get(attr);
        //                 for (String value : domain) {
        //                     taskOptions.add(TaskOption.create(attr + "::" + value, value).id);
        //                 }
        //             }
        //         }
        //         createJobTasks(dataSources, rowToTuple, this.id, datafolder, taskOptions);
        //         return STATE_RUNING;
        //     } else {
        //         Map<String, String> rowIdToAnswer = Task.findRowToAnswer(this.id);
        //         if (outputSource.id == assignedProject.destDataSource.id) {
        //             System.out.println("Flush to " + outputSource.name);
        //             cop.execute(tmpStore, inputTables, dataStore, outputSource.name, rowIdToAnswer);
        //         } else{
        //             cop.execute(tmpStore, inputTables, tmpStore, "" + outputSource.id, rowIdToAnswer);
        //         }
        //         return STATE_COMPLETE;
        //     }
        // }
        // return "Undefined";
    }

    /*
    *  This function create tasks based on the crowdsourced tuples
    */
    public static void createJobTasks (List<DataSource> inDataSources, 
        List<Annotation> annos, Long jobId)  throws Exception {
        System.out.println("Create Job Tasks: " + annos.size());
        if (annos == null) return; // no need to create tasks
        for (Annotation anno : annos) {
            JSONArray jhtmls = new JSONArray();
            JSONArray jOptions = new JSONArray();
            String token;
            for (int i = 0; i < anno.contentTuples.size(); i ++) {
                DataSource datasource = inDataSources.get(i);
                String html = instanceTemplate(datasource.htmlTemplate, 
                        datasource.getHeaders(), anno.contentTuples.get(i), datasource);
                jhtmls.put(html);
            }
            if (anno.contentTuples.size() == inDataSources.size() - 1) {
                // there should be a data source for rendering options
                DataSource datasource = inDataSources.get(inDataSources.size() - 1);
                for (int i = 0; i < anno.optionTuples.size(); i ++) {
                    String html = instanceTemplate(datasource.htmlTemplate, 
                        datasource.getHeaders(), anno.optionTuples.get(i), datasource);
                    JSONObject joption = new JSONObject();
                    joption.put("id", anno.optionTuples.get(i).tid);
                    joption.put("content", html);
                    jOptions.put(joption);
                }
            } else {
                for (int i = 0; i < anno.optionTuples.size(); i ++) {
                    String optionId;
                    if (anno.optionTuples.get(i).tid == null) {
                        optionId = anno.optionTuples.get(i).getValue(0);
                    } else {
                        optionId = String.valueOf(anno.optionTuples.get(i).tid);
                    }
                    JSONObject joption = new JSONObject();
                    joption.put("id", optionId);
                    joption.put("content", optionId);
                    jOptions.put(joption);
                }
            }
            Task task = Task.create("HIT", anno.token, anno.multiLabel, jhtmls.toString(), 
                jOptions.toString(), jobId);
        }
    }

    /*
     * Collect the annotation result from all the tasks
     */
    public static List<Annotation> collectAnnotations (Long jobId) {
        Job job = Job.find.ref(jobId);
        List<Task> tasks = Task.findInvolving(jobId);
        Map<Long, Map<String, Annotation>> taskToWorkerToAnswer = 
            new HashMap<Long, Map<String, Annotation>> ();
        for (Task task : tasks) {
            List<Assignment> asgs = Assignment.findInvolving(task.id);
            Map<String, Annotation> workerToAnswer = 
                new HashMap<String, Annotation> ();
            for (Assignment asg : asgs) {
                List<String> selections = asg.getSelections();
                Annotation anno = new Annotation(task.token, task.multiLabel, selections);
                workerToAnswer.put(asg.workerID, anno);
            }
            taskToWorkerToAnswer.put(task.id, workerToAnswer);
        }
        Map<Long, Annotation> taskToResult = 
                MajorityVotingAggregator.aggregate(taskToWorkerToAnswer);
        List<Annotation> annos = new ArrayList<Annotation>();
        for (Long task : taskToResult.keySet()) {
            taskToResult.get(task).token = Task.find.ref(task).token;
            annos.add(taskToResult.get(task));
        }
        return annos;

    }

    private static String instanceTemplate (String htmlTemplate, List<String> headers, 
        sg.edu.nus.comp.datastore.schema.Tuple tuple, DataSource inputTable) {
        String html = new String(htmlTemplate);
        int col = 0;
        for (String header : headers) {
            String var = "\\$\\{" + header + "\\}";
            String value = tuple.getValue(col);
            html = html.replaceAll(var, value);
            col ++;
        }
        return html;
    }



  


    // public Map<String, Set<String>> getAttributeDomains () {
    //     JSONObject opJson = new JSONObject(opJsonStr);
    //     Op op = Op.parse(opJson);
    //     if (op instanceof CrowdFill) {
    //         List<String> attrs = ((CrowdFill)op).attributes;
    //         List<Set<String>> attrDomains = 
    //             ((CrowdFill)op).attrDomains;
    //         Map<String, Set<String>> attrToDomain = 
    //             new HashMap<String, Set<String>> ();
    //         for (int i = 0; i < attrs.size(); i ++) {
    //             String attr = attrs.get(i);
    //             Set<String> domain = attrDomains.get(i);
    //             attrToDomain.put(attr, domain);
    //         }
    //         return attrToDomain;
    //     }
    //     return null;
    // }

    // public List<String> getFillAttributes () {
    //     JSONObject opJson = new JSONObject(opJsonStr);
    //     Op op = Op.parse(opJson);
    //     if (op instanceof CrowdFill) {
    //         return ((CrowdFill)op).attributes;
    //     } 
    //     return null;
    // }

    // public String getPredicateStr() {
    //     JSONObject opJson = new JSONObject(opJsonStr);
    //     Op op = Op.parse(opJson);
    //     if (op instanceof CrowdSelect) {
    //         return ((CrowdSelect)op).printPredicates();
    //     } else if (op instanceof CrowdJoin) {
    //         return ((CrowdJoin)op).printPredicates();
    //     }
    //     return "";
    // }

    

    

    /*
    * Update job status if necessary
    */

    /*
    * Check if all the tasks in the job completed
    */



    

    
}