package hr.bilac.josip.tasker.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.DateTimeComparator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import hr.bilac.josip.tasker.R;
import hr.bilac.josip.tasker.database.model.Task;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {

    private Context context;
    private List<Task> taskList;


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView task;
        public TextView dot;
        public TextView timestamp;
        public TextView duedate;
        public TextView finished;
        public RelativeLayout task_row_layout;

        public MyViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.task);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
            duedate = view.findViewById(R.id.duedate);
            task_row_layout = view.findViewById(R.id.task_list_row_layout);
        }
    }

    public TasksAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Task task = taskList.get(position);


        holder.task.setText(task.getTask());


        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Formatting and displaying timestamp
        holder.timestamp.setText("Created: " + task.getTimestamp());

        holder.duedate.setText("Due: " + task.getDuedate());

        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
        Date myDateOne = formatStringToDate(task.getDuedate());
        Date myDateTwo = formatStringToDate(Calendar.getInstance().getTime().toString());

        int retVal = dateTimeComparator.compare(myDateOne, myDateTwo);
        if(retVal < 0 && task.getFinished().equals("no")) {
            holder.task_row_layout.setBackgroundColor(Color.parseColor("#fde4e5"));
        } else if (retVal == 0 && task.getFinished().equals("no")) {
            holder.duedate.setPaintFlags(holder.duedate.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            holder.duedate.setTextColor(Color.parseColor("#FF9800"));
        }

        if (task.getFinished() != null && (taskList.indexOf(task) == position)) {
            if (task.getFinished().equals("yes")) {
                holder.task.setPaintFlags(holder.task.getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM dd yyyy");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }

    private Date formatStringToDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("MMM dd yyyy");
            Date date = fmt.parse(dateStr);
            return date;
        } catch (ParseException e) {

        }
        return null;
    }
}
