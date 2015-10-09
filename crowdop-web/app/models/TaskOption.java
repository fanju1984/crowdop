package models;

import java.util.*;
import javax.persistence.*;
import play.db.ebean.*;

@Entity
public class TaskOption extends Model {

    @Id
    public Long id;

    public String name; // the group of the task, i.e., HIT
    public String text; // the key of the data record

    public TaskOption(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public static TaskOption create(String name, String text) {
        TaskOption taskOption = new TaskOption(name, text);
        taskOption.save();
        return taskOption;
    }

    public static Model.Finder<Long,TaskOption> find = new Model.Finder(Long.class, TaskOption.class);
}