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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.NewUrlObject;
import pl.nask.hsn2.ParameterException;
import pl.nask.hsn2.ResourceException;
import pl.nask.hsn2.StorageException;
import pl.nask.hsn2.TaskContext;
import pl.nask.hsn2.protobuff.Resources.ScdbgResult;
import pl.nask.hsn2.protobuff.Resources.ScdbgResultList;
import pl.nask.hsn2.service.fileutils.FileHelper;
import pl.nask.hsn2.service.scdbg.ProcessedOffset;
import pl.nask.hsn2.service.scdbg.ScdbgTool;
import pl.nask.hsn2.service.scdbg.ScdbgToolResult;
import pl.nask.hsn2.task.Task;
import pl.nask.hsn2.wrappers.ObjectDataWrapper;

public class ShellcodeTask implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellcodeTask.class);

    private final ScdbgTool tool;
    private Long fileId;
    private final TaskContext jobContext;

    public ShellcodeTask(ScdbgTool tool, TaskContext jobContext, ObjectDataWrapper data) {
        this.tool = tool;
        this.jobContext = jobContext;
        fileId = data.getReferenceId("content");
    }

    public final boolean takesMuchTime() {
        return fileId != null;
    }

    public final void process() throws ParameterException, ResourceException, StorageException {
        if (fileId == null) {
            LOGGER.info("Task skipped");
        } else {
            File tmpDir = null;
            try {
            	tmpDir = FileHelper.createTempDir();
            	File file = downloadFile(tmpDir);
            	ScdbgToolResult res = tool.runWithFile(file.getAbsolutePath());
            	jobContext.addAttribute("scdbg_offsets", res.getNumberOfShellcodes());
            	if ( res.getNumberOfShellcodes() > 0) {

            		long resultsReferenceId = saveResultsInDataStore(res);
            		jobContext.addReference("scdbg_results", resultsReferenceId);

            		addNewObjects(res);
            	} else {
            		LOGGER.debug("no shellcode detected for jobID:{}, reqID:{}",jobContext.getJobId(),jobContext.getReqId() );
            	}
            } catch (IOException e) {
            	throw new ResourceException("Error running scdbg", e);
            } finally {
            	deleteTaskTempDir(tmpDir);
            }
        }
    }

    private void deleteTaskTempDir(File dir) {
        if (dir != null) {
            try {
            	LOGGER.debug("Deleting tmp dir: {}",dir.getAbsolutePath());
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
               LOGGER.warn("Could not delete a task temp directory", e);
            }
        }

    }

    private void addNewObjects(ScdbgToolResult res) throws StorageException {
        for (String url: res.getOutgoingUrls()) {
            try {
                NewUrlObject newObject = new NewUrlObject(url, "shell-scdbg", "url");
                jobContext.newObject(newObject);
            } catch (URIException e) {
                LOGGER.warn("Not an URL! {} ", url);
            }
        }
    }

    private long saveResultsInDataStore(ScdbgToolResult res) throws StorageException, ResourceException {
    	ScdbgResultList.Builder scdbgResultListMessage = ScdbgResultList.newBuilder();

    	for (ProcessedOffset offset: res.getProcessedOffsets()) {
    		try {
    			Long dumpFileId = null;
    			if (offset.hasMemoryDump()) {
    				dumpFileId = jobContext.saveInDataStore(new FileInputStream(offset.getDumpFile()));
    			}

    			Long graphFileId = null;
    			if (offset.hasGraphFile()) {
    				graphFileId = jobContext.saveInDataStore(new FileInputStream(offset.getGraphFile()));
    			}

    			ScdbgResult scdbgResultMessage = makeScdbgMessage(offset.getOffsetAsInt(), offset.getOutput(), dumpFileId, graphFileId);
    			scdbgResultListMessage.addResults(scdbgResultMessage);
    			File f = offset.getGraphFile();
    			if ( f != null && f.exists()) {
    				File pd = f.getParentFile();
    				FileUtils.deleteDirectory(pd);
    				LOGGER.debug("TMP dir deleted:{}.",pd.getAbsolutePath());
    			}
    		} catch (FileNotFoundException e) {
    			throw new ResourceException("File not found", e);
    		} catch (IOException e) {
    			LOGGER.warn("Couldn't delete tmp dir.",e);
    		}
    	}

    	return jobContext.saveInDataStore(scdbgResultListMessage.build().toByteArray());
    }

    private ScdbgResult makeScdbgMessage(int offsetAsInt, String output, Long dumpFileId, Long graphFileId) {
        ScdbgResult.Builder b = ScdbgResult.newBuilder();
        b.setOffset(offsetAsInt);
        b.setOutput(output);
        if (dumpFileId != null)
            b.setDump(jobContext.asReference(dumpFileId));
        if (graphFileId != null)
            b.setGraph(jobContext.asReference(graphFileId));

        return b.build();
    }


    private File downloadFile(File targetDir) throws ResourceException, StorageException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            long downloadTimeStart = System.currentTimeMillis();
            File tmpFile = File.createTempFile(jobContext.getJobId() + "-" + jobContext.getReqId() + "-" + fileId + "-", "", targetDir);
            is = jobContext.getFileAsInputStream(fileId);
            fos = new FileOutputStream(tmpFile);
            IOUtils.copy(is, fos);
            LOGGER.debug("Downloaded {} ({}) in {} ms",new Object[]{tmpFile.getName(), FileUtils.byteCountToDisplaySize(tmpFile.length()), System.currentTimeMillis() - downloadTimeStart});
            return tmpFile;
        } catch (IOException e) {
            throw new ResourceException("Cannot create temporary file", e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(fos);
        }
    }
}
