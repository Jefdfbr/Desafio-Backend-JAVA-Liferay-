package com.seatecnologia.todo.web.portlet;

import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.seatecnologia.todo.model.SubTask;
import com.seatecnologia.todo.model.Task;
import com.seatecnologia.todo.service.TaskLocalService;
import java.io.File;
import java.util.Date;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.tools",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.display-name=Todo List",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=com_seatecnologia_todo_web_portlet_TodoListPortlet",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class TodoListPortlet extends MVCPortlet {

	private static final Log _log = LogFactoryUtil.getLog(TodoListPortlet.class);
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

	// -- Task actions -----------------------------------------------------------

	public void addTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		String title = ParamUtil.getString(r, "title").trim();
		if (title.isEmpty() || title.length() > 200) { SessionErrors.add(r, "title-required"); return; }
		String desc = sanitize(ParamUtil.getString(r, "description"), 2000);
		TaskLocalService.addTask(td.getUserId(), title, desc, new Date());
		SessionMessages.add(r, "task-added");
		sendRedirect(r, rp);
	}

	public void editTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long taskId = ParamUtil.getLong(r, "taskId");
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		String title = ParamUtil.getString(r, "title").trim();
		if (title.isEmpty() || title.length() > 200) { SessionErrors.add(r, "title-required"); return; }
		String desc = sanitize(ParamUtil.getString(r, "description"), 2000);
		TaskLocalService.updateTask(taskId, title, desc);
		SessionMessages.add(r, "task-updated");
		rp.setRenderParameter("mvcPath", "/edit_task.jsp");
		rp.setRenderParameter("taskId", String.valueOf(taskId));
	}

	public void deleteTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long taskId = ParamUtil.getLong(r, "taskId");
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		TaskLocalService.deleteTask(taskId);
		SessionMessages.add(r, "task-deleted");
		sendRedirect(r, rp);
	}

	public void toggleComplete(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long taskId = ParamUtil.getLong(r, "taskId");
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		TaskLocalService.toggleTaskCompleted(taskId);
		sendRedirect(r, rp);
	}

	// -- SubTask actions --------------------------------------------------------

	public void addSubTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long taskId = ParamUtil.getLong(r, "taskId");
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		String title = ParamUtil.getString(r, "title").trim();
		if (title.isEmpty() || title.length() > 200) { SessionErrors.add(r, "subtask-title-required"); return; }
		TaskLocalService.addSubTask(taskId, title);
		SessionMessages.add(r, "subtask-added");
		sendRedirect(r, rp);
	}

	public void editSubTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long subTaskId = ParamUtil.getLong(r, "subTaskId");
		long taskId = ParamUtil.getLong(r, "taskId");
		SubTask subTask = TaskLocalService.getSubTask(subTaskId);
		if (subTask == null || subTask.getTaskId() != taskId) { SessionErrors.add(r, "not-authorized"); return; }
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		String title = ParamUtil.getString(r, "title").trim();
		if (title.isEmpty() || title.length() > 200) { SessionErrors.add(r, "subtask-title-required"); return; }
		TaskLocalService.updateSubTask(subTaskId, title);
		SessionMessages.add(r, "subtask-updated");
		rp.setRenderParameter("mvcPath", "/edit_task.jsp");
		rp.setRenderParameter("taskId", String.valueOf(taskId));
	}

	public void toggleSubTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long subTaskId = ParamUtil.getLong(r, "subTaskId");
		SubTask subTask = TaskLocalService.getSubTask(subTaskId);
		if (subTask == null) { SessionErrors.add(r, "not-authorized"); return; }
		Task task = TaskLocalService.getTask(subTask.getTaskId());
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		TaskLocalService.toggleSubTaskCompleted(subTaskId);
		sendRedirect(r, rp);
	}

	public void deleteSubTask(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long subTaskId = ParamUtil.getLong(r, "subTaskId");
		SubTask subTask = TaskLocalService.getSubTask(subTaskId);
		if (subTask == null) { SessionErrors.add(r, "not-authorized"); return; }
		Task task = TaskLocalService.getTask(subTask.getTaskId());
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }
		TaskLocalService.deleteSubTask(subTaskId);
		SessionMessages.add(r, "subtask-deleted");
		sendRedirect(r, rp);
	}

	// -- Image upload -----------------------------------------------------------

	public void uploadImage(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (!td.isSignedIn()) { SessionErrors.add(r, "not-signed-in"); return; }
		long taskId = ParamUtil.getLong(r, "taskId");
		Task task = TaskLocalService.getTask(taskId);
		if (task == null || task.getUserId() != td.getUserId()) { SessionErrors.add(r, "not-authorized"); return; }

		UploadPortletRequest upload = PortalUtil.getUploadPortletRequest(r);
		File file = upload.getFile("imageFile");
		String fileName = upload.getFileName("imageFile");
		String mimeType = upload.getContentType("imageFile");

		if (file == null || !file.exists() || file.length() == 0 || fileName == null || fileName.isEmpty()) {
			rp.setRenderParameter("uploadError", "image-invalid");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}
		if (file.length() > MAX_IMAGE_SIZE) {
			rp.setRenderParameter("uploadError", "image-too-large");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}
		if (!isValidImageMagic(file)) {
			rp.setRenderParameter("uploadError", "image-invalid");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}

		// Nome único para evitar DuplicateFileEntryException no Document Library
		String uniqueName = "task_" + taskId + "_" + System.currentTimeMillis() + "_" + fileName;
		try {
			ServiceContext sc = ServiceContextFactory.getInstance(r);
			sc.setScopeGroupId(td.getScopeGroupId());
			FileEntry fe = DLAppLocalServiceUtil.addFileEntry(
				td.getUserId(),
				td.getScopeGroupId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				uniqueName,
				mimeType,
				uniqueName,
				"",
				"",
				file,
				sc
			);
			TaskLocalService.setTaskImage(taskId, fe.getFileEntryId());
			SessionMessages.add(r, "image-uploaded");
		} catch (Exception e) {
			_log.error("Image upload failed for taskId=" + taskId, e);
			rp.setRenderParameter("uploadError", "image-upload-error");
		}

		rp.setRenderParameter("mvcPath", "/edit_task.jsp");
		rp.setRenderParameter("taskId", String.valueOf(taskId));
	}

	// -- Helpers ----------------------------------------------------------------

	private String auditJson(String action, long userId, String email, String details) {
		return String.format(
			"{\"ts\":\"%s\",\"action\":\"%s\",\"userId\":%d,\"email\":\"%s\",\"details\":\"%s\"}",
			new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()),
			action, userId, com.seatecnologia.todo.util.TodoUtil.sanitize(email, 255),
			com.seatecnologia.todo.util.TodoUtil.sanitize(details, 500)
		);
	}

	private boolean isValidImageMagic(File file) {
		return com.seatecnologia.todo.util.TodoUtil.isValidImageMagic(file);
	}

	private String sanitize(String input, int maxLength) {
		return com.seatecnologia.todo.util.TodoUtil.sanitize(input, maxLength);
	}

}
