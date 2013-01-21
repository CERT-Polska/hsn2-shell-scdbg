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

import pl.nask.hsn2.GenericService;
import pl.nask.hsn2.service.scdbg.ScdbgLinuxBinaryWrapper;
import pl.nask.hsn2.service.scdbg.ScdbgTool;
import pl.nask.hsn2.service.scdbg.ScdbgWrapper;

public final class ScService {

    private ScService() {}

    public static void main(String[] args) throws InterruptedException {
        ScCommandLineParams cmd = new ScCommandLineParams();
        
        cmd.parseParams(args);

        ScdbgWrapper wrapper = new ScdbgLinuxBinaryWrapper(cmd.getScdbgPath(), cmd.getScdbgTimeout(),cmd.getMaxThreads());

        ScdbgTool tool = new ScdbgTool(wrapper, new File(System.getProperty("java.io.tmpdir")));

        GenericService service = new GenericService(new ScTaskFactory(tool), cmd.getMaxThreads(), cmd.getRbtCommonExchangeName());

        cmd.applyArguments(service);
        service.run();
    }
}
