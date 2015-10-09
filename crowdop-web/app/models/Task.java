package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;

import org.json.*;
import sg.edu.nus.comp.annotation.*;
import sg.edu.nus.comp.crowdagg.*;

@Entity
@Table(name="crowdtask")
public class Task extends Model {
    @Id
    public Long id;
    public String folder; // the group of the task, i.e., HIT
    public String token; // the token of the task
    public Boolean multiLabel;

    @Column(columnDefinition = "TEXT")
    public String htmls; // the stem of the question
    @Column(columnDefinition = "TEXT")
    public String options;

    @ManyToOne
    public Job assignedJob; // the job assigned with the task


    // @ManyToMany(cascade = CascadeType.REMOVE)
    // public List<TaskOption> options = new ArrayList<TaskOption>();

    public static Task create(String folder, String token, Boolean multiLabel, 
        String htmls, String options, Long jobID) {
        Task task = new Task(folder, token, multiLabel, htmls, options, Job.find.ref(jobID));
        task.save();
        return task;
    }


    public Task(String folder, String token, Boolean multiLabel, String htmls, String options, Job assignedJob) {
        this.folder = folder;
        this.token = token;
        this.multiLabel = multiLabel;
        this.htmls = htmls;
        this.options = options;
        this.assignedJob = assignedJob;
    }

    public int contentTupleSize() {
        JSONArray jhtmls = new JSONArray(htmls);
        return jhtmls.length();
    }

    public String getTupleHtml (int index) {
        JSONArray jhtmls = new JSONArray(htmls);
        return jhtmls.getString(index);
    } 

    // public static List<String> getSelections (Long taskId) {
    //     Task task = Task.find.ref(taskId);
    //     System.out.println("New Get selections: " + task.id + "\t" + task.result);
    //     JSONArray jresult = new JSONArray(task.result);
    //     List<String> selections = new ArrayList<String> ();
    //     for (int i = 0; i < jresult.length(); i ++) {
    //         selections.add(jresult.getString(i));
    //     }
    //     return selections;
    // }

    public List<List<String>> getOptions () {
        List<List<String>> ops = new ArrayList<List<String>> ();
        JSONArray joptions = new JSONArray (options);
        for (int i = 0; i < joptions.length(); i ++) {
            String[] op = {
                "" + joptions.getJSONObject(i).get("id"),
                "" + joptions.getJSONObject(i).get("content")
            };
            ops.add(Arrays.asList(op));
        }
        return ops;
    }

    public List<Assignment> getAssignments () {
        return Assignment.findInvolving(this.id);
    }

    public static Model.Finder<Long,Task> find = new Model.Finder(Long.class, Task.class);

    public static List<Task> findInvolving(Long jobID) {
        return find.where()
            .eq("assignedJob.id", jobID)
            .findList();
    }

    // public static void setResult (Long taskId, List<String> selections) {
    //     Task task = Task.find.ref(taskId);
    //     JSONArray jresult = new JSONArray ();
    //     for (String sel : selections) {
    //         jresult.put(sel);
    //     }
    //     task.result = "abc";//jresult.toString();
    //     System.out.println("Set task result: " + task.id + "\t" + task.result);
    //     task.update();
    //     task.save();
    // }



    public static boolean allFinished (Long jobId) {
        Job job = Job.find.ref(jobId);
        List<Task> tasks = Task.findInvolving(jobId);
        for (Task task : tasks) {
            List<Assignment> asgs = Assignment.findInvolving(task.id);
            if (asgs.size() < job.assignNum) {
                return false; 
            } 
        }
        return true;
    }

    // public static List<Annotation> collectAnnotations(Long jobId) {
    //     List<Task> tasks = findInvolving(jobId);
    //     for (Task task : tasks) {

    //     }
    // }
    // public static Map<String, String> findRowToAnswer (Long jobId) {
        
    //     Map<String, String> rowIdToAnswer = new HashMap<String, String>();
    //     for (Task task : tasks) {
    //         rowIdToAnswer.put(task.rowId, task.aggResult);
    //     }
    //     return rowIdToAnswer;
    // }

    // public String getSubObjectHtml (int index) {
    //     return objectHtmls.split("\\|\\|")[index];
    // }

    // public static void addTaskOption (Long taskId, Long optionId) {
    //     TaskOption option = TaskOption.find.ref(optionId);
    //     Task task = Task.find.ref(taskId);
    //     task.options.add(option);
    //     task.update();
    // }

    


    

    
}