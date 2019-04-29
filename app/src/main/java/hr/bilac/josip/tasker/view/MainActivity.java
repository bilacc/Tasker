package hr.bilac.josip.tasker.view;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTimeComparator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import hr.bilac.josip.tasker.R;
import hr.bilac.josip.tasker.database.DatabaseHelper;
import hr.bilac.josip.tasker.database.model.Task;
import hr.bilac.josip.tasker.utils.MyDividerItemDecoration;
import hr.bilac.josip.tasker.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity {
    private TasksAdapter mAdapter;
    private List<Task> taskList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noTasksView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noTasksView = findViewById(R.id.empty_tasks_view);

        db = new DatabaseHelper(this);

        taskList.addAll(db.getAllTasks());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTaskDialog(false, null, -1);
            }
        });

        mAdapter = new TasksAdapter(this, taskList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyTasks();

        //Long press on recyclerview item opens alert dialog with options to edit delete or finish task.
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        final Handler updateSeekBarHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                  mAdapter.notifyDataSetChanged();

                    updateSeekBarHandler.postDelayed(this, 60000);
                }

        };
        updateSeekBarHandler.post(runnable);
    }

    //Insert new task in db and refresh list
    private void createTask(String task, String duedate, String timestamp) {
        // inserting task in db and getting
        // newly inserted task id
        long id = db.insertTask(task, duedate, timestamp);

        // get the newly inserted task from db
        Task n = db.getTask(id);

        if (n != null) {
            // adding new task to array list at 0 position
            taskList.add(0, n);
            // refreshing the list
            mAdapter.notifyDataSetChanged();
            toggleEmptyTasks();
        }
    }

    //set task status to finished in db and refresh list
    private void finishTask(String finished, int position) {
        Task n = taskList.get(position);
        n.setFinished(finished);

        db.finishTask(n, finished);

        taskList.set(position, n);
        //mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();

        toggleEmptyTasks();
    }

    //Update task in db & update item in the list accordingly
    private void updateTask(String task, String duedate, int position) {
        Task n = taskList.get(position);
        // updating task text
        n.setTask(task);
        n.setDuedate(duedate);

        // updating task in db
        db.updateTask(n, formatDateWithYear(duedate));

        // refreshing the list
        taskList.set(position, n);
        mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();

        toggleEmptyTasks();
    }

    //Delete task from db and remove item from the list accordingly
    private void deleteTask(int position) {
        // deleting the task from db
        db.deleteTask(taskList.get(position));

        // removing the task from the list
        taskList.remove(position);
        playSound(R.raw.throw_trash);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyDataSetChanged();

        toggleEmptyTasks();
    }

    //Opens dialog with edit, delete, finish options
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete", "Finish/Unfinish task"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showTaskDialog(true, taskList.get(position), position);
                } else if (which == 1) {
                    deleteTask(position);
                } else {
                    if (taskList.get(position).getFinished() != null) {
                        if (taskList.get(position).getFinished().equals("yes")) {
                            finishTask("no", position);

                        } else {
                            finishTask("yes", position);
                            playSound(R.raw.applause);
                        }
                    }
                }
            }
        });
        builder.show();
    }

    //Show alert to input task and duedate (datepicker dialog)
    private void showTaskDialog(final boolean shouldUpdate, final Task task, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        final View view = layoutInflaterAndroid.inflate(R.layout.task_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputTask = view.findViewById(R.id.task);
        final TextView dueDate = view.findViewById(R.id.date);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_task_title) : getString(R.string.lbl_edit_task_title));

        if (task == null) {
            dueDate.setHint(getResources().getString(R.string.duedate_hint));
        } else {
            dueDate.setText(task.getDuedate().toString());
        }

        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();

                DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                dueDate.setText(formatDateWithYear(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth));

                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                dpd.show();
            }
        });

        if (shouldUpdate && task != null) {
            inputTask.setText(task.getTask());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
                Date myDateOne = formatStringToDate(dueDate.getText().toString());
                Date myDateTwo = formatStringToDate(Calendar.getInstance().getTime().toString());

                int retVal = dateTimeComparator.compare(myDateOne, myDateTwo);


                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputTask.getText().toString())) {
                    playSound(R.raw.glass_bottle);
                    Toast.makeText(MainActivity.this, "You must enter task.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty((dueDate.getText().toString()))) {
                    playSound(R.raw.glass_bottle);
                    Toast.makeText(MainActivity.this, "You must enter due date.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (retVal < 0) {
                    playSound(R.raw.glass_bottle);
                    Toast.makeText(MainActivity.this, "Due date shouldn't be set earlier than today", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating task
                if (shouldUpdate && task != null) {
                    // update task by it's id
                    updateTask(inputTask.getText().toString(), dueDate.getText().toString(), position);

                } else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy");
                    String timestamp = simpleDateFormat.format(new Date());
                    // create new task
                    createTask(inputTask.getText().toString(), dueDate.getText().toString(), timestamp);
                    playSound(R.raw.write_pencil);



                }


            }
        });
    }

    //toggle list and empty tasks view
    private void toggleEmptyTasks() {
        // you can check tasksList.size() > 0

        if (db.getTasksCount() > 0) {
            noTasksView.setVisibility(View.GONE);
        } else {
            noTasksView.setVisibility(View.VISIBLE);
        }
    }

    private Date formatStringToDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("MMM d yyy");
            Date date = fmt.parse(dateStr);
            return date;
        } catch (ParseException e) {

        }
        return null;
    }

    private String formatDateWithYear(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d yyyy");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }

    private void removeFinishedTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            // deleting the task from db
            if (taskList.get(i).getFinished().equals("yes")) {
                db.deleteTask(taskList.get(i));
                taskList.remove(i);
                i = i - 1;
            }

        }
        playSound(R.raw.throw_trash);
        mAdapter.notifyDataSetChanged();
        toggleEmptyTasks();
    }

    private void playSound(int sound) {
        final MediaPlayer mp;
        mp = MediaPlayer.create(getApplicationContext(), sound);
        mp.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Delete all finished tasks");
                alertDialog.setMessage("Are you sure you want to delete all finished tasks?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeFinishedTasks();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return true;
            case R.id.action_sort_duedate_asc:
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task o1, Task o2) {
                        return o1.getDuedate().compareTo(o2.getDuedate());
                    }
                });
                recyclerView.setAdapter(mAdapter);
                return true;
            case R.id.action_sort_duedate_descending:
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task o1, Task o2) {
                        return o1.getDuedate().compareTo(o2.getDuedate());
                    }
                });
                Collections.reverse(taskList);
                recyclerView.setAdapter(mAdapter);
                return true;
            case R.id.action_sort_finished:
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task o1, Task o2) {
                        return o1.getFinished().compareTo(o2.getFinished());
                    }
                });
                recyclerView.setAdapter(mAdapter);
                return true;
            case R.id.action_exit:
                finishAndRemoveTask();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
