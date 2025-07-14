package com.perforce.team.tests.swarmreview;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.tests.Utils;

public class LocalTestUtil {

	private static final String[] DOCKER_LOCATIONS = new String[] { System.getenv("DOCKER_LOCATION"),
			"/usr/local/bin/docker", "/usr/bin/docker" };
	static private Process p4d;
	static private File serverRoot;
	private Process process;
	private Thread dockerThread;
	private Object waitLock = new Object();

	private LocalTestUtil() {
	}

	public static LocalTestUtil newInstance() {
		return new LocalTestUtil();
	}

	public void login(IP4Connection conn, String user, String password) throws Exception {
		conn.getServer().setUserName(user == null ? conn.getParameters().getUser() : user);
		conn.getServer().login(password);
	}

	public String getServerRoot() {
		return serverRoot.getAbsolutePath();
	}

	protected File getTestDir() {
		File testsDir = new File("testRun/swarmTest");
		if (!testsDir.exists()) {
			testsDir.mkdirs();
		}
		return testsDir;
	}
	/**
	 * Initialises and starts docker containers for P4 Code Review and P4 Server
	 * @throws Exception
	 */
	public void initDocker(final boolean downloadImages) throws Exception{
		dockerThread= new Thread(new Runnable(){
			public void run(){
				try {
					dockerDown(downloadImages);
					dockerStart();
				} catch (Exception e) {
					e.printStackTrace();
					dockerDown(true);
					destroyDockerProcess();
				}finally{
					synchronized(waitLock){
						waitLock.notifyAll();
					}
				}
			}
		});
		dockerThread.start();
		waitForDocker();
	}
	/**
	 * Wait for docker containers to startup
	 */
	public void waitForDocker(){
		try {
			synchronized(waitLock){
				waitLock.wait();
			}
		} catch (InterruptedException e) {
			dockerDown(true);
			destroyDockerProcess();
		}
	}

	/**
	 * Returns the path to P4 Server executable
	 * @return
	 * @throws IOException
	 */
	public String getP4dPath(String p4dVersion) throws IOException {
		String path = "servers/" + p4dVersion + "/" + Platform.getOS().toLowerCase(Locale.ENGLISH) + "/"
				+ Platform.getOSArch().toLowerCase(Locale.ENGLISH) + "/p4d";
		path = Utils.getBundlePath(path);
		new P4JavaSysFileCommandsHelper().setExecutable(path, true, false);
		return path;
	}

	/**
	 * Destroys java Process that started Docker
	 * @return
	 */
	public int destroyDockerProcess() {
		int exitValue = -10;
		if (process != null) {
			if (isAlive(process)) {
				process.destroy();
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				exitValue = process.exitValue();
			}
		}
		return exitValue;
	}
	
	private boolean isAlive(Process process) {
	    try {
	        process.exitValue();
	        return false;
	    } catch (Exception e) {
	        return true;
	    }
	}
	


	/**
	 * Starts up Docker containers
	 */
	private void dockerStart() {

		try {
			Bundle bundle = Platform.getBundle("com.perforce.team.tests");
			String url;
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				url = FileLocator.toFileURL(bundle.getEntry("docker-start.bat")).getPath();
			}
			else {
				url = FileLocator.toFileURL(bundle.getEntry("docker-start.sh")).getPath();
			}
			
			ProcessBuilder builder = new ProcessBuilder(url);

			builder.redirectErrorStream(true);
			Process p = null;
			try {
				p = builder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

			InputStream is = p.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));

			String val = "";
			while ((val = r.readLine()) != null) {
				System.out.println(val);

				if (val.toLowerCase().endsWith("swarm ready")) {
					System.out.println("Done... " + val);
					break;
				}
			}
			process = p;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down Docker and deletes images
	 */
	public void dockerDown(boolean deleteImages) {
		try {
			String url;
			Bundle bundle = Platform.getBundle("com.perforce.team.tests");
			if (System.getProperty("os.name").toLowerCase().contains("win")) {
				url = FileLocator.toFileURL(bundle.getEntry("docker-down.bat")).getPath();
			}
			else {
				url = FileLocator.toFileURL(bundle.getEntry("docker-down.sh")).getPath();
			}
			
			ProcessBuilder builder = new ProcessBuilder(url, String.valueOf(deleteImages));

			builder.redirectErrorStream(true);
			Process p = null;
			try {
				p = builder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

			InputStream is = p.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));

			String val = "";
			System.out.println("Initialising docker containers..............");
			while ((val = r.readLine()) != null) {
				System.out.println(val);
				if (val.toLowerCase().endsWith("swarm ready")) {
					System.out.println("Done... " + val);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			
		}
	}

	IP4Connection getLocalServer(String root, ConnectionParameters params) throws Exception {

		ConnectionParameters parameters = new ConnectionParameters();
		if (params == null) {
			parameters.setClient("test");
			parameters.setUser("testuser");
			parameters.setPort("localhost:1668");
			// parameters.setPassword("");
			// parameters.setCharset("utf8");
		}

		File testsDir = getTestDir();
		serverRoot = new File(testsDir, "p4d" + root);
		serverRoot.mkdirs();
		serverRoot.deleteOnExit();
		ProcessBuilder builder = new ProcessBuilder(getP4dPath("2017.1"), "-r", serverRoot.getAbsolutePath(), "-p",
				parameters.getPort(), "-L", serverRoot.getAbsolutePath().replace("/", "_") + ".log", "-vserver=5");

		p4d = builder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p4d.getInputStream()));
		String line = reader.readLine();
		if (line == null) {
			throw new Exception("P4 Server not producing output");
		}
		if (!line.endsWith("starting...")) {
			String first = line;
			line = reader.readLine();
			if (line == null || !line.endsWith("starting...")) {
				throw new Exception("P4 Server not producing 'starting...' message: " + first + "\n" + line);
			}
		}
		IP4Connection conn = new P4Connection(parameters);
		return conn;
	}

	public ConnectionParameters getConnectionParameters(String user, String pw, String port, String charSet,
			boolean ignoreSSLValidation, String client) {
		ConnectionParameters params = new ConnectionParameters();
		params.setUser(user);
		params.setClient(client);
		params.setPort(port);
		params.setCharset(charSet);
		params.setPassword(pw);
		params.setIgnoreSSLValidation(ignoreSSLValidation);
		return params;
	}

	public IP4Connection getExistingServerConnection(ConnectionParameters parameters) throws Exception {
		IP4Connection conn = new P4Connection(parameters);
		return conn;
	}

	void stop() {
		try {
			p4d.destroy();
			serverRoot.deleteOnExit();
		} catch (Exception ex) {

		}
	}

}
