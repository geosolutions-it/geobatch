package it.geosolutions.tools.io.file;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchMove {
	
	private final Logger LOGGER = LoggerFactory.getLogger(AsynchMove.class);
	
	private final Map<String, Object> map;
	private final FutureTask<Boolean> task;
	private final File source;
	private final File dest;
	
	/**
	 * @return the source
	 */
	public File getSource() {
		return source;
	}

	/**
	 * @return the dest
	 */
	public File getDest() {
		return dest;
	}
	
	/**
	 * @return the map
	 */
	public Map<String, Object> getMap() {
		return map;
	}
	
	public Boolean isMoved() throws InterruptedException, ExecutionException{
		return task.get();
	}
	
	public FutureTask<Boolean> getMoveTask(){
		return task;
	}

	public AsynchMove(final Map<String, Object> map, final File source, final File dest,final ExecutorService es) throws Exception {

		
		if (map==null || source==null || dest==null || es==null)
			throw new Exception("Some of passed argument are null!");
		
		if (es.isShutdown() || es.isTerminated())
			throw new Exception("Executor service is not started!");
		
		this.map=map;
		this.source=source;
		this.dest=dest;
		
		
		task=new FutureTask<Boolean>(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				try {
					FileUtils.moveFileToDirectory(source, dest, true);
					return Boolean.TRUE;
				} catch (Exception e) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error(e.getLocalizedMessage());
					return Boolean.FALSE;
				}
			}
		});
		
		es.execute(task);
		
	}
}
