package org.webcurator.core.reader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.util.List;

/**
 * The Service for reading log and report files and providing them back to the client.
 */
@RestController
@RequestMapping("/curator/log-reader")
public class LogReaderController {
    /** The LogReader instance to delegate to. */
    @Autowired
    @Qualifier("logReader") // add a qualifier here because the bean name might change
    private LogReader logReader;
    
    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFiles(java.lang.String)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE)
    public List<String> listLogFiles(@PathVariable(value = "job") String job) {
        return logReader.listLogFiles(job);
    }    
    
    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#listLogFileAttributes(java.lang.String)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_PROPERTIES)
    public List<LogFilePropertiesDTO> listLogFileAttributes(@PathVariable(value = "job") String job) {
        return logReader.listLogFileAttributes(job);
    }    

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#countLines(java.lang.String, java.lang.String)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_LINE_COUNT)
    public Integer countLines(@PathVariable(value = "job") String job,
                              @RequestParam(value = "filename") String filename) {
        return logReader.countLines(job, filename);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#tail(java.lang.String, java.lang.String, int)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_TAIL)
    public List<String> tail(@PathVariable(value = "job") String job,
                             @RequestParam(value = "filename") String filename,
                             @RequestParam(value = "number-lines") int numberOfLines) {
        return logReader.tail(job, filename, numberOfLines);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#get(java.lang.String, java.lang.String, int, int)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_GET)
    public List<String> get(@PathVariable(value = "job") String job,
                            @RequestParam("filename") String aFileName,
                            @RequestParam(value = "start-line") int startLine,
                            @RequestParam(value = "number-of-lines") int noOfLines) {
        return logReader.get(job, aFileName, startLine, noOfLines);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getHopPath(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_HOP_PATH)
    public List<String> getHopPath(@PathVariable(value = "job") String job,
                               @RequestParam(value = "filename") String filename,
                               @RequestParam(value = "result-oid") String resultOid,
                               @RequestParam(value = "url") String url) {
        return logReader.getHopPath(job, resultOid, filename, url);
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineBeginning(String, String, String). 
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_BEGINNING)
    public Integer findFirstLineBeginning(@PathVariable(value = "job") String job,
                                          @RequestParam(value = "filename") String filename,
                                          @RequestParam(value = "match") String match) {
        return logReader.findFirstLineBeginning(job, filename, match);
    }

    /* (non-Javadoc)
     * @see LogReader#findFirstLineContaining(String, String, String). 
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_CONTAINING_MATCH)
    public Integer findFirstLineContaining(@PathVariable(value = "job") String job,
                                           @RequestParam(value = "filename") String filename,
                                           @RequestParam(value = "match") String match) {
        return logReader.findFirstLineContaining(job, filename, match);
    }
    
    /* (non-Javadoc)
     * @see LogReader#findFirstLineAfterTimeStamp(String, String, Long). 
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_FIRST_LINE_AFTER_TIMESTAMP)
    public Integer findFirstLineAfterTimeStamp(@PathVariable(value = "job")String job,
                                               @RequestParam(value = "filename") String filename,
                                               @RequestParam(value = "timestamp") Long timestamp) {
        return logReader.findFirstLineAfterTimeStamp(job, filename, timestamp);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.reader.LogReader#getByRegularExpression(String, String, String, String, boolean, int, int).
     */
    @GetMapping(path = LogReaderPaths.LOG_FILE_BY_REGULAR_EXPRESSION)
    public List<String> getByRegularExpression(@PathVariable(value = "job") String job,
                                               @RequestParam(value = "filename") String filename,
                                               @RequestParam(value = "regular-expression") String regularExpression,
                                               @RequestParam(value = "add-lines") String addLines,
                                               @RequestParam(value = "prepend-line-numbers") boolean prependLineNumbers,
                                               @RequestParam(value = "skip-first-matches") int skipFirstMatches,
                                               @RequestParam(value = "number-of-matches") int numberOfMatches) {
        return logReader.getByRegularExpression(job, filename, regularExpression, addLines, prependLineNumbers, skipFirstMatches, numberOfMatches);
    }

    @GetMapping(path = LogReaderPaths.LOG_FILE_RETRIEVE_LOG_FILE)
    public DataHandler retrieveLogfile(@PathVariable(value= "job") String job,
                                       @RequestParam(value = "filename") String filename) {
        try {
            return new DataHandler(new FileDataSource(logReader.retrieveLogfile(job, filename)));
        }
        catch(RuntimeException rex) {
            System.out.println("Error");
            throw rex;
        }            
    }
}
