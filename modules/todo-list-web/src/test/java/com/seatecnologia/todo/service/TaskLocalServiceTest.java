package com.seatecnologia.todo.service;

import com.seatecnologia.todo.model.SubTask;
import com.seatecnologia.todo.model.Task;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios de TaskLocalService usando H2 in-memory (sem Liferay).
 * Cobre: Task CRUD, SubTask CRUD, isolamento por userId/taskId, ordenacao.
 */
class TaskLocalServiceTest {

    private static javax.sql.DataSource ds;

    @BeforeAll
    static void setUpSchema() throws Exception {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:todo_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        h2.setUser("sa");
        h2.setPassword("");
        ds = h2;
        TaskLocalService._dataSource = ds;

        try (Connection cn = ds.getConnection()) {
            cn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS Todo_Task (" +
                "  taskId BIGINT PRIMARY KEY," +
                "  userId BIGINT NOT NULL," +
                "  title VARCHAR(200) NOT NULL," +
                "  description VARCHAR(2000)," +
                "  completed BOOLEAN NOT NULL DEFAULT FALSE," +
                "  imageFileEntryId BIGINT DEFAULT 0," +
                "  createdAt TIMESTAMP," +
                "  modifiedAt TIMESTAMP" +
                ")"
            );
            cn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS Todo_SubTask (" +
                "  subTaskId BIGINT PRIMARY KEY," +
                "  taskId BIGINT NOT NULL," +
                "  title VARCHAR(200) NOT NULL," +
                "  completed BOOLEAN NOT NULL DEFAULT FALSE," +
                "  createdAt TIMESTAMP" +
                ")"
            );
        }
    }

    @AfterAll
    static void tearDownAll() {
        TaskLocalService._dataSource = null;
    }

    @BeforeEach
    void clearData() throws Exception {
        try (Connection cn = ds.getConnection()) {
            cn.createStatement().execute("DELETE FROM Todo_SubTask");
            cn.createStatement().execute("DELETE FROM Todo_Task");
        }
    }

    // ── Task: addTask ─────────────────────────────────────────────────────────

