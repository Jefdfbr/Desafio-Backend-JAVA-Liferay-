package com.seatecnologia.todo.model;

import java.io.Serializable;
import java.util.Date;

public class SubTask implements Serializable {
	private static final long serialVersionUID = 1L;
	private long subTaskId;
	private long taskId;
	private String title;
	private boolean completed;
	private Date createdAt;

	public SubTask() {}
	public long getSubTaskId() { return subTaskId; }
	public void setSubTaskId(long id) { this.subTaskId = id; }
	public long getTaskId() { return taskId; }
	public void setTaskId(long id) { this.taskId = id; }
	public String getTitle() { return title; }
	public void setTitle(String t) { this.title = t; }
	public boolean getCompleted() { return completed; }
	public void setCompleted(boolean c) { this.completed = c; }
	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date d) { this.createdAt = d; }
}
