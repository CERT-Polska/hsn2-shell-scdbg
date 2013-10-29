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

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

import pl.nask.hsn2.CommandLineParams;
import pl.nask.hsn2.ResourceException;
import pl.nask.hsn2.ServiceMain;
import pl.nask.hsn2.service.scdbg.ScdbgLinuxBinaryWrapper;
import pl.nask.hsn2.service.scdbg.ScdbgTool;
import pl.nask.hsn2.service.scdbg.ScdbgWrapper;
import pl.nask.hsn2.task.TaskFactory;

public final class ScService extends ServiceMain {

    public static void main(final String[] args) throws DaemonInitException, Exception {
    	ScService scs = new ScService();
    	scs.init(new DaemonContext() {
			public DaemonController getController() {
				return null;
			}
			public String[] getArguments() {
				return args;
			}
		});
    	scs.start();
    }

	@Override
	protected void prepareService() {
	}

	@Override
	protected Class<? extends TaskFactory> initializeTaskFactory() {
		try {
			ScCommandLineParams cmd = (ScCommandLineParams)getCommandLineParams();
			ScdbgWrapper wrapper = new ScdbgLinuxBinaryWrapper(cmd.getScdbgPath(), cmd.getScdbgTimeout(),cmd.getMaxThreads());
	        ScdbgTool scdbgTool = new ScdbgTool(wrapper, new File(System.getProperty("java.io.tmpdir")));
	        ScTaskFactory.prepereForAllThreads(scdbgTool);
	        return ScTaskFactory.class;
		}
		catch(ResourceException e){
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected CommandLineParams newCommandLineParams() {
		return new ScCommandLineParams();
	}
}
