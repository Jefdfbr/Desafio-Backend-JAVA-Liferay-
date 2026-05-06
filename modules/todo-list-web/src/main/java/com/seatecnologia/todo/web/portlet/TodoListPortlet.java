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
import java.io.FileInputStream;
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

	// ── Task actions ──────────────────────────────────────────────────────────

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

	// ── SubTask actions ───────────────────────────────────────────────────────

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

	// ── Image upload ──────────────────────────────────────────────────────────

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

	// ── User registration ─────────────────────────────────────────────────────

	public void registerUser(ActionRequest r, ActionResponse rp) throws Exception {
		ThemeDisplay td = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);
		if (td.isSignedIn()) { SessionErrors.add(r, "already-signed-in"); return; }
		String email = ParamUtil.getString(r, "emailAddress").trim();
		String firstName = sanitize(ParamUtil.getString(r, "firstName"), 75);
		String lastName = sanitize(ParamUtil.getString(r, "lastName"), 75);
		String password = ParamUtil.getString(r, "password1");
		if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty()) {
			SessionErrors.add(r, "registration-fields-required");
			rp.setRenderParameter("mvcPath", "/register.jsp");
			return;
		}
		try {
			ServiceContext sc = com.liferay.portal.kernel.service.ServiceContextFactory.getInstance(r);
			com.liferay.portal.kernel.model.User newUser =
				com.liferay.portal.kernel.service.UserLocalServiceUtil.addUser(
					0L, td.getCompanyId(), false, password, password,
					true, null, email, td.getLocale(),
					firstName, null, lastName,
					0L, 0L, true, 1, 1, 1970, null, 0,
					new long[0], new long[0], new long[0], new long[0], false, sc
				);
			// Ensure user can log in immediately without terms-of-use interstitial
			newUser.setAgreedToTermsOfUse(true);
			com.liferay.portal.kernel.service.UserLocalServiceUtil.updateUser(newUser);
			rp.setRenderParameter("registrationSuccess", "true");
		} catch (com.liferay.portal.kernel.exception.UserEmailAddressException e) {
			SessionErrors.add(r, "email-already-used");
		} catch (com.liferay.portal.kernel.exception.UserPasswordException e) {
			SessionErrors.add(r, "password-too-weak");
		} catch (Exception e) {
			_log.error("Registration failed for email=" + email, e);
			SessionErrors.add(r, "registration-failed");
		}
		rp.setRenderParameter("mvcPath", "/register.jsp");
	}

	// ── Helpers ───────────────────────────────────────────────────────────────

	private boolean isValidImageMagic(File file) {
		byte[] magic = new byte[4];
		try (FileInputStream fis = new FileInputStream(file)) {
			if (fis.read(magic) < 3) return false;
		} catch (Exception e) {
			return false;
		}
		// JPEG: FF D8 FF
		if ((magic[0] & 0xFF) == 0xFF && (magic[1] & 0xFF) == 0xD8 && (magic[2] & 0xFF) == 0xFF) return true;
		// PNG: 89 50 4E 47
		if ((magic[0] & 0xFF) == 0x89 && (magic[1] & 0xFF) == 0x50 && (magic[2] & 0xFF) == 0x4E && (magic[3] & 0xFF) == 0x47) return true;
		// GIF: 47 49 46 38
		if ((magic[0] & 0xFF) == 0x47 && (magic[1] & 0xFF) == 0x49 && (magic[2] & 0xFF) == 0x46 && (magic[3] & 0xFF) == 0x38) return true;
		return false;
	}

	private String sanitize(String input, int maxLength) {
		if (input == null) return "";
		input = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		return input.length() > maxLength ? input.substring(0, maxLength) : input;
	}

}
