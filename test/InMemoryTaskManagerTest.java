import manager.InMemoryTaskManager;
import manager.TaskManagerTest;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}
