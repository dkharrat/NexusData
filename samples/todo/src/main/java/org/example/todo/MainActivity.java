package org.example.todo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.nexusdata.core.ObjectContext;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private ListView listView;
    private EntryListAdapter<Task> listAdapter;

    private static final int ADD_TASK = 1;
    private static final int DELETE_TASK = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
        List<Task> tasks = ctx.findAll(Task.class);

        if (listAdapter == null) {
            listAdapter = new EntryListAdapter<Task>(this, tasks) {
                @Override
                protected String getTitleViewText(Task task) {
                    return task.getTitle();
                }
                @Override
                protected String getDetailViewText(Task task) {
                    return task.getNotes();
                }
            };
            listView.setAdapter(listAdapter);
        } else {
            listAdapter.clear();
            listAdapter.addAll(tasks);
            listAdapter.notifyDataSetChanged();
        }
    }

    public abstract class EntryListAdapter<T> extends ArrayAdapter<T> {

        public final static int NONE_SELECTED = -1;
        private int m_selectedItem = NONE_SELECTED;

        public EntryListAdapter(Context context, List<T> entries) {
            super(context, 0, entries);
        }

        public void setSelectedItem(int position) {
            m_selectedItem = position;
            notifyDataSetChanged();
        }

        public int getSelectedItem() {
            return m_selectedItem;
        }

        abstract protected String getTitleViewText(T entry);
        abstract protected String getDetailViewText(T entry);

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            T entry = getItem(position);

            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.list_item_with_detail, null);

            TextView titleView = (TextView)view.findViewById(R.id.list_item_title);
            TextView detailsView = (TextView)view.findViewById(R.id.list_item_detail);

            titleView.setText(getTitleViewText(entry) + "2");
            detailsView.setText(getDetailViewText(entry));

            return view;
        }
    }
}
