// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package org.example.todo;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _User extends ManagedObject {

    public interface Property {
        final static String NAME = "name";
        final static String TASKS = "tasks";
    }


    public String getName() {
        return (String)getValue(Property.NAME);
    }

    public void setName(String name) {
        setValue(Property.NAME, name);
    }


    @SuppressWarnings("unchecked")
    public Set<Task> getTasks() {
        return (Set<Task>)getValue(Property.TASKS);
    }

    public void setTasks(Set<Task> tasks) {
        setValue(Property.TASKS, tasks);
    }

    public void addTask(Task task) {
        getTasks().add(task);
    }
}
