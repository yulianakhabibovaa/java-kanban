import manager.Managers;
import manager.TaskManager;
import tasks.Status;
import tasks.Task;
import tasks.Epic;
import tasks.SubTask;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    private static final LocalDateTime now = LocalDateTime.now();

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        System.out.println("Заводим задачи");
        Task task1 = taskManager.create(new Task("Задача 1", "Описание задачи 1", Duration.ofMinutes(30L), now.plusMinutes(30L)));
        Task task2 = taskManager.create(new Task("Задача 2", "Описание задачи 2", Duration.ofMinutes(60L), now.plusMinutes(60L)));
        Epic epic1 = taskManager.create(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask1 = taskManager.create(new SubTask("Подзадача 1", "Описание подзадачи 1", epic1, Duration.ofMinutes(60L), now.plusMinutes(60L)));
        SubTask subTask2 = taskManager.create(new SubTask("Подзадача 2", "Описание подзадачи 2", epic1, Duration.ofMinutes(60L), now.plusMinutes(60L)));
        Epic epic2 = taskManager.create(new Epic("Эпик 2", "Описание эпика 2"));
        SubTask subTask3 = taskManager.create(new SubTask("Подзадача 1", "Описание подзадачи 1", epic2, Duration.ofMinutes(60L), now.plusMinutes(60L)));
        taskManager.getEpicById(2);
        taskManager.getSubTaskById(6);
        taskManager.getTaskById(0);
        taskManager.getSubTaskById(6);
        printAllTasks(taskManager);

        System.out.println("Меняем статусы задач");
        task1 = taskManager.update(new Task(task1.getTitle(), task1.getDescription(), Status.DONE, task1.getId(), Duration.ofMinutes(60L), now.plusMinutes(60L)));
        task2 = taskManager.update(new Task(task2.getTitle(), task2.getDescription(), Status.IN_PROGRESS, task2.getId(), Duration.ofMinutes(60L), now.plusMinutes(60L)));
        subTask1 = taskManager.update(new SubTask(subTask1.getTitle(), subTask1.getDescription(), Status.DONE, subTask1.getId(), Duration.ofMinutes(60L), now.plusMinutes(60L), subTask1.getCurrentEpic()));
        subTask2 = taskManager.update(new SubTask(subTask2.getTitle(), subTask2.getDescription(), Status.IN_PROGRESS, subTask2.getId(), Duration.ofMinutes(60L), now.plusMinutes(60L), subTask2.getCurrentEpic()));
        subTask3 = taskManager.update(new SubTask(subTask3.getTitle(), subTask3.getDescription(), Status.DONE, subTask3.getId(), Duration.ofMinutes(60L), now.plusMinutes(60L), subTask3.getCurrentEpic()));

        printAllTasks(taskManager);

        System.out.println("Удаляем задачи");
        taskManager.clearTaskById(task2.getId());
        taskManager.clearSubTaskById(subTask2.getId());
        taskManager.clearEpicById(epic2.getId());

        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getEpics()) {
            System.out.println(epic);

            for (SubTask task : manager.getSubTasksByEpic(epic)) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (SubTask subtask : manager.getSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
