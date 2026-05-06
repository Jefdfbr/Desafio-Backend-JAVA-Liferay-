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
			SessionErrors.add(r, "image-invalid");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}
		if (file.length() > MAX_IMAGE_SIZE) {
			SessionErrors.add(r, "image-too-large");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}
		if (!isValidImageMagic(file)) {
			SessionErrors.add(r, "image-invalid");
			rp.setRenderParameter("mvcPath", "/edit_task.jsp");
			rp.setRenderParameter("taskId", String.valueOf(taskId));
			return;
		}

		try {
			ServiceContext sc = ServiceContextFactory.getInstance(r);
			sc.setScopeGroupId(td.getScopeGroupId());
			FileEntry fe = DLAppLocalServiceUtil.addFileEntry(
				td.getUserId(),
				td.getScopeGroupId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				fileName,
				mimeType,
				fileName,
				"",
				"",
				file,
				sc
			);
			TaskLocalService.setTaskImage(taskId, fe.getFileEntryId());
			SessionMessages.add(r, "image-uploaded");
		} catch (Exception e) {
			_log.error("Image upload failed for taskId=" + taskId, e);
			SessionErrors.add(r, "image-upload-error");
		}

		rp.setRenderParameter("mvcPath", "/edit_task.jsp");
		rp.setRenderParameter("taskId", String.valueOf(taskId));
	}

	// -- User registration ------------------------------------------------------

	public void registerUser(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (td.isSignedIn()) { SessionErrors.add(r, "already-signed-in"); return; }
		String email = ParamUtil.getString(r, "emailAddress").trim();
		String firstName = sanitize(ParamUtil.getString(r, "firstName"), 75);
		String lastName = sanitize(ParamUtil.getString(r, "lastName"), 75);
		String password = ParamUtil.getString(r, "password1");
		String password2 = ParamUtil.getString(r, "password2");
		if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
			SessionErrors.add(r, "registration-fields-required");
			rp.setRenderParameter("mvcPath", "/register.jsp");
			return;
		}
		if (!password.equals(password2)) {
			SessionErrors.add(r, "passwords-do-not-match");
			rp.setRenderParameter("mvcPath", "/register.jsp");
			return;
		}
		try {
			ServiceContext sc = com.liferay.portal.kernel.service.ServiceContextFactory.getInstance(r);
			// Criar com autoPassword=true (senha temporaria gerada pelo Liferay)
			com.liferay.portal.kernel.model.User newUser =
				com.liferay.portal.kernel.service.UserLocalServiceUtil.addUser(
					0L, td.getCompanyId(), true, null, null,
					true, null, email, td.getLocale(),
					firstName, null, lastName,
					0L, 0L, true, 1, 1, 1970, null, 0,
					new long[0], new long[0], new long[0], new long[0], false, sc
				);
			long userId = newUser.getUserId();
			// Definir senha escolhida pelo usuario via API canonica do Liferay
			// Isso garante que o hash seja feito com o algoritmo interno correto (PBKDF2)
			// e que passwordModifiedDate seja preenchido.
			com.liferay.portal.kernel.service.UserLocalServiceUtil.updatePassword(
				userId, password, password, false
			);
			// Agora marcar termos aceitos e email verificado usando os atômicos
			com.liferay.portal.kernel.service.UserLocalServiceUtil.updateAgreedToTermsOfUse(userId, true);
			com.liferay.portal.kernel.service.UserLocalServiceUtil.updateEmailAddressVerified(userId, true);
			_log.info(auditJson("registerUser", 0, "system",
				"newUserId=" + userId + ", email=" + sanitize(email, 255)));
			rp.setRenderParameter("registrationSuccess", "true");
		} catch (com.liferay.portal.kernel.exception.UserEmailAddressException e) {
			_log.warn(auditJson("registerUser", 0, "system",
				"failed, email=" + sanitize(email, 255) + ", reason=email_already_used"));
			SessionErrors.add(r, "email-already-used");
		} catch (com.liferay.portal.kernel.exception.UserPasswordException e) {
			_log.warn(auditJson("registerUser", 0, "system",
				"failed, email=" + sanitize(email, 255) + ", reason=password_too_weak"));
			SessionErrors.add(r, "password-too-weak");
		} catch (Exception e) {
			_log.error(auditJson("registerUser", 0, "system",
				"failed, email=" + sanitize(email, 255) + ", exception=" + e.getClass().getSimpleName()));
			SessionErrors.add(r, "registration-failed");
		}
		rp.setRenderParameter("mvcPath", "/register.jsp");
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
