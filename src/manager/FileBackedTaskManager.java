package manager;

import exceptions.ManagerImportTaskException;
import exceptions.ManagerReadSaveFileException;
import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static tasks.Task.DATE_TIME_FORMATER;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final String SCV_HEAD = "id,type,name,status,description,duration,startTime,endTime,epic\n";
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
                fw.write(TaskUtils.toString(task));
            }
            for (Epic epic : epics.values()) {
                fw.write(TaskUtils.toString(epic));
            }
            for (SubTask subTask : subTasks.values()) {
                fw.write(TaskUtils.toString(subTask));
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
        int id = Integer.parseInt(split[0]);
        TaskType type = TaskType.valueOf(split[1]);
        String title = split[2];
        String description = split[4];
        Status status = Status.valueOf(split[3]);
        Duration duration = Duration.ofMinutes(Long.parseLong(split[5]));
        LocalDateTime startTime = LocalDateTime.parse(split[6], DATE_TIME_FORMATER);
        LocalDateTime endTime = LocalDateTime.parse(split[7], DATE_TIME_FORMATER);
        int epicId = 0;
        if (split.length == 9) {
            epicId = Integer.parseInt(split[8]);
        }

        switch (type) {
            case TASK -> {
                Task task = new Task(title, description, status, id, duration, startTime);
                tasks.put(task.getId(), task);
                updateLastId(task.getId());
                if (task.getStartTime() != null) {
                    prioritisedTasks.add(task.copy());
                }
                return;
            }
            case EPIC -> {
                Epic epic = new Epic(title, description, status, id, new ArrayList<>(), duration, startTime, endTime);
                epics.put(epic.getId(), epic);
                updateLastId(epic.getId());
                return;
            }
            case SUBTASK -> {
                SubTask subTask = new SubTask(title, description, status, id, duration, startTime, epics.get(epicId));
                epics.get(subTask.getCurrentEpic().getId()).addSubTask(subTask);
                subTasks.put(subTask.getId(), subTask);
                updateLastId(subTask.getId());
                if (subTask.getStartTime() != null) {
                    prioritisedTasks.add(subTask.copy());
                }
                return;
            }
        }
        throw new ManagerImportTaskException("Не удалось считать задачу из строки: " + saveLine);
    }

    private void updateLastId(int id) {
        if (id >= lastId) {
            lastId = id + 1;
        }
    }
}
