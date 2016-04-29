/*
 * Copyright (c) NASK, NCSC
 *
 * This file is part of HoneySpider Network 2.1.
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

import pl.nask.hsn2.ParameterException;
import pl.nask.hsn2.TaskContext;
import pl.nask.hsn2.service.scdbg.ScdbgTool;
import pl.nask.hsn2.task.Task;
import pl.nask.hsn2.task.TaskFactory;
import pl.nask.hsn2.wrappers.ObjectDataWrapper;
import pl.nask.hsn2.wrappers.ParametersWrapper;

public class ScTaskFactory implements TaskFactory {
	private static final int SCDBG_MEMORY_LIMIT = 128;
    private static ScdbgTool tool;

    public static void prepereForAllThreads(ScdbgTool tool) {
        ScTaskFactory.tool = tool;
    }

    public final Task newTask(TaskContext jobContext, ParametersWrapper parameters, ObjectDataWrapper data)
            throws ParameterException {
    	int memLimit = parameters.getInt("scdbg_memory_limit", SCDBG_MEMORY_LIMIT);
    	tool.setMemoryLimit(memLimit);
        return new ShellcodeTask(tool, jobContext, data);
    }
}