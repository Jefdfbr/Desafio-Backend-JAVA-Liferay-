package com.seatecnologia.todo.service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.seatecnologia.todo.model.SubTask;
import com.seatecnologia.todo.model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskLocalService {
	private static final Log _log = LogFactoryUtil.getLog(TaskLocalService.class);
	private static final String URL = "jdbc:postgresql://alje-postgres-1:5432/desafio2";
	private static final String USER = "alje";
	private static final String PASS = "alje";

	static { try { Class.forName("org.postgresql.Driver"); } catch(Exception e) { _log.error(e); } }

	private static Connection c() throws Exception { return DriverManager.getConnection(URL, USER, PASS); }

	private static long nid(Connection cn, String t, String col) throws Exception {
		Statement s = null; ResultSet r = null;
		try { s = cn.createStatement(); r = s.executeQuery("SELECT COALESCE(MAX("+col+"),0)+1 FROM "+t); r.next(); return r.getLong(1); }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(s!=null) s.close(); } catch(Exception e){} }
	}

	public static Task addTask(long uid, String title, String desc, Date now) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try {
			cn = c(); long id = nid(cn, "Todo_Task", "taskId"); Timestamp ts = new Timestamp(now.getTime());
			ps = cn.prepareStatement("INSERT INTO Todo_Task(taskId,userId,title,description,completed,imageFileEntryId,createdAt,modifiedAt) VALUES("+id+",?,?,?,false,0,?,?)");
			ps.setLong(1,uid); ps.setString(2,title); ps.setString(3,desc!=null?desc:""); ps.setTimestamp(4,ts); ps.setTimestamp(5,ts);
			ps.executeUpdate();
			Task t = new Task(); t.setTaskId(id); t.setUserId(uid); t.setTitle(title); t.setDescription(desc); t.setCompleted(false); t.setCreatedAt(now); t.setModifiedAt(now);
			return t;
		} finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static Task getTask(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null; ResultSet r = null;
		try { cn = c(); ps = cn.prepareStatement("SELECT * FROM Todo_Task WHERE taskId=?"); ps.setLong(1,id); r = ps.executeQuery(); if(r.next()) return mt(r); return null; }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void updateTask(long id, String title, String desc) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); ps = cn.prepareStatement("UPDATE Todo_Task SET title=?,description=?,modifiedAt=? WHERE taskId=?"); ps.setString(1,title); ps.setString(2,desc!=null?desc:""); ps.setTimestamp(3,new Timestamp(new Date().getTime())); ps.setLong(4,id); ps.executeUpdate(); }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void deleteTask(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); ps = cn.prepareStatement("DELETE FROM Todo_SubTask WHERE taskId=?"); ps.setLong(1,id); ps.executeUpdate(); close(ps); ps = cn.prepareStatement("DELETE FROM Todo_Task WHERE taskId=?"); ps.setLong(1,id); ps.executeUpdate(); }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void toggleTaskCompleted(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); ps = cn.prepareStatement("UPDATE Todo_Task SET completed=NOT completed,modifiedAt=? WHERE taskId=?"); ps.setTimestamp(1,new Timestamp(new Date().getTime())); ps.setLong(2,id); ps.executeUpdate(); }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void setTaskImage(long id, long feid) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); ps = cn.prepareStatement("UPDATE Todo_Task SET imageFileEntryId=?,modifiedAt=? WHERE taskId=?"); ps.setLong(1,feid); ps.setTimestamp(2,new Timestamp(new Date().getTime())); ps.setLong(3,id); ps.executeUpdate(); }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static List<Task> getTasksByUserId(long uid) throws Exception {
		Connection cn = null; PreparedStatement ps = null; ResultSet r = null; List<Task> l = new ArrayList<>();
		try { cn = c(); ps = cn.prepareStatement("SELECT * FROM Todo_Task WHERE userId=? ORDER BY completed ASC, createdAt DESC"); ps.setLong(1,uid); r = ps.executeQuery(); while(r.next()) l.add(mt(r)); return l; }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static int getTasksCountByUserIdAndCompleted(long uid, boolean c) throws Exception {
		Connection cn = null; PreparedStatement ps = null; ResultSet r = null;
		try { cn = c(); ps = cn.prepareStatement("SELECT COUNT(*) FROM Todo_Task WHERE userId=? AND completed=?"); ps.setLong(1,uid); ps.setBoolean(2,c); r = ps.executeQuery(); r.next(); return r.getInt(1); }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static SubTask addSubTask(long tid, String title) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); long id = nid(cn, "Todo_SubTask", "subTaskId"); ps = cn.prepareStatement("INSERT INTO Todo_SubTask(subTaskId,taskId,title,completed,createdAt) VALUES("+id+",?,?,false,?)"); ps.setLong(1,tid); ps.setString(2,title); ps.setTimestamp(3,new Timestamp(new Date().getTime())); ps.executeUpdate(); SubTask s = new SubTask(); s.setSubTaskId(id); s.setTaskId(tid); s.setTitle(title); s.setCompleted(false); s.setCreatedAt(new Date()); return s; }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static SubTask getSubTask(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null; ResultSet r = null;
		try { cn = c(); ps = cn.prepareStatement("SELECT * FROM Todo_SubTask WHERE subTaskId=?"); ps.setLong(1,id); r = ps.executeQuery(); if(r.next()) return ms(r); return null; }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void deleteSubTask(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try { cn = c(); ps = cn.prepareStatement("DELETE FROM Todo_SubTask WHERE subTaskId=?"); ps.setLong(1,id); ps.executeUpdate(); }
		finally { try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static List<SubTask> getSubTasksByTaskId(long tid) throws Exception {
		Connection cn = null; PreparedStatement ps = null; ResultSet r = null; List<SubTask> l = new ArrayList<>();
		try { cn = c(); ps = cn.prepareStatement("SELECT * FROM Todo_SubTask WHERE taskId=? ORDER BY createdAt ASC"); ps.setLong(1,tid); r = ps.executeQuery(); while(r.next()) l.add(ms(r)); return l; }
		finally { try { if(r!=null) r.close(); } catch(Exception e){} try { if(ps!=null) ps.close(); } catch(Exception e){} try { if(cn!=null) cn.close(); } catch(Exception e){} }
	}

	public static void updateSubTask(long id, String title) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try {
			cn = c(); ps = cn.prepareStatement("UPDATE Todo_SubTask SET title=? WHERE subTaskId=?");
			ps.setString(1, title); ps.setLong(2, id); ps.executeUpdate();
		} finally { close(ps); closeConn(cn); }
	}

	public static void toggleSubTaskCompleted(long id) throws Exception {
		Connection cn = null; PreparedStatement ps = null;
		try {
			cn = c(); ps = cn.prepareStatement("UPDATE Todo_SubTask SET completed=NOT completed WHERE subTaskId=?");
			ps.setLong(1, id); ps.executeUpdate();
		} finally { close(ps); closeConn(cn); }
	}

	private static Task mt(ResultSet r) throws Exception { Task t = new Task(); t.setTaskId(r.getLong("taskId")); t.setUserId(r.getLong("userId")); t.setTitle(r.getString("title")); t.setDescription(r.getString("description")); t.setCompleted(r.getBoolean("completed")); t.setImageFileEntryId(r.getLong("imageFileEntryId")); t.setCreatedAt(r.getTimestamp("createdAt")); t.setModifiedAt(r.getTimestamp("modifiedAt")); return t; }
	private static SubTask ms(ResultSet r) throws Exception { SubTask s = new SubTask(); s.setSubTaskId(r.getLong("subTaskId")); s.setTaskId(r.getLong("taskId")); s.setTitle(r.getString("title")); s.setCompleted(r.getBoolean("completed")); s.setCreatedAt(r.getTimestamp("createdAt")); return s; }
	private static void close(PreparedStatement ps) { try { if(ps!=null) ps.close(); } catch(Exception e){} }
	private static void closeConn(Connection cn) { try { if(cn!=null) cn.close(); } catch(Exception e){} }
}
