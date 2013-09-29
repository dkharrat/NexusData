package org.example.todo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import ch.qos.logback.classic.Logger;
import org.nexusdata.core.ObjectContext;

public class NewTaskActivity extends Activity {

    EditText titleText, notesText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_task);

        titleText = (EditText)findViewById(R.id.title);
        notesText = (EditText)findViewById(R.id.notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.form_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        boolean result = super.onOptionsItemSelected(item);
        if (validateForm()) {
            saveTask();
            result = true;
        }
        return result;
    }

    private boolean validateForm() {
        return !TextUtils.isEmpty(titleText.getText());
    }

    private void saveTask() {
        String title = titleText.getText().toString();
        String notes = notesText.getText().toString();

        ObjectContext ctx = TodoApp.getMainObjectContext();

        Task newTask = ctx.newObject(Task.class);
        newTask.setTitle(title);
        newTask.setNotes(notes);

        ctx.save();

        finish();
    }
}
