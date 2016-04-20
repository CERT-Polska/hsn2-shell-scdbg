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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.ResourceException;
import pl.nask.hsn2.service.fileutils.FileHelper;

public class ScdbgTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScdbgTool.class);

    private final File tmpDir;
    private final ScdbgWrapper scdbg;

    public ScdbgTool(ScdbgWrapper scdbg, File tmpDir) {
        this.scdbg = scdbg;
        this.tmpDir = tmpDir;
    }

    public final ScdbgToolResult runWithFile(String filename) throws IOException, ResourceException {
        LOGGER.info("Scanning file {}", filename);
        ScdbgResultBuilder builder = new ScdbgResultBuilder();
        scanForOffsets(builder, filename);
        ScdbgToolResult partialResult = builder.build();
        List<Offset> offsets = partialResult.getOffsets();
        for (Offset offset: offsets) {
            scanOffset(builder, filename, offset);
        }

        return builder.build();
    }


    final void scanOffset(ScdbgResultBuilder builder, String filename, Offset offset) throws ResourceException {
        LOGGER.debug("Scanning file {} with offset {}", filename, offset.getOffset());
        File localTemp;
        try {
            localTemp = FileHelper.createTempDir(tmpDir);
        } catch (IOException e) {
            throw new ResourceException("Error creating tmp graphFile", e);
        }

        File graphFile;
        try {
            graphFile = File.createTempFile("scdbg-", "-graphFile", localTemp);
            LOGGER.debug("Created tmp graph file:{}",graphFile.getAbsolutePath());
        } catch (IOException e) {
            throw new ResourceException("Error creating tmp graphFile", e);
        }

        builder.startOffsetProcessing(offset, graphFile, localTemp);
        InputStream input = scdbg.executeWithOffset(filename, graphFile, offset.getOffset(), localTemp);
        processInput(builder, input);
        builder.endOffsetProcessing();
    }

    private void processInput(ScdbgResultBuilder builder, InputStream input) throws ResourceException {
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(input));
            String line = null;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug("Got line: {}", line);
                builder.scanOutputLine(line);
            }
        } catch (IOException e) {
            throw new ResourceException("Error reading output from scdbg", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    final void scanForOffsets(ScdbgResultBuilder builder, String filename) throws ResourceException  {
        LOGGER.debug("Scanning file {} for offsets",filename);
        InputStream input = scdbg.executeForOffsets(filename, tmpDir);
        processInput(builder, input);
    }

	public final void setMemoryLimit(int memoryLimitInMb) {
		scdbg.setMemoryLimit(memoryLimitInMb);
	}
}
