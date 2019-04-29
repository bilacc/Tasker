package hr.bilac.josip.tasker.database.model;

public class Task {
    public static final String TABLE_NAME = "tasks";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DUEDATE = "duedate";
    public static final String COLUMN_FINISHED = "finished";

    private int id;
    private String task;
    private String timestamp;
    private String duedate;
    private String finished;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TASK + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_DUEDATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_FINISHED + " TEXT DEFAULT 'no'"
                    + ")";

    public Task() {
    }

    public Task(int id, String task, String timestamp, String duedate) {
        this.id = id;
        this.task = task;
        this.timestamp = timestamp;
        this.duedate = duedate;
        this.finished = "no";
    }

    public String getDuedate() {
        return duedate;
    }

    public String getFinished() {
        return finished;
    }

    public void setFinished(String finished) {
        this.finished = finished;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public int getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
