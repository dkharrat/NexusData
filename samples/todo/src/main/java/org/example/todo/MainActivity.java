package org.example.todo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import com.github.dkharrat.nexusdata.core.FetchRequest;
import com.github.dkharrat.nexusdata.core.ObjectContext;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private enum DisplayMode {
        TODO,
        COMPLETED
    }

    private ListView listView;
    private TaskListAdapter listAdapter;

    private static final int ADD_TASK = 1;

    private DisplayMode displayMode = DisplayMode.TODO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Arrays.asList("Todo", "Completed")), new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (itemPosition == 0) {
                    displayMode = DisplayMode.TODO;
                } else {
                    displayMode = DisplayMode.COMPLETED;
                }
                refreshUI();
                return true;
            }
        });

        listView = (ListView)findViewById(R.id.todo_tasks_list);

        refreshUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, ADD_TASK, Menu.NONE, "Add")
                .setIcon(android.R.drawable.ic_menu_add)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
        boolean result = super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case ADD_TASK: {
                Intent intent = new Intent(this, NewTaskActivity.class);
                startActivity(intent);

                break;
            }
        }

        return result;
    }

    private void refreshUI() {
        ObjectContext ctx = TodoApp.getMainObjectContext();

        boolean displayCompleted = (displayMode == DisplayMode.COMPLETED);
        FetchRequest<Task> fetchRequest = ctx.newFetchRequestBuilder(Task.class)
                .predicate("completed == "+displayCompleted)
                .build();
        List<Task> tasks = ctx.executeFetchOperation(fetchRequest);

        if (listAdapter == null) {
            listAdapter = new TaskListAdapter(this, tasks);
            listView.setAdapter(listAdapter);
        } else {
            listAdapter.clear();
            listAdapter.addAll(tasks);
            listAdapter.notifyDataSetChanged();
        }
    }

    public class TaskListAdapter extends ArrayAdapter<Task> {

        Handler handler = new Handler();

        public TaskListAdapter(Context context, List<Task> entries) {
            super(context, 0, entries);
        }

        private void setStrikeThroughView(TextView textView, boolean strikeThrough) {
            if (strikeThrough) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final Task task = getItem(position);

            final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View view = inflater.inflate(R.layout.list_item_with_detail, null);

            final TextView titleView = (TextView)view.findViewById(R.id.list_item_title);
            final TextView detailsView = (TextView)view.findViewById(R.id.list_item_detail);
            final CheckBox completedCheckbox = (CheckBox)view.findViewById(R.id.checkbox_completed);
            completedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    task.setCompleted(isChecked);
                    if (task.isCompleted()) {
                        setStrikeThroughView(titleView, true);
                        setStrikeThroughView(detailsView, true);
                    } else {
                        setStrikeThroughView(titleView, false);
                        setStrikeThroughView(detailsView, false);
                    }
                    TodoApp.getMainObjectContext().save();

                    if (displayMode == DisplayMode.COMPLETED && !task.isCompleted() ||
                            displayMode == DisplayMode.TODO && task.isCompleted()) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                listAdapter.remove(task);
                                listAdapter.notifyDataSetChanged();
                            }
                        }, 1500);
                    }
                }
            });

            titleView.setText(task.getTitle());
            detailsView.setText(task.getNotes());
            completedCheckbox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                setStrikeThroughView(titleView, true);
                setStrikeThroughView(detailsView, true);
            }

            return view;
        }
    }
}
