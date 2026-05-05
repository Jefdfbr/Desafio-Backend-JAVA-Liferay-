<%@ include file="/init.jsp" %>

<%
if (!themeDisplay.isSignedIn()) {
    response.sendRedirect(themeDisplay.getURLSignIn());
    return;
}

long taskId = ParamUtil.getLong(request, "taskId");
Task task = TaskLocalService.getTask(taskId);

if (task == null || task.getUserId() != themeDisplay.getUserId()) {
%>
<div class="todo-container">
    <div class="todo-alert todo-alert-error">Tarefa não encontrada ou acesso negado.</div>
    <portlet:renderURL var="backURL" />
    <a href="<%= backURL %>" class="btn-todo btn-secondary">&#8592; Voltar</a>
</div>
<%
    return;
}

List<SubTask> subTasks = TaskLocalService.getSubTasksByTaskId(taskId);

String imageUrl = null;
if (task.getImageFileEntryId() > 0) {
    try {
        FileEntry fe = DLAppLocalServiceUtil.getFileEntry(task.getImageFileEntryId());
        imageUrl = "/documents/" + themeDisplay.getScopeGroupId() + "/" + fe.getFolderId() + "/" + HtmlUtil.escape(fe.getTitle());
    } catch (Exception ex) { /* arquivo removido */ }
}
%>

<div class="todo-container">

    <portlet:renderURL var="backURL" />
    <a href="<%= backURL %>" class="btn-todo btn-secondary todo-back">&#8592; Voltar para lista</a>

    <liferay-ui:success key="task-updated"    message="task-updated" />
    <liferay-ui:success key="subtask-updated" message="subtask-updated" />
    <liferay-ui:success key="subtask-added"   message="subtask-added" />
    <liferay-ui:success key="subtask-deleted" message="subtask-deleted" />
    <liferay-ui:success key="image-uploaded"  message="image-uploaded" />
    <liferay-ui:error   key="title-required"          message="title-required" />
    <liferay-ui:error   key="subtask-title-required"  message="subtask-title-required" />
    <liferay-ui:error   key="not-authorized"          message="not-authorized" />
    <liferay-ui:error   key="image-invalid"           message="image-invalid" />
    <liferay-ui:error   key="image-too-large"         message="image-too-large" />
    <liferay-ui:error   key="image-upload-error"      message="image-upload-error" />

    <%-- Editar tarefa --%>
    <div class="todo-card">
        <h3>Editar Tarefa</h3>
        <portlet:actionURL name="editTask" var="editTaskURL" />
        <aui:form action="<%= editTaskURL %>" method="post" name="editTaskForm">
            <aui:input name="taskId" type="hidden" value="<%= taskId %>" />
            <aui:input name="title" label="Título" value="<%= task.getTitle() %>" maxlength="200" required="true">
                <aui:validator name="required" />
                <aui:validator name="maxLength">200</aui:validator>
            </aui:input>
            <aui:input name="description" label="Descrição" type="textarea" maxlength="2000"
                       value="<%= task.getDescription() != null ? task.getDescription() : \"\" %>" />
            <aui:button type="submit" value="Salvar Alterações" cssClass="btn-todo btn-primary" />
        </aui:form>
    </div>

    <%-- Upload de imagem --%>
    <div class="todo-card">
        <h3>Imagem da Tarefa</h3>
        <% if (imageUrl != null) { %>
        <div class="current-image">
            <p class="image-label">Imagem atual:</p>
            <img src="<%= imageUrl %>" alt="Imagem da tarefa" class="edit-image-preview" />
        </div>
        <% } %>
        <portlet:actionURL name="uploadImage" var="uploadURL" />
        <aui:form action="<%= uploadURL %>" method="post" enctype="multipart/form-data" name="uploadImageForm">
            <aui:input name="taskId" type="hidden" value="<%= taskId %>" />
            <aui:input name="imageFile" type="file"
                       label="Selecionar imagem (JPEG, PNG ou GIF — máx. 5 MB)" />
            <aui:button type="submit"
                        value="<%= imageUrl != null ? \"Substituir Imagem\" : \"Enviar Imagem\" %>"
                        cssClass="btn-todo btn-secondary" />
        </aui:form>
    </div>

    <%-- Subtarefas --%>
    <div class="todo-card">
        <h3>Subtarefas (<%= subTasks.size() %>)</h3>

        <% if (subTasks.isEmpty()) { %>
        <p class="todo-empty-sub">Nenhuma subtarefa ainda.</p>
        <% } else { %>
        <div class="subtask-edit-list">
            <% for (SubTask sub : subTasks) { %>

            <portlet:actionURL name="toggleSubTask" var="toggleSubURL">
                <portlet:param name="subTaskId" value="<%= String.valueOf(sub.getSubTaskId()) %>" />
            </portlet:actionURL>
            <portlet:actionURL name="deleteSubTask" var="deleteSubURL">
                <portlet:param name="subTaskId" value="<%= String.valueOf(sub.getSubTaskId()) %>" />
            </portlet:actionURL>
            <portlet:actionURL name="editSubTask" var="editSubURL" />

            <div class="subtask-edit-item <%= sub.getCompleted() ? "subtask-done" : "" %>">
                <a href="<%= toggleSubURL %>" class="subtask-checkbox <%= sub.getCompleted() ? "checked" : "" %>">
                    <span></span>
                </a>
                <form method="post" action="<%= editSubURL %>" class="subtask-edit-form">
                    <input type="hidden" name="<portlet:namespace />subTaskId" value="<%= sub.getSubTaskId() %>" />
                    <input type="hidden" name="<portlet:namespace />taskId"    value="<%= taskId %>" />
                    <input type="text"
                           name="<portlet:namespace />title"
                           value="<%= HtmlUtil.escape(sub.getTitle()) %>"
                           class="subtask-edit-input <%= sub.getCompleted() ? "subtask-done-text" : "" %>"
                           maxlength="200" />
                    <button type="submit" class="btn-todo btn-edit-sm">&#10003;</button>
                </form>
                <a href="<%= deleteSubURL %>"
                   class="btn-todo btn-delete-sm"
                   onclick="return confirm('Excluir subtarefa?')">&#10005;</a>
            </div>

            <% } %>
        </div>
        <% } %>

        <portlet:actionURL name="addSubTask" var="addSubURL">
            <portlet:param name="taskId" value="<%= String.valueOf(taskId) %>" />
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