    @Test
    void addTask_retornaTaskComIdPositivo() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Minha Tarefa", "Descricao", new Date());
        assertNotNull(t);
        assertTrue(t.getTaskId() > 0);
        assertEquals(1L, t.getUserId());
        assertEquals("Minha Tarefa", t.getTitle());
        assertEquals("Descricao", t.getDescription());
        assertFalse(t.getCompleted());
        assertEquals(0L, t.getImageFileEntryId());
    }

    @Test
    void addTask_descricaoNula_armazenaVazia() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Sem Desc", null, new Date());
        Task found = TaskLocalService.getTask(t.getTaskId());
        assertNotNull(found);
        assertNotNull(found.getDescription()); // armazenado como ""
    }

    @Test
    void addTask_idsCrescentes() throws Exception {
        Task t1 = TaskLocalService.addTask(1L, "T1", null, new Date());
        Task t2 = TaskLocalService.addTask(1L, "T2", null, new Date());
        assertTrue(t2.getTaskId() > t1.getTaskId());
    }

    // ── Task: getTask ─────────────────────────────────────────────────────────

    @Test
    void getTask_existente_retornaDadosPersistidos() throws Exception {
        Task added = TaskLocalService.addTask(1L, "Buscar", "Desc", new Date());
        Task found = TaskLocalService.getTask(added.getTaskId());
        assertNotNull(found);
        assertEquals("Buscar", found.getTitle());
        assertEquals("Desc", found.getDescription());
        assertEquals(1L, found.getUserId());
    }

    @Test
    void getTask_inexistente_retornaNull() throws Exception {
        assertNull(TaskLocalService.getTask(99999L));
    }

    // ── Task: updateTask ──────────────────────────────────────────────────────

    @Test
    void updateTask_alteraTituloEDescricao() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Antes", "Desc1", new Date());
        TaskLocalService.updateTask(t.getTaskId(), "Depois", "Desc2");
        Task updated = TaskLocalService.getTask(t.getTaskId());
        assertEquals("Depois", updated.getTitle());
        assertEquals("Desc2", updated.getDescription());
    }

    @Test
    void updateTask_descricaoNula_armazenaVazia() throws Exception {
        Task t = TaskLocalService.addTask(1L, "T", "Original", new Date());
        TaskLocalService.updateTask(t.getTaskId(), "T", null);
        Task updated = TaskLocalService.getTask(t.getTaskId());
        assertNotNull(updated.getDescription());
    }

    // ── Task: deleteTask ──────────────────────────────────────────────────────

    @Test
    void deleteTask_removeTask() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Remover", null, new Date());
        TaskLocalService.deleteTask(t.getTaskId());
        assertNull(TaskLocalService.getTask(t.getTaskId()));
    }

    @Test
    void deleteTask_cascataSubTasks() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Com Subs", null, new Date());
        TaskLocalService.addSubTask(t.getTaskId(), "Sub1");
        TaskLocalService.addSubTask(t.getTaskId(), "Sub2");
        TaskLocalService.deleteTask(t.getTaskId());
        assertNull(TaskLocalService.getTask(t.getTaskId()));
        assertTrue(TaskLocalService.getSubTasksByTaskId(t.getTaskId()).isEmpty());
    }

    // ── Task: toggleTaskCompleted ─────────────────────────────────────────────

    @Test
    void toggleTaskCompleted_inverteFlagDuasVezes() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Toggle", null, new Date());
        assertFalse(TaskLocalService.getTask(t.getTaskId()).getCompleted());

        TaskLocalService.toggleTaskCompleted(t.getTaskId());
        assertTrue(TaskLocalService.getTask(t.getTaskId()).getCompleted());

        TaskLocalService.toggleTaskCompleted(t.getTaskId());
        assertFalse(TaskLocalService.getTask(t.getTaskId()).getCompleted());
    }

    // ── Task: setTaskImage ────────────────────────────────────────────────────

    @Test
    void setTaskImage_atualizaFileEntryId() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Imagem", null, new Date());
        assertEquals(0L, t.getImageFileEntryId());
        TaskLocalService.setTaskImage(t.getTaskId(), 77L);
        assertEquals(77L, TaskLocalService.getTask(t.getTaskId()).getImageFileEntryId());
    }

    // ── Task: getTasksByUserId ────────────────────────────────────────────────

    @Test
    void getTasksByUserId_retornaApenasDoUsuario() throws Exception {
        TaskLocalService.addTask(1L, "U1-A", null, new Date());
        TaskLocalService.addTask(1L, "U1-B", null, new Date());
        TaskLocalService.addTask(2L, "U2-A", null, new Date());

        List<Task> result = TaskLocalService.getTasksByUserId(1L);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(task -> task.getUserId() == 1L));
    }

    @Test
    void getTasksByUserId_pendenteAntesDeConcluidaOrdenacao() throws Exception {
        Task concluida = TaskLocalService.addTask(1L, "Concluida", null, new Date());
        TaskLocalService.addTask(1L, "Pendente", null, new Date());
        TaskLocalService.toggleTaskCompleted(concluida.getTaskId());

        List<Task> tasks = TaskLocalService.getTasksByUserId(1L);
        assertEquals(2, tasks.size());
        assertFalse(tasks.get(0).getCompleted());
        assertTrue(tasks.get(1).getCompleted());
    }

    @Test
    void getTasksByUserId_semTarefas_retornaListaVazia() throws Exception {
        assertTrue(TaskLocalService.getTasksByUserId(999L).isEmpty());
    }

    // ── Task: getTasksCountByUserIdAndCompleted ───────────────────────────────

    @Test
    void getTasksCountByUserIdAndCompleted_contaPendentesConcluidas() throws Exception {
        TaskLocalService.addTask(1L, "P1", null, new Date());
        TaskLocalService.addTask(1L, "P2", null, new Date());
        Task done = TaskLocalService.addTask(1L, "D1", null, new Date());
        TaskLocalService.toggleTaskCompleted(done.getTaskId());

        assertEquals(2, TaskLocalService.getTasksCountByUserIdAndCompleted(1L, false));
        assertEquals(1, TaskLocalService.getTasksCountByUserIdAndCompleted(1L, true));
    }

    @Test
    void getTasksCountByUserIdAndCompleted_semTarefas_retornaZero() throws Exception {
        assertEquals(0, TaskLocalService.getTasksCountByUserIdAndCompleted(999L, false));
        assertEquals(0, TaskLocalService.getTasksCountByUserIdAndCompleted(999L, true));
    }

    // ── SubTask: addSubTask ───────────────────────────────────────────────────

    @Test
    void addSubTask_persisteERetorna() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask s = TaskLocalService.addSubTask(t.getTaskId(), "Subtarefa");
        assertNotNull(s);
        assertTrue(s.getSubTaskId() > 0);
        assertEquals(t.getTaskId(), s.getTaskId());
        assertEquals("Subtarefa", s.getTitle());
        assertFalse(s.getCompleted());
    }

    // ── SubTask: getSubTask ───────────────────────────────────────────────────

    @Test
    void getSubTask_existente_retornaDados() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask added = TaskLocalService.addSubTask(t.getTaskId(), "SubX");
        SubTask found = TaskLocalService.getSubTask(added.getSubTaskId());
        assertNotNull(found);
        assertEquals("SubX", found.getTitle());
        assertEquals(t.getTaskId(), found.getTaskId());
    }

    @Test
    void getSubTask_inexistente_retornaNull() throws Exception {
        assertNull(TaskLocalService.getSubTask(99999L));
    }

    // ── SubTask: updateSubTask ────────────────────────────────────────────────

    @Test
    void updateSubTask_alteraTitulo() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask s = TaskLocalService.addSubTask(t.getTaskId(), "Antes");
        TaskLocalService.updateSubTask(s.getSubTaskId(), "Depois");
        assertEquals("Depois", TaskLocalService.getSubTask(s.getSubTaskId()).getTitle());
    }

    // ── SubTask: deleteSubTask ────────────────────────────────────────────────

    @Test
    void deleteSubTask_removeSubTask() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask s = TaskLocalService.addSubTask(t.getTaskId(), "Sub");
        TaskLocalService.deleteSubTask(s.getSubTaskId());
        assertNull(TaskLocalService.getSubTask(s.getSubTaskId()));
    }

    @Test
    void deleteSubTask_naoAfetaOutrasSubTasks() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask s1 = TaskLocalService.addSubTask(t.getTaskId(), "Sub1");
        SubTask s2 = TaskLocalService.addSubTask(t.getTaskId(), "Sub2");
        TaskLocalService.deleteSubTask(s1.getSubTaskId());
        assertNull(TaskLocalService.getSubTask(s1.getSubTaskId()));
        assertNotNull(TaskLocalService.getSubTask(s2.getSubTaskId()));
    }

    // ── SubTask: toggleSubTaskCompleted ───────────────────────────────────────

    @Test
    void toggleSubTaskCompleted_inverteFlagDuasVezes() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        SubTask s = TaskLocalService.addSubTask(t.getTaskId(), "Sub");
        assertFalse(TaskLocalService.getSubTask(s.getSubTaskId()).getCompleted());

        TaskLocalService.toggleSubTaskCompleted(s.getSubTaskId());
        assertTrue(TaskLocalService.getSubTask(s.getSubTaskId()).getCompleted());

        TaskLocalService.toggleSubTaskCompleted(s.getSubTaskId());
        assertFalse(TaskLocalService.getSubTask(s.getSubTaskId()).getCompleted());
    }

    // ── SubTask: getSubTasksByTaskId ──────────────────────────────────────────

    @Test
    void getSubTasksByTaskId_retornaSubTasksDaTarefa() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Pai", null, new Date());
        TaskLocalService.addSubTask(t.getTaskId(), "Sub1");
        TaskLocalService.addSubTask(t.getTaskId(), "Sub2");
        List<SubTask> subs = TaskLocalService.getSubTasksByTaskId(t.getTaskId());
        assertEquals(2, subs.size());
        assertTrue(subs.stream().allMatch(s -> s.getTaskId() == t.getTaskId()));
    }

    @Test
    void getSubTasksByTaskId_semSubTasks_retornaVazio() throws Exception {
        Task t = TaskLocalService.addTask(1L, "Sem Sub", null, new Date());
        assertTrue(TaskLocalService.getSubTasksByTaskId(t.getTaskId()).isEmpty());
    }

    @Test
    void getSubTasksByTaskId_isoladoPorTaskId() throws Exception {
        Task t1 = TaskLocalService.addTask(1L, "Pai1", null, new Date());
        Task t2 = TaskLocalService.addTask(1L, "Pai2", null, new Date());
        TaskLocalService.addSubTask(t1.getTaskId(), "Sub de T1");
        assertTrue(TaskLocalService.getSubTasksByTaskId(t2.getTaskId()).isEmpty());
    }
}
