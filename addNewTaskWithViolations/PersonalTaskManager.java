import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PersonalTaskManager {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(" đến hạn không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
            return null;
        }

        LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        JSONArray tasks;
        try {
            tasks = jsonHandler.loadJsonArray();
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
            return null;
        }

        for (Object obj : tasks) {
            JSONObject existingTask = (JSONObject) obj;
            if (existingTask.get("title").toString().equalsIgnoreCase(title) &&
                existingTask.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER))) {
                System.out.println(String.format("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.", title));
                return null;
            }
        }

        String taskId = UUID.randomUUID().toString();
        JSONObject newTask = new JSONObject();
        newTask.put("id", taskId);
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priorityLevel);
        newTask.put("status", "Chưa hoàn thành");
        newTask.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("last_updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        tasks.add(newTask);

        try {
            jsonHandler.saveJsonArray(tasks);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
            return null;
        }

        System.out.println(String.format("Đã thêm nhiệm vụ mới thành công với ID: %s", taskId));
        return newTask;
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        System.out.println("\nThêm nhiệm vụ hợp lệ:");
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

        System.out.println("\nThêm nhiệm vụ với tiêu đề rỗng:");
        manager.addNewTask(
            "",
            "Nhiệm vụ không có tiêu đề.",
            "2025-07-22",
            "Thấp"
        );
    }
}