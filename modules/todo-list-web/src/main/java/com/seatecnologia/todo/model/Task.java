package com.seatecnologia.todo.model;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
	private static final long serialVersionUID = 1L;
	private long taskId;
	private long userId;
	private String title;
	private String description;
	private boolean completed;
	private long imageFileEntryId;
	private Date createdAt;
	private Date modifiedAt;

	public Task() {}
	public long getTaskId() { return taskId; }
	public void setTaskId(long taskId) { this.taskId = taskId; }
	public long getUserId() { return userId; }
	public void setUserId(long userId) { this.userId = userId; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public boolean getCompleted() { return completed; }
	public void setCompleted(boolean completed) { this.completed = completed; }
	public long getImageFileEntryId() { return imageFileEntryId; }
	public void setImageFileEntryId(long id) { this.imageFileEntryId = id; }
	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date d) { this.createdAt = d; }
	public Date getModifiedAt() { return modifiedAt; }
	public void setModifiedAt(Date d) { this.modifiedAt = d; }
}
