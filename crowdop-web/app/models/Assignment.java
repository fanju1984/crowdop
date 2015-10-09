package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;

import org.json.*;


@Entity
public class Assignment extends Model {

    @Id
    public Long id;

    @ManyToOne
    public Task assignedTask; // the task assigned with workers

    public String workerID;
    public String assignmentID;
    public Date acceptDateTime; // the time when the worker accepts the task
    public Date submitDateTime; // the time when the worker submits the answer

    // @ManyToMany(cascade = CascadeType.REMOVE)
    // public List<TaskOption> selectedOptions = new ArrayList<TaskOption>();
    // the options selected by the worker
    public String result;
    public String freetext; // the freetext submitted by the worker
    


    public Assignment(Task assignedTask, String workerID, String assignmentID, 
        Date acceptDateTime, Date submitDataTime, String comments, String result) {
        this.assignedTask = assignedTask;
        this.workerID = workerID;
        this.assignmentID = assignmentID;
        this.acceptDateTime = acceptDateTime;
        this.submitDateTime = submitDataTime;
        this.freetext = comments;
        this.result = result;
        //this.selectedOptions = selectedOptions;
    }

    public static Assignment create(Long assignedTaskId, String workerID, String assignmentID, 
        Date acceptDateTime, Date submitDataTime, String comments, String result) {
        Task assignedTask = Task.find.ref(assignedTaskId);
        Assignment assignment = new Assignment(assignedTask, workerID, assignmentID, 
            acceptDateTime, submitDataTime, comments, result);
        assignment.save();
        return assignment;
    }

    public String getResult () {
        return result;
    }

    public List<String> getSelections () {
        JSONArray jresult = new JSONArray(result);
        List<String> selections = new ArrayList<String> ();
        for (int i = 0; i < jresult.length(); i ++) {
            selections.add(jresult.getString(i));
        }
        return selections;
    }

    // public static void addTaskOption (Long assignmentId, Long optionId) {
    //     TaskOption option = TaskOption.find.ref(optionId);
    //     Assignment assignment = Assignment.find.ref(assignmentId);
    //     assignment.selectedOptions.add(option);
    //     assignment.update ();
    // }

    // public String getAnswerStr () {
    //     String answer = "";
    //     for (int i = 0; i < selectedOptions.size(); i ++) {
    //         answer += selectedOptions.get(i).name;
    //         if (i < selectedOptions.size() - 1) {
    //             answer += "||";
    //         }
    //     }
    //     return answer;
    // }


    public static Model.Finder<Long,Assignment> find = new Model.Finder(Long.class, Assignment.class);

    public static List<Assignment> findInvolving(Long taskID) {
        return find.where()
            .eq("assignedTask.id", taskID)
            .findList();
    }
}