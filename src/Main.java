import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(
                new Task("Пройти теорию спринта", "Изучить материалы по ООП в Практикуме"));
        Task task2 = manager.createTask(
                new Task("Задать вопрос наставнику", "Уточнить про наследование в Java"));

        Epic projectEpic = manager.createEpic(
                new Epic("Подготовка к проектной работе", "Выполнение задания по трекеру задач"));
        Subtask sub1 = manager.createSubtask(
                new Subtask("Проанализировать ТЗ", "Разобрать требования к проекту",
                        projectEpic.getId()));
        Subtask sub2 = manager.createSubtask(
                new Subtask("Спроектировать классы", "Создать UML-диаграмму",
                        projectEpic.getId()));

        Epic codeEpic = manager.createEpic(
                new Epic("Реализация проекта", "Написание кода для трекера задач"));
        Subtask sub3 = manager.createSubtask(
                new Subtask("Реализовать TaskManager", "Написать класс для управления задачами",
                        codeEpic.getId()));

        printAllTasks(manager);

        System.out.println("Обновляю статусы учебных задач");
        task1.setStatus(TaskStatus.DONE);
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        printAllTasks(manager);

        System.out.println("Архивирую выполненные задачи");
        manager.deleteTask(task1.getId());
        manager.deleteEpic(projectEpic.getId());

        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Текущие учебные задачи:");
        System.out.println("Обычные задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("Учебные эпики:");
        manager.getAllEpics().forEach(System.out::println);

        System.out.println("Подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        manager.getAllEpics().forEach(epic -> {
            System.out.printf("Подзадачи эпика :", epic.getTitle());
            manager.getSubtasksByEpic(epic.getId()).forEach(System.out::println);
        });
        System.out.println("История :");
        manager.getHistory().forEach(System.out::println);
    }
}