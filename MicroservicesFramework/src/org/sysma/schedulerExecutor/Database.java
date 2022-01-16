package org.sysma.schedulerExecutor;

public class Database {
	/*private final String dbPath;

	public Database(String dbPath) {
		super();
		this.dbPath = dbPath;
		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Connection getDbConnection() throws SQLException {
		return (DriverManager.getConnection("jdbc:sqlite:"+dbPath));
	}
	
	public void doWrite(Communication comm, String query, String queryName, Consumer<PreparedStatement> fill) throws SQLException {
		String qEntry = "QRY-"+dbPath+"-WRITE-"+queryName;
		Entry entry = comm.entry;
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.CALL, qEntry, System.currentTimeMillis()));
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.WAIT, qEntry, System.currentTimeMillis()));
		Connection c = getDbConnection();
		var ps = c.prepareStatement(query);
		fill.accept(ps);
		ps.execute();
		ps.close();
		entry.log.get().add(new LogLine(comm.client, qEntry, EVENT.END, null, System.currentTimeMillis()));
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.RESUME, null, System.currentTimeMillis()));
	}
	
	public void doRead(Communication comm, String query, String queryName, Consumer<PreparedStatement> fill, Consumer<ResultSet> read) throws SQLException {
		String qEntry = "QRY-"+dbPath+"-READ-"+queryName;
		Entry entry = comm.entry;
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.CALL, qEntry, System.currentTimeMillis()));
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.WAIT, qEntry, System.currentTimeMillis()));
		Connection c = getDbConnection();
		var ps = c.prepareStatement(query);
		fill.accept(ps);
		var ans = ps.executeQuery();
		entry.log.get().add(new LogLine(comm.client, qEntry, EVENT.END, null, System.currentTimeMillis()));
		entry.log.get().add(new LogLine(comm.client, entry.entryName, EVENT.RESUME, null, System.currentTimeMillis()));
		read.accept(ans);
		ans.close();
		ps.close();
	}
	*/
}
