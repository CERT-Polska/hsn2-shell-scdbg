/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.nask.hsn2.service.scdbg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.ResourceException;

class TimedScdbgProcess implements Callable<Integer> {

    private byte[] bytes;
    private PrintWriter writer;
    private String[] commandLine;
    private File localTmp;
    private int memoryLimitInKb;

	public TimedScdbgProcess(String commandLine, File localTmp,
			int memoryLimitInMb) {
		this.localTmp = localTmp;
		memoryLimitInKb = 1024 * memoryLimitInMb;
		this.commandLine = new String[] { "bash", "-c",
				"ulimit -v " + memoryLimitInKb + ";" + commandLine };
	}

    @Override
    public Integer call() throws Exception {
    	for (String s:commandLine){
    		System.out.print(s + " ");
    	}
    	System.out.println();
    	
    	
    	Process p = Runtime.getRuntime().exec(commandLine, null, localTmp);

        try {
            writer = new PrintWriter(p.getOutputStream());
            // have to emit ENTER to make scdbg end processing.
            writer.println();
            writer.flush();

            // read as much as possible and buffer since if the process produces too much data it will result with a deadlock.
            bytes = IOUtils.toByteArray(p.getInputStream());
        } catch (IOException e) {
            throw new ResourceException("Error executing scdbg", e);
        } finally {
            IOUtils.closeQuietly(writer);
            if (p != null) {
                p.destroy();
            }
        }
        return p.waitFor();
    }

    public byte[] getBytes() {
        return bytes;
    }
}

public class ScdbgLinuxBinaryWrapper implements ScdbgWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScdbgLinuxBinaryWrapper.class);
    private static final int NO_SHELLCODE_DETECTED_EXIT_VALUE = 255;
    private static final int SUCCESS_EXIT_VALUE = 0;
    private static final int DEFAULT_EXECUTION_TIMEOUT = 60;
    private final String scdbgPath;
    private final String scdbgTimeout;
    private static ExecutorService THREAD_POOL;
    private int memoryLimitInMb;

    public ScdbgLinuxBinaryWrapper(String scdbgPath, String scdbgTimeout,int noThreads) throws ResourceException {
    	THREAD_POOL = Executors.newFixedThreadPool(noThreads > 0 ? noThreads:1 );
        File f = new File(scdbgPath);
        if (!f.exists()) {
            throw new IllegalArgumentException("Path to scdbg binary does not exist: " + scdbgPath);
        }
        this.scdbgPath = scdbgPath;
        this.scdbgTimeout = scdbgTimeout;
        checkSCDBGTool();
    }

    public ScdbgLinuxBinaryWrapper(String scdbgPath, String scdbgTimeout) throws ResourceException {
    	this(scdbgPath, scdbgTimeout, 1);
    }

    /**
     * Checks whether SCDBG can be started without errors (e.g. missing
     * dependency)
     *
     * @throws ResourceException
     * @throws IllegalArgumentException if problems occur in running SCDBG tool
     */
    final void checkSCDBGTool() throws ResourceException {
        try {
            LOGGER.debug("Testing '" + scdbgPath + "'");
            Process p = Runtime.getRuntime().exec(scdbgPath + " \n");
            byte[] b;  //this must consume output of running process
            if (p.waitFor() != SUCCESS_EXIT_VALUE) {
                b = IOUtils.toByteArray(p.getErrorStream());
                LOGGER.error(new String(b));
                throw new IllegalArgumentException("Cannot start " + scdbgPath);
            } else {
                b = IOUtils.toByteArray(p.getInputStream());
                LOGGER.debug(new String(b));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new ResourceException("Error checking scdbg tool.",e);
        } catch (InterruptedException e) {
            LOGGER.error("Check interrupted.", e);
        }
    }

    @Override
    public InputStream executeForOffsets(String filename, File localTemp) throws ResourceException {
        return execute(getCommandLineForScan(filename), localTemp);
    }

    @Override
    public InputStream executeWithOffset(String filename, File graphFile, String offset, File localTemp) throws ResourceException {
        return execute(getCommandLineForAnalyse(filename, graphFile.getAbsolutePath(), offset), localTemp);
    }

    private static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<T> task = new FutureTask<T>(c);
        THREAD_POOL.execute(task);
        return task.get(timeout, timeUnit);
    }

    private InputStream execute(String commandLine, File localTmp) throws ResourceException {
        LOGGER.debug("Executing {}", (Object) commandLine);
        try {
            long timeout = 0;
            
            try {
                timeout = (scdbgTimeout != null && scdbgTimeout.length() > 0) ? Integer.parseInt(scdbgTimeout) : DEFAULT_EXECUTION_TIMEOUT;
            } catch (NumberFormatException e) {
                timeout = DEFAULT_EXECUTION_TIMEOUT;
            }
            TimedScdbgProcess scdbgTask = new TimedScdbgProcess(commandLine, localTmp, memoryLimitInMb);
            int retVal = timedCall(scdbgTask, timeout, TimeUnit.SECONDS);
            if (retVal != NO_SHELLCODE_DETECTED_EXIT_VALUE && retVal != SUCCESS_EXIT_VALUE) {
            	LOGGER.warn("Unexpected scdbg shell exit val = {}, for command: {}",retVal,commandLine);
                throw new ResourceException("Error executing scdbg, abnormal exit code:["+retVal+"]");
            }
            return new ByteArrayInputStream(scdbgTask.getBytes());
        } catch (InterruptedException e) {
            throw new ResourceException("Error executing scdbg.interrupted.", e);
        } catch (ExecutionException e) {
            throw new ResourceException("Error executing scdbg", e);
        } catch (TimeoutException e) {
            throw new ResourceException("Error executing scdbg.timeout.", e);
        }
    }

	private String getCommandLineForScan(String filename) {
		return scdbgPath + " /a /getpc /f " + filename.replaceAll("/", "//");
	}

	private String getCommandLineForAnalyse(String filename,
 String graphFile,
			String offset) {
		return scdbgPath + " /nc /G " + graphFile.replaceAll("/", "//")
				+ " /mm /mdll /a /d /hex /foff " + offset + " /f "
				+ filename.replaceAll("/", "//");
	}

	@Override
	public void setMemoryLimit(int memoryLimitInMb) {
		this.memoryLimitInMb = memoryLimitInMb;
	}
}
