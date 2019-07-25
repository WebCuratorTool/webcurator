/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;

/**
 * The LogReader specifies how to list and retrieve log and report files a
 * WCT component.
 * @author nwaight
 */
public interface LogReader {
	
	/**
     * List of names of all the log files currently available for the specified job.
     * @param job the job to return the list of files for
     * @return the list of Log file names
     */
    List<String> listLogFiles(String job);
        
	/**
     * List all the the log files currently available for the specified job.
     * @param job the job to return the list of files for
     * @return the array of 'log file objects'
     */
    List<LogFilePropertiesDTO> listLogFileAttributes(String job);

	/**
     * Count the number of lines in the specified log file.
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @return the count of lines
     */
	Integer countLines(String job, String filename);
    
    /**
     * Return a String containing the specified number of lines from the end
     * of the specified log file for the specified job.
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @param numberOfLines the number of lines to return
     * @return the lines from the jobs log file
     */
    List<String> tail(String job, String filename, int numberOfLines);

    /**
     * Return a String containing the specified number of lines from the beginning
     * of the specified log file for the specified job.
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @param startLine the the line to start from
     * @param numberOfLines the number of lines to return
     * @return the lines from the jobs log file
     */
    List<String> get(String job, String filename, int startLine, int numberOfLines);
        
    /**
     * Return a String array containing the hop path to the given Url 
     * extracted from the sorted crawl.log file for the specified job.
     * @param job the job to return the log data for
     * @param resultOid the harvest result oid of the crawl
     * @param filename the name of the sorted crawl.log file
     * @param url the Url to derive the hop path for
     * @return the lines representing the hop path
     */
    List<String> getHopPath(String job, String resultOid, String filename, String url);

    /**
     * Return an Integer containing the first line beginning with match
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @param match the regex to match
     * @return the line index
     */
	Integer findFirstLineBeginning(String job, String filename, String match);
    
    /**
     * Return an Integer containing the first line containing match
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @param match the regex to match
     * @return the line index
     */
	Integer findFirstLineContaining(String job, String filename, String match);
    
    /**
     * Return an Integer containing the first line beginning with timestamp
     * @param job the job to return the log data for
     * @param filename the name of the file
     * @param timestamp the timestamp to match
     * @return the line index
     */
	Integer findFirstLineAfterTimeStamp(String job, String filename, Long timestamp);
    
    /**
     * Returns all lines in a jobs log/file matching a given regular expression.  
     * Possible to get lines immediately following the matched line.  Also 
     * possible to have each line prepended by it's line number.
     *
     * @param job the job to return the log data for
     * @param filename The filename of the log/file
     * @param regularExpression The regular expression that is to be used
     * @param addLines Any lines following a match that <b>begin</b> with this 
     *                 string will also be included. We will stop including new 
     *                 lines once we hit the first that does not match.
     * @param prependLineNumbers If true, then each line will be prepended by 
     *                           it's line number in the file.
     * @param skipFirstMatches The first number of matches up to this value will
     *                         be skipped over.
     * @param numberOfMatches Once past matches that are to be skipped this many
     *                        matches will be added to the return value. A
     *                        value of 0 will cause all matching lines to be
     *                        included.
     * @return An array of two strings is returned. At index 0 tall lines in a
     *         log/file matching a given regular expression is located.
     *         At index 1 there is an informational string about how large a
     *         segment of the file is being returned.
     *         Null is returned if errors occur (file not found or io exception)
     *         If a PatternSyntaxException occurs, it's error message will be
     *         returned and the informational string will be empty (not null).
     */
    List<String> getByRegularExpression(String job, String filename,
                                        String regularExpression,
                                        String addLines,
                                        boolean prependLineNumbers,
                                        int skipFirstMatches,
                                        int numberOfMatches);
    
    /**
     * Retrieve a file for download.
     * @param job The name of the job to get the log file for.
     * @param filename The name of the log file.
     * @return
     */
    File retrieveLogfile(String job, String filename);

}
