package org.sysma.schedulerExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class Queries {
	public static class ReadQuery{
		private final String database;
		private final String queryText;
		private final String queryName;
		
		private ReadQuery(String database, String queryName, String queryText) {
			this.database = database;
			this.queryName = queryName;
			this.queryText = queryText;
		}
		
		void doRead(Communication comm, Consumer<PreparedStatement> fill, Consumer<ResultSet> read) throws SQLException {
			Entry entry = comm.entry;
			entry.log.get().add(new LogLine.QueryCall(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));
			
			PreparedStatement ps;
			Connection c;
			ResultSet ans;
			while(true) {
				try {
					c = getDbConnection(database, comm.threadName);
					ps = c.prepareStatement(queryText);
					fill.accept(ps);
					ans = ps.executeQuery();
					break;
				} catch (SQLException e) {
					if(!e.getMessage().startsWith("[SQLITE_BUSY]"))
						throw e;
				}
			}
			
			read.accept(ans);
			ans.close();
			ps.close();
			entry.log.get().add(new LogLine.QueryResume(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));
		}
	}
	
	public static class WriteQuery{
		private final String database;
		private final String queryText;
		private final String queryName;
		
		private WriteQuery(String database, String queryName, String queryText) {
			this.database = database;
			this.queryName = queryName;
			this.queryText = queryText;
		}
		
		void doWrite(Communication comm, Consumer<PreparedStatement> fill) throws SQLException {
			Entry entry = comm.entry;
			entry.log.get().add(new LogLine.QueryCall(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));

			PreparedStatement ps;
			Connection c;
			while(true) {
				try {
					c = getDbConnection(database, comm.threadName);
					ps = c.prepareStatement(queryText);
					fill.accept(ps);
					ps.execute();
					break;
				} catch (SQLException e) {
					if(!e.getMessage().startsWith("[SQLITE_BUSY]"))
						throw e;
				}
			}

			entry.log.get().add(new LogLine.QueryResume(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void doWrite(Communication comm, Consumer<PreparedStatement> fill, Consumer<ResultSet> read) throws SQLException {
			Entry entry = comm.entry;
			entry.log.get().add(new LogLine.QueryCall(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));

			PreparedStatement ps;
			Connection c;
			while(true) {
				try {
					c = getDbConnection(database, comm.threadName);
					ps = c.prepareStatement(queryText);
					fill.accept(ps);
					ps.execute();
					break;
				} catch (SQLException e) {
					if(!e.getMessage().startsWith("[SQLITE_BUSY]"))
						throw e;
				}
			}

			entry.log.get().add(new LogLine.QueryResume(comm.entry.taskName, comm.entry.entryName, database, queryName, comm.client, System.currentTimeMillis()));
			var rs = ps.getResultSet();
			read.accept(rs);
			rs.close();
			try {
				ps.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static HashMap<String, HashMap<String, ReadQuery>> queriesRead = new HashMap<>();
	private static HashMap<String, HashMap<String, WriteQuery>> queriesWrite = new HashMap<>();
	private static HashMap<String, ArrayList<String>> allQueries = new HashMap<>();
	
	public static ReadQuery registerRead(String database, String name, String queryText) {
		allQueries.computeIfAbsent(database, (x)-> new ArrayList<>()).add(name);
		return queriesRead.computeIfAbsent(database, (x)-> new HashMap<>())
			.computeIfAbsent(name, (x)->new ReadQuery(database, name, queryText));
	}

	public static WriteQuery registerWrite(String database, String name, String queryText) {
		allQueries.computeIfAbsent(database, (x)-> new ArrayList<>()).add(name);
		return queriesWrite.computeIfAbsent(database, (x)-> new HashMap<>())
			.computeIfAbsent(name, (x)->new WriteQuery(database, name, queryText));
	}
	
	private static final ConcurrentHashMap<String, HashMap<String, Connection>> connCache = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, MongoClient> mongoConnCache = new ConcurrentHashMap<>();

	private static Connection getDbConnection(String dbPath, String threadName) throws SQLException {
		return connCache.computeIfAbsent(threadName, z->new HashMap<>())
			.computeIfAbsent(dbPath, z->{
				try {
					return DriverManager.getConnection("jdbc:sqlite:"+dbPath);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			});
	}

	static MongoClient getMongoDbConnection(String threadName) {
		return mongoConnCache.computeIfAbsent(threadName, z->{
			return MongoClients.create();
		});
	}
	
	public static HashMap<String, ArrayList<String>> getAll() {
		return allQueries;
	}
}
