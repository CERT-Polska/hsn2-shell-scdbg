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

import java.io.File;
import java.util.List;

public class ProcessedOffset {

    private File dumpFile;
    private File graphFile;

    private final String output;

    private final List<String> urls;

    private final Offset offset;

    public ProcessedOffset(Offset offset, String output, File graphFile, File dumpFile, List<String> outgoingUrls) {
        this.offset = offset;
        this.output = output;
        this.graphFile = graphFile;
        this.dumpFile = dumpFile;
        urls = outgoingUrls;
    }

    public boolean hasMemoryDump() {
        return dumpFile != null && dumpFile.exists();
    }

    public String getOutput() {
        return output;
    }

    public List<String> getOutgoingUrls() {
        return urls;
    }

    public boolean hasGraphFile() {
        return graphFile != null && graphFile.exists();
    }

    public int getOffsetAsInt() {
        return Integer.parseInt(offset.getOffset().substring(2), 16);
    }

    public File getDumpFile() {
        return dumpFile;
    }

    public File getGraphFile() {
        return graphFile;
    }
}
