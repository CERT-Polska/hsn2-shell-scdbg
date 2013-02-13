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

package pl.nask.hsn2.service;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

import pl.nask.hsn2.GenericService;
import pl.nask.hsn2.service.scdbg.ScdbgLinuxBinaryWrapper;
import pl.nask.hsn2.service.scdbg.ScdbgTool;
import pl.nask.hsn2.service.scdbg.ScdbgWrapper;

public final class ScService implements Daemon {

    private ScCommandLineParams cmd;
	private volatile ScdbgTool scdbgTool;
	private Thread serviceWorker;

    public static void main(String[] args) throws DaemonInitException, Exception {
    	
    	ScService scs = new ScService();
    	scs.init(new JsvcArgsWrapper(args));
    	scs.start();
    	scs.serviceWorker.join();
    	scs.stop();
    }

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
			cmd = new ScCommandLineParams();
	        cmd.parseParams(context.getArguments());

	        ScdbgWrapper wrapper = new ScdbgLinuxBinaryWrapper(cmd.getScdbgPath(), cmd.getScdbgTimeout(),cmd.getMaxThreads());

	        scdbgTool = new ScdbgTool(wrapper, new File(System.getProperty("java.io.tmpdir")));
		
	}

	@Override
	public void start() throws Exception {
		final GenericService service = new GenericService(new ScTaskFactory(scdbgTool), cmd.getMaxThreads(), cmd.getRbtCommonExchangeName(), cmd.getRbtNotifyExchangeName());

        cmd.applyArguments(service);
        serviceWorker = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						System.exit(1);
						
					}
				});
				 try {
					service.run();
				} catch (InterruptedException e) {
					System.exit(0);
				}
				
			}
		},"ShellAnalyzer-service");
        serviceWorker.start();
       
		
	}

	@Override
	public void stop() throws Exception {
		serviceWorker.interrupt();
		serviceWorker.join();
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	
	private static class JsvcArgsWrapper implements DaemonContext{

		private String[] args;

		public JsvcArgsWrapper(String[] p) {
			this.args = p;
		}
		@Override
		public DaemonController getController() {
			return null;
		}

		@Override
		public String[] getArguments() {
			return args;
		}
		
	}
}
