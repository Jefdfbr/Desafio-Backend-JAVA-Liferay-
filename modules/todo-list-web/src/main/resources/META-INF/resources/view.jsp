<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/init.jsp" %>

<%
SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<% if (!themeDisplay.isSignedIn()) { %>

<div class="todo-login-prompt">
    <div class="todo-login-card">
        <div class="todo-logo">&#10003;</div>
        <h2>Todo List</h2>
        <p>Gerencie suas tarefas de forma simples e eficiente.</p>
        <div class="todo-login-actions">
            <a href="<%= themeDisplay.getURLSignIn() %>" class="btn-todo btn-primary">Entrar</a>
            <a href="<%= com.liferay.portal.kernel.util.PortalUtil.getCreateAccountURL(request, themeDisplay) %>" class="btn-todo btn-secondary">Criar conta</a>
        </div>
    </div>
</div>

<% } else {
    long currentUserId = themeDisplay.getUserId();
    List<Task> tasks = TaskLocalService.getTasksByUserId(currentUserId);
    int totalTasks = tasks.size();
    long completedCount = 0;
    for (Task t : tasks) { if (t.getCompleted()) completedCount++; }
    long pendingCount = totalTasks - completedCount;
%>

<div class="todo-container">

    <liferay-ui:success key="task-added"      message="task-added" />
    <liferay-ui:success key="task-updated"    message="task-updated" />
    <liferay-ui:success key="task-deleted"    message="task-deleted" />
    <liferay-ui:success key="subtask-added"   message="subtask-added" />
    <liferay-ui:success key="subtask-deleted" message="subtask-deleted" />
    <liferay-ui:error   key="not-authorized"        message="not-authorized" />
    <liferay-ui:error   key="title-required"         message="title-required" />
    <liferay-ui:error   key="subtask-title-required" message="subtask-title-required" />

    <%-- Contadores --%>
    <div class="todo-stats">
        <div class="stat-card stat-total">
            <span class="stat-number"><%= totalTasks %></span>
            <span class="stat-label">Total</span>
        </div>
        <div class="stat-card stat-pending">
            <span class="stat-number"><%= pendingCount %></span>
            <span class="stat-label">Pendentes</span>
        </div>
        <div class="stat-card stat-done">
            <span class="stat-number"><%= completedCount %></span>
            <span class="stat-label">Concluídas</span>
        </div>
    </div>

    <%-- Formulário nova tarefa --%>
    <div class="todo-add-task">
        <h3>Nova Tarefa</h3>
        <portlet:actionURL name="addTask" var="addTaskURL" />
        <aui:form action="<%= addTaskURL %>" method="post" name="addTaskForm">
            <aui:input name="title" label="Título" maxlength="200" required="true">
                <aui:validator name="required" />
                <aui:validator name="maxLength">200</aui:validator>
            </aui:input>
            <aui:input name="description" label="Descrição" type="textarea" maxlength="2000" />
            <aui:button type="submit" value="Adicionar Tarefa" cssClass="btn-todo btn-primary" />
        </aui:form>
    </div>

    <%-- Lista de tarefas --%>
    <div class="todo-task-list">
        <h3>Minhas Tarefas (<%= totalTasks %>)</h3>

        <% if (tasks.isEmpty()) { %>
        <div class="todo-empty">
            <p>Nenhuma tarefa cadastrada. Adicione a primeira acima!</p>
        </div>
        <% } %>

        <% for (Task task : tasks) {
            List<SubTask> subTasks = TaskLocalService.getSubTasksByTaskId(task.getTaskId());
            String imageUrl = null;
            if (task.getImageFileEntryId() > 0) {
                try {
                    FileEntry fe = DLAppLocalServiceUtil.getFileEntry(task.getImageFileEntryId());
                    imageUrl = "/documents/" + themeDisplay.getScopeGroupId() + "/" + fe.getFolderId() + "/" + HtmlUtil.escape(fe.getTitle());
                } catch (Exception ex) { /* arquivo removido */ }
            }
        %>

        <portlet:actionURL name="toggleComplete" var="toggleURL">
            <portlet:param name="taskId" value="<%= String.valueOf(task.getTaskId()) %>" />
        </portlet:actionURL>
        <portlet:actionURL name="deleteTask" var="deleteTaskURL">
            <portlet:param name="taskId" value="<%= String.valueOf(task.getTaskId()) %>" />
        </portlet:actionURL>
        <portlet:renderURL var="editURL">
            <portlet:param name="mvcPath" value="/edit_task.jsp" />
            <portlet:param name="taskId" value="<%= String.valueOf(task.getTaskId()) %>" />
        </portlet:renderURL>

        <div class="todo-task-card <%= task.getCompleted() ? "todo-completed" : "" %>">
            <div class="task-header">
                <a href="<%= toggleURL %>" class="task-checkbox <%= task.getCompleted() ? "checked" : "" %>"
                   title="<%= task.getCompleted() ? "Marcar como pendente" : "Marcar como concluída" %>">
                    <span class="checkbox-icon"></span>
                </a>

                <div class="task-info">
                    <h4 class="task-title"><c:out value="<%= task.getTitle() %>" /></h4>
                    <% if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) { %>
                    <p class="task-desc"><c:out value="<%= task.getDescription() %>" /></p>
                    <% } %>
                    <span class="task-date"><%= task.getCreatedAt() != null ? sdf.format(task.getCreatedAt()) : "" %></span>
                </div>

                <% if (imageUrl != null) { %>
                <div class="task-thumb">
                    <img src="<%= imageUrl %>" alt="Imagem da tarefa" class="task-thumbnail" />
                </div>
                <% } %>

                <div class="task-actions">
                    <a href="<%= editURL %>" class="btn-todo btn-edit">Editar</a>
                    <a href="<%= deleteTaskURL %>"
                       class="btn-todo btn-delete"
                       onclick="return confirm('Excluir esta tarefa e todas as subtarefas?')">Excluir</a>
                </div>
            </div>

            <%-- Subtarefas --%>
            <div class="subtask-section">
                <% if (!subTasks.isEmpty()) { %>
                <div class="subtask-list">
                    <% for (SubTask sub : subTasks) { %>

                    <portlet:actionURL name="toggleSubTask" var="toggleSubURL">
                        <portlet:param name="subTaskId" value="<%= String.valueOf(sub.getSubTaskId()) %>" />
                    </portlet:actionURL>
                    <portlet:actionURL name="deleteSubTask" var="deleteSubURL">
                        <portlet:param name="subTaskId" value="<%= String.valueOf(sub.getSubTaskId()) %>" />
                    </portlet:actionURL>

                    <div class="subtask-item <%= sub.getCompleted() ? "subtask-done" : "" %>">
                        <a href="<%= toggleSubURL %>" class="subtask-checkbox <%= sub.getCompleted() ? "checked" : "" %>">
                            <span></span>
                        </a>
                        <span class="subtask-title"><c:out value="<%= sub.getTitle() %>" /></span>
                        <a href="<%= deleteSubURL %>"
                           class="subtask-delete"
                           onclick="return confirm('Excluir subtarefa?')">&#10005;</a>
                    </div>

                    <% } %>
                </div>
                <% } %>

                <portlet:actionURL name="addSubTask" var="addSubURL">
                    <portlet:param name="taskId" value="<%= String.valueOf(task.getTaskId()) %>" />
                </portlet:actionURL>
                <form method="post" action="<%= addSubURL %>" class="add-subtask-form">
                    <div class="add-subtask-row">
                        <input type="text"
                               name="<portlet:namespace />title"
                               placeholder="Nova subtarefa..."
                               class="subtask-input"
                               maxlength="200" />
                        <button type="submit" class="btn-add-sub">+</button>
                    </div>
                </form>
            </div>
        </div>

        <% } %>
    </div>
</div>

<% } %>
