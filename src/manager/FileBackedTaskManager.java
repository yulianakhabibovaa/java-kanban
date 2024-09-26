package manager;

import exceptions.ManagerImportTaskException;
import exceptions.ManagerReadSaveFileException;
import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static tasks.TaskType.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final String SCV_HEAD = "id,type,name,status,description,epic\n";
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        super();
        this.saveFile = saveFile;
        initializeState();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearTaskById(Integer id) {
        super.clearTaskById(id);
        save();
    }

    @Override
    public void clearSubTaskById(Integer id) {
        super.clearSubTaskById(id);
        save();
    }

    @Override
    public void clearEpicById(Integer id) {
        super.clearEpicById(id);
        save();
    }

    @Override
    public Task create(Task task) {
        Task result = super.create(task);
        save();
        return result;
    }

    @Override
    public Task update(Task task) {
        Task result = super.update(task);
        save();
        return result;
    }

    @Override
    public Epic create(Epic epic) {
        Epic result = super.create(epic);
        save();
        return result;
    }

    @Override
    public Epic update(Epic epic) {
        Epic result = super.update(epic);
        save();
        return result;
    }

    @Override
    public SubTask create(SubTask subTask) {
        SubTask result = super.create(subTask);
        save();
        return result;
    }

    @Override
    public SubTask update(SubTask subTask) {
        SubTask result = super.update(subTask);
        save();
        return result;
    }

    private void save() {
        try (FileWriter fw = new FileWriter(saveFile, false)) {
            fw.write(SCV_HEAD);
            for (Task task : tasks.values()) {
                fw.write(toString(task));
            }
            for (Epic epic : epics.values()) {
                fw.write(toString(epic));
            }
            for (SubTask subTask : subTasks.values()) {
                fw.write(toString(subTask));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении: " + e);
        }
    }

    private void initializeState() {
        Path saveFilePath = saveFile.toPath();
        if (Files.exists(saveFilePath)) {
            try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
                if (br.ready() && br.readLine().equals(SCV_HEAD.trim())) {
                    while (br.ready()) {
                        String taskLine = br.readLine();
                        importTaskFromString(taskLine);
                    }
                }
            } catch (IOException e) {
                throw new ManagerReadSaveFileException("Не удалось прочитать файл сохранения: " + e);
            }
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file);
    }

    private void importTaskFromString(String saveLine) {
        String[] split = saveLine.split(",");
        switch (TaskType.valueOf(split[1])) {
            case TASK -> {
                Task task = new Task(split[2], split[4], Status.valueOf(split[3]), Integer.parseInt(split[0]));
                tasks.put(task.getId(), task);
                updateLastId(task.getId());
                return;
            }
            case EPIC -> {
                Epic epic = new Epic(split[2], split[4], Status.valueOf(split[3]), Integer.parseInt(split[0]), new ArrayList<>());
                epics.put(epic.getId(), epic);
                updateLastId(epic.getId());
                return;
            }
            case SUBTASK -> {
                SubTask subTask = new SubTask(split[2], split[4], Status.valueOf(split[3]), Integer.parseInt(split[0]), epics.get(Integer.parseInt(split[5])));
                epics.get(subTask.getCurrentEpic().getId()).addSubTask(subTask);
                subTasks.put(subTask.getId(), subTask);
                updateLastId(subTask.getId());
                return;
            }
        }
        throw new ManagerImportTaskException("Не удалось считать задачу из строки: " + saveLine);
    }

    private String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,\n", task.getId(), TASK, task.getTitle(), task.getStatus(), task.getDescription());
    }

    private String toString(SubTask task) {
        return String.format("%s,%s,%s,%s,%s,%s\n", task.getId(), SUBTASK, task.getTitle(), task.getStatus(), task.getDescription(), task.getCurrentEpic().getId());
    }

    private String toString(Epic task) {
        return String.format("%s,%s,%s,%s,%s,\n", task.getId(), EPIC, task.getTitle(), task.getStatus(), task.getDescription());
    }

    private void updateLastId(int id) {
        if (id > lastId) {
            lastId = id + 1;
        }
    }
}
