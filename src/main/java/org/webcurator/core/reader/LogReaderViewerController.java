package org.webcurator.core.reader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * The Service for reading log and report files and providing them back to the client.
 */
@RestController
//@RequestMapping(produces = "application/json")
public class LogReaderViewerController {
    /** The LogReader instance to delegate to. */
    @Autowired
    @Qualifier("logReader") // add a qualifier here because the bean name might change
    private LogReader logReader;
    
    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFiles(java.lang.String)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<String> listLogFiles(@PathVariable(value = "job") String job) {
        return logReader.listLogFiles(job);
    }    
    
    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFileAttributes(java.lang.String)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_PROPERTIES, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<LogFilePropertiesDTO> listLogFileAttributes(@PathVariable(value = "job") String job) {
        List<LogFilePropertiesDTO> rst=logReader.listLogFileAttributes(job);
        return rst;
    }    

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#countLines(java.lang.String, java.lang.String)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_LINE_COUNT, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public Integer countLines(@PathVariable(value = "job") String job,
                              @RequestParam(value = "filename") String filename) {
        return logReader.countLines(job, filename);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#tail(java.lang.String, java.lang.String, int)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_TAIL, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<String> tail(@PathVariable(value = "job") String job,
                             @RequestParam(value = "filename") String filename,
                             @RequestParam(value = "number-of-lines") int numberOfLines) {
        return logReader.tail(job, filename, numberOfLines);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#get(java.lang.String, java.lang.String, int, int)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_JOB, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<String> get(@PathVariable(value = "job") String job,
                            @RequestParam("filename") String aFileName,
                            @RequestParam(value = "start-line") int startLine,
                            @RequestParam(value = "number-of-lines") int noOfLines) {
        return logReader.get(job, aFileName, startLine, noOfLines);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getHopPath(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_HOP_PATH, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<String> getHopPath(@PathVariable(value = "job") String job,
                               @RequestParam(value = "filename") String filename,
                               @RequestParam(value = "result-oid") String resultOid,
                               @RequestParam(value = "url") String url) {
        return logReader.getHopPath(job, resultOid, filename, url);
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineBeginning(String, String, String). 
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_BEGINNING, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public Integer findFirstLineBeginning(@PathVariable(value = "job") String job,
                                          @RequestParam(value = "filename") String filename,
                                          @RequestParam(value = "match") String match) {
        return logReader.findFirstLineBeginning(job, filename, match);
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineContaining(String, String, String). 
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_CONTAINING_MATCH, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public Integer findFirstLineContaining(@PathVariable(value = "job") String job,
                                           @RequestParam(value = "filename") String filename,
                                           @RequestParam(value = "match") String match) {
        return logReader.findFirstLineContaining(job, filename, match);
    }
    
    /* (non-Javadoc)
     * @see LogReader#findFirstLineAfterTimeStamp(String, String, Long). 
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_AFTER_TIMESTAMP, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public Integer findFirstLineAfterTimeStamp(@PathVariable(value = "job")String job,
                                               @RequestParam(value = "filename") String filename,
                                               @RequestParam(value = "timestamp") Long timestamp) {
        return logReader.findFirstLineAfterTimeStamp(job, filename, timestamp);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getByRegularExpression(String, String, String, String, boolean, int, int).
     */
    @RequestMapping(path = LogReaderPaths.LOG_FILE_BY_REGULAR_EXPRESSION, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public List<String> getByRegularExpression(@PathVariable(value = "job") String job,
                                               @RequestParam(value = "filename") String filename,
                                               @RequestParam(value = "regular-expression") String regularExpression,
                                               @RequestParam(value = "add-lines") String addLines,
                                               @RequestParam(value = "prepend-line-numbers") boolean prependLineNumbers,
                                               @RequestParam(value = "skip-first-matches") int skipFirstMatches,
                                               @RequestParam(value = "number-of-matches") int numberOfMatches) {
        return logReader.getByRegularExpression(job, filename, regularExpression, addLines, prependLineNumbers, skipFirstMatches, numberOfMatches);
    }


    @RequestMapping(path = LogReaderPaths.LOG_FILE_RETRIEVE_LOG_FILE, method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public void retrieveLogfile(@PathVariable(value= "job") String job,
                                                    @RequestParam(value = "filename") String filename,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        try {
            File file=logReader.retrieveLogfile(job, filename);
            StreamUtils.copy(new FileInputStream(file), response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
