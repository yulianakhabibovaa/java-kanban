import tasks.Status;
import tasks.Task;
import tasks.Epic;
import tasks.SubTask;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        System.out.println("Заводим задачи");
        Task task1 = taskManager.create(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = taskManager.create(new Task("Задача 2", "Описание задачи 2"));
        Epic epic1 = taskManager.create(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask1 = taskManager.create(new SubTask("Подзадача 1", "Описание подзадачи 1", epic1));
        SubTask subTask2 = taskManager.create(new SubTask("Подзадача 2", "Описание подзадачи 2", epic1));
        Epic epic2 = taskManager.create(new Epic("Эпик 2", "Описание эпика 2"));
        SubTask subTask3 = taskManager.create(new SubTask("Подзадача 1", "Описание подзадачи 1", epic2));

        System.out.println("Задачи : " + taskManager.getTasks());
        System.out.println("Эпики : " + taskManager.getEpics());
        System.out.println("Подзадачи : " + taskManager.getSubTasks());

        System.out.println("Меняем статусы задач");
        task1 = taskManager.update(new Task(task1.getTitle(), task1.getDescription(), Status.DONE, task1.getId()));
        task2 = taskManager.update(new Task(task2.getTitle(), task2.getDescription(), Status.IN_PROGRESS, task2.getId()));
        subTask1 = taskManager.update(new SubTask(subTask1.getTitle(), subTask1.getDescription(), Status.DONE, subTask1.getId(), subTask1.getCurrentEpic()));
        subTask2 = taskManager.update(new SubTask(subTask2.getTitle(), subTask2.getDescription(), Status.IN_PROGRESS, subTask2.getId(), subTask2.getCurrentEpic()));
        subTask3 = taskManager.update(new SubTask(subTask3.getTitle(), subTask3.getDescription(), Status.DONE, subTask3.getId(), subTask3.getCurrentEpic()));

        System.out.println("Задачи : " + taskManager.getTasks());
        System.out.println("Эпики : " + taskManager.getEpics());
        System.out.println("Подзадачи : " + taskManager.getSubTasks());

        System.out.println("Удаляем задачи");
        taskManager.clearTaskById(task2.getId());
        taskManager.clearTaskById(subTask2.getId());
        taskManager.clearTaskById(epic2.getId());

        System.out.println("Задачи : " + taskManager.getTasks());
        System.out.println("Эпики : " + taskManager.getEpics());
        System.out.println("Подзадачи : " + taskManager.getSubTasks());
    }
}
