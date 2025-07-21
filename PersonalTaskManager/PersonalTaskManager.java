import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] VALID_PRIORITIES = {"Thấp", "Trung bình", "Cao"};
    private final JsonFileHandler jsonHandler;

    // Lớp TaskManagerException
    public static class TaskManagerException extends Exception {
        public TaskManagerException(String message) {
            super(message);
        }

        public TaskManagerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Lớp JsonFileHandler
    public static class JsonFileHandler {
        private final String filePath;

        public JsonFileHandler(String filePath) {
            this.filePath = filePath;
        }

        public JSONArray loadJsonArray() throws TaskManagerException {
            JSONParser parser = new JSONParser();
            try (FileReader reader = new FileReader(filePath)) {
                Object obj = parser.parse(reader);
                if (obj instanceof JSONArray) {
                    return (JSONArray) obj;
                }
                return new JSONArray();
            } catch (IOException | ParseException e) {
                throw new TaskManagerException("Lỗi khi đọc file database", e);
            }
        }

        public void saveJsonArray(JSONArray data) throws TaskManagerException {
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(data.toJSONString());
                file.flush();
            } catch (IOException e) {
                throw new TaskManagerException("Lỗi khi ghi vào file database", e);
            }
        }
    }

    // Lớp Task
    public static class Task {
        private long id;
        private String title;
        private String description;
        private LocalDate dueDate;
        private String priority;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime lastUpdatedAt;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

        public Task(long id, String title, String description, LocalDate dueDate, String priority) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.priority = priority;
            this.status = "Chưa hoàn thành";
            this.createdAt = LocalDateTime.now();
            this.lastUpdatedAt = LocalDateTime.now();
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("title", title);
            json.put("description", description);
            json.put("due_date", dueDate.format(DATE_FORMATTER));
            json.put("priority", priority);
            json.put("status", status);
            json.put("created_at", createdAt.format(DATE_TIME_FORMATTER));
            json.put("last_updated_at", lastUpdatedAt.format(DATE_TIME_FORMATTER));
            return json;
        }

        public String getTitle() {
            return title;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }
    }

    // Constructor của PersonalTaskManager
    public PersonalTaskManager() {
        this.jsonHandler = new JsonFileHandler(DB_FILE_PATH);
    }

    private String validateTaskInput(String title, String dueDateStr, String priorityLevel) {
        if (title == null || title.trim().isEmpty()) {
            return "Lỗi: Tiêu đề không được để trống.";
        }
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            return "Lỗi: Ngày đến hạn không được để trống.";
        }
        try {
            LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return "Lỗi: Ngày đến hạn không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.";
        }
        if (!Arrays.asList(VALID_PRIORITIES).contains(priorityLevel)) {
            return "Lỗi: Mức độ ưu tiên không hợp lệ. Vui lòng chọn từ: Thấp, Trung bình, Cao.";
        }
        return null;
    }

    public JSONObject addNewTask(String title, String description,
                                 String dueDateStr, String priorityLevel) throws TaskManagerException {
        String validationError = validateTaskInput(title, dueDateStr, priorityLevel);
        if (validationError != null) {
            throw new TaskManagerException(validationError);
        }

        LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        JSONArray tasks = jsonHandler.loadJsonArray();

        for (Object obj : tasks) {
            JSONObject existingTask = (JSONObject) obj;
            if (existingTask.get("title").toString().equalsIgnoreCase(title) &&
                existingTask.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER))) {
                throw new TaskManagerException(
                    String.format("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.", title));
            }
        }

        long taskId = tasks.size() + 1;
        Task newTask = new Task(taskId, title, description, dueDate, priorityLevel);
        tasks.add(newTask.toJson());
        jsonHandler.saveJsonArray(tasks);

        System.out.println(String.format("Đã thêm nhiệm vụ mới thành công với ID: %d", taskId));
        return newTask.toJson();
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        try {
            System.out.println("\ carcere:\nThêm nhiệm vụ hợp lệ:");
            manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
            );

            System.out.println("\nThêm nhiệm vụ trùng lặp:");
            manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
            );
        } catch (TaskManagerException e) {
            System.err.println(e.getMessage());
        }

        try {
            System.out.println("\nThêm nhiệm vụ với tiêu đề rỗng:");
            manager.addNewTask(
                "",
                "Nhiệm vụ không có tiêu đề.",
                "2025-07-22",
                "Thấp"
            );
        } catch (TaskManagerException e) {
            System.err.println(e.getMessage());
        }
    }
}