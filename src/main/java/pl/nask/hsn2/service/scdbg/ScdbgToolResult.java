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

import java.util.ArrayList;
import java.util.List;

public class ScdbgToolResult {

    private List<Offset> offsets;

    private List<ProcessedOffset> processedOffsets;

    private List<String> outgoingUrls = new ArrayList<String>();

    ScdbgToolResult(List<Offset> offsets) {
        this.offsets = offsets;
    }

    ScdbgToolResult(List<Offset> offsets, List<ProcessedOffset> processedOffsets) {
        this.offsets = offsets;
        this.processedOffsets = processedOffsets;

        for (ProcessedOffset off: processedOffsets) {
            outgoingUrls.addAll(off.getOutgoingUrls());
        }
    }

    public int getNumberOfShellcodes() {
        return offsets.size();
    }


    public List<String> getOutgoingUrls() {
        return outgoingUrls;
    }

    List<Offset> getOffsets() {
        return offsets;
    }

    public List<ProcessedOffset> getProcessedOffsets() {
        return processedOffsets;
    }
}
