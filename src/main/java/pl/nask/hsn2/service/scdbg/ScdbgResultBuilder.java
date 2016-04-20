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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.ResourceException;

public class ScdbgResultBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScdbgResultBuilder.class);

    /*
     * pattern for:
     * 3) offset=0xc797       steps=MAX    final_eip=40d819
     * 4) offset= 0xad89       steps=2313       final_eip= 40be41
     */
    private Pattern lineWithOffsetPattern = Pattern.compile(".*[0-9]+\\) offset= ?([a-z0-9]+) +steps=([0-9]+|MAX) +final_eip= ?([a-z0-9]+).*");

    // pattern for:
    // Shellcode detected at offset = 0x012c
    private Pattern lineWithDetectedShellcodePattern = Pattern.compile(".*Shellcode detected at offset = ?([a-z0-9]+)");

    private List<Offset> offsets = new ArrayList<Offset>();
    private List<ProcessedOffset> processedOffsets = new ArrayList<ProcessedOffset>();
    private ProcessedOffsetBuilder currentOffset;

    public final ScdbgToolResult build() {
        return new ScdbgToolResult(offsets, processedOffsets);
    }

    public final void scanOutputLine(String line) throws ResourceException {
        LOGGER.debug("Got output: {}", line);
        if (currentOffset == null) {
            tryToMatchOffsetPattern(line);
        } else {
            currentOffset.handleOutputLine(line);
        }
    }

    final boolean tryToMatchOffsetPattern(String line) {
        String found = match(lineWithOffsetPattern, line, 1);
        if (found == null) {
            found = match(lineWithDetectedShellcodePattern, line, 1);
        }

        if (found != null) {
            Offset offset = new Offset(found, null);
            offsets.add(offset);
            return true;
        } else {
            return false;
        }
    }

    private String match(Pattern pattern, String line, int group) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            return matcher.group(group);
        } else {
            return null;
        }
    }

    final boolean tryToMatchDetectedShellcodePattern(String line) {
        Matcher matcher = lineWithDetectedShellcodePattern.matcher(line);
        if (matcher.matches()) {
            String offset = matcher.group(1);
            offsets.add(new Offset(offset, null));
            return true;
        } else {
            return false;
        }
    }

    final boolean tryToMatchUrlPattern(String line) {
        return false;
    }

    public final void startOffsetProcessing(Offset offset, File graphFile, File localTempDir) {
        ProcessedOffsetBuilder builder = new ProcessedOffsetBuilder(offset, localTempDir);
        builder.setGraphFile(graphFile);
        currentOffset = builder;
    }

    public final void endOffsetProcessing() {
        ProcessedOffset offset = currentOffset.build();
        currentOffset = null;
        processedOffsets.add(offset);
    }
}
