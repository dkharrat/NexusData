// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package org.example.todo;

import java.util.Date;

import com.github.dkharrat.nexusdata.core.ManagedObject;

class _Task extends ManagedObject {

    public interface Property {
        final static String TITLE = "title";
        final static String NOTES = "notes";
        final static String DUE_BY = "dueBy";
        final static String COMPLETED = "completed";
        final static String PRIORITY = "priority";
        final static String ASSIGNED_TO = "assignedTo";
    }

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW,
    }

    public String getTitle() {
        return (String)getValue(Property.TITLE);
    }

    public void setTitle(String title) {
        setValue(Property.TITLE, title);
    }

    public String getNotes() {
        return (String)getValue(Property.NOTES);
    }

    public void setNotes(String notes) {
        setValue(Property.NOTES, notes);
    }

    public Date getDueBy() {
        return (Date)getValue(Property.DUE_BY);
    }

    public void setDueBy(Date dueBy) {
        setValue(Property.DUE_BY, dueBy);
    }

    public boolean isCompleted() {
        return (Boolean)getValue(Property.COMPLETED);
    }

    public void setCompleted(boolean completed) {
        setValue(Property.COMPLETED, completed);
    }

    public Priority getPriority() {
        return (Priority)getValue(Property.PRIORITY);
    }

    public void setPriority(Priority priority) {
        setValue(Property.PRIORITY, priority);
    }


    public User getAssignedTo() {
        return (User)getValue(Property.ASSIGNED_TO);
    }

    public void setAssignedTo(User assignedTo) {
        setValue(Property.ASSIGNED_TO, assignedTo);
    }

}
