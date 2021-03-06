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

import pl.nask.hsn2.CommandLineParams;

public class ScCommandLineParams extends CommandLineParams {

    private static final OptionNameWrapper SCDBG_PATH = new OptionNameWrapper("scdbg", "scdbgPath");
    private static final OptionNameWrapper SCDBG_TIMEOUT = new OptionNameWrapper("timeout", "scdbgTimeout");

    @Override
    public final void initOptions() {
        super.initOptions();
        addOption(SCDBG_PATH, "path", "Full path to scdbg binary");
        addOption(SCDBG_TIMEOUT, "timeout", "Scdbg execution timeout");
    }

    @Override
    public final void initDefaults() {
        super.initDefaults();
        setDefaultValue(SCDBG_TIMEOUT, "60");
        setDefaultServiceNameAndQueueName("shell-scdbg");
    }

    public final String getScdbgPath() {
        return getOptionValue(SCDBG_PATH);
    }

    public final String getScdbgTimeout() {
        return getOptionValue(SCDBG_TIMEOUT);
    }
}
