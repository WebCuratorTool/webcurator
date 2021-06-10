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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.webcurator.domain.model.core.LogFilePropertiesDTO;

/**
 * An implementation of a LogReader that uses the specified LogProvider to access
 * the log and report files.
 *
 * @author nwaight
 */
public class LogReaderImpl implements LogReader {
    /**
     * the log provider to delegate to.
     */
    private LogProvider logProvider;

    /**
     * @see LogReader#listLogFiles(String).
     */
    public List<String> listLogFiles(String job) {
        return logProvider.getLogFileNames(job);
    }

    /**
     * @see LogReader#listLogFileAttributes(String).
     */
    public List<LogFilePropertiesDTO> listLogFileAttributes(String job) {
        return logProvider.getLogFileAttributes(job);
    }

    /**
     * @see LogReader#countLines(String, String).
     */
    public Integer countLines(String job, String filename) {
        Integer count = 0;

        File logFile = logProvider.getLogFile(job, filename);
        if (logFile != null) {
            BufferedReader bf = null;
            try {
                bf = new BufferedReader(new FileReader(logFile), 8192);

                while (bf.readLine() != null) {
                    count++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (bf != null) {
                    try {
                        bf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return count;
    }

    /**
     * @see LogReader#tail(String, String, int).
     */
    public List<String> tail(String job, String filename, int numberOfLines) {
        List<String> theTail;

        File logFile = logProvider.getLogFile(job, filename);
        if (logFile != null) {
            theTail = LogReaderUtils.tail(logFile.toString(), numberOfLines);
        } else {
            theTail = Arrays.asList("");
        }

        return theTail;
    }

    /**
     * @see LogReader#get(String, String, int, int).
     */
    public List<String> get(String job, String filename, int startLine, int numberOfLines) {
        List<String> theLines;

        File logFile = logProvider.getLogFile(job, filename);
        if (logFile != null) {
            theLines = LogReaderUtils.get(logFile.toString(), startLine, numberOfLines);
        } else {
            theLines = Arrays.asList("");
        }

        return theLines;
    }

    /**
     * @see LogReader#getHopPath(String, String, String, String).
     */
    public List<String> getHopPath(String job, String resultOid, String filename, String url) {

        File logFile = logProvider.getLogFile(job, filename);
        List<String> hopPaths = new ArrayList<>();

        searchForUrl(logFile, resultOid, url, hopPaths);

        Collections.reverse(hopPaths);

        return hopPaths;
    }

    private void searchForUrl(File theFile, String resultOid, String theUrl, List<String> resultsList) {

        String inLine = null;
        String referrer = null;
        boolean foundLast = false;
        boolean foundUrl = false;


        BufferedReader inputStream = null;

        try {
            inputStream = new BufferedReader(new FileReader(theFile.getAbsolutePath()));
            while ((inLine = inputStream.readLine()) != null) {
                String[] columns = inLine.split(" ");
                String dateTime = columns[0];
                String url = columns[3];
                if (url.equalsIgnoreCase(theUrl)) {
                    foundUrl = true;
                    String paths = columns[4];
                    String lastPathChar = paths.substring(paths.length() - 1);
                    String liveSite = "<a href='" + url + "' target='_blank'><b><u>Live Site</u></b></a>";
                    String browseTool = "<a href='curator/tools/browse/" + resultOid + "/" + url +
                            "' target='_blank'><b><u>Browse Tool</u></b></a>";
                    resultsList.add(browseTool + " " + liveSite + " " + dateTime.substring(0, 10) + " " +
                            dateTime.substring(11, 16) + " " + lastPathChar + " " + url + "\r");
                    if (lastPathChar.equals("-")) {
                        foundLast = true;
                        break;
                    } else {
                        referrer = columns[5];
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (foundUrl && !foundLast) {
            searchForUrl(theFile, resultOid, referrer, resultsList);
        }
    }

    /**
     * @see LogReader#getByRegularExpression(String, String, String, String, boolean, int, int).
     */
    public List<String> getByRegularExpression(String job, String filename, String regularExpression, String addLines,
                                               boolean prependLineNumbers, int skipFirstMatches, int numberOfMatches) {
        List<String> lines;

        File logFile = logProvider.getLogFile(job, filename);
        if (logFile != null) {
            lines = LogReaderUtils.getByRegularExpression(logFile.toString(), regularExpression, addLines, prependLineNumbers, skipFirstMatches, numberOfMatches);
        } else {
            lines = Arrays.asList("");
        }

        return lines;
    }

    /**
     * @see LogReader#findFirstLineBeginning(String, String, String).
     */
    public Integer findFirstLineBeginning(String job, String filename, String match) {
        Integer line = 0;

        try {
            File logFile = logProvider.getLogFile(job, filename);
            if (logFile != null) {
                line = LogReaderUtils.findFirstLineBeginning(new FileReader(logFile), match);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return line;
    }

    /**
     * @see LogReader#findFirstLineContaining(String, String, String).
     */
    public Integer findFirstLineContaining(String job, String filename, String match) {
        Integer line = 0;

        try {
            File logFile = logProvider.getLogFile(job, filename);
            if (logFile != null) {
                line = LogReaderUtils.findFirstLineContaining(new FileReader(logFile), match);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return line;
    }

    /**
     * @see LogReader#findFirstLineAfterTimeStamp(String, String, Long).
     */
    public Integer findFirstLineAfterTimeStamp(String job, String filename, Long timestamp) {
        Integer line = 0;

        try {
            File logFile = logProvider.getLogFile(job, filename);
            if (logFile != null) {
                line = findFirstLineAfterTimeStamp(new FileReader(logFile), timestamp);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return line;
    }

    /**
     * @param logProvider the logProvider to set
     */
    public void setLogProvider(LogProvider logProvider) {
        this.logProvider = logProvider;
    }

    public File retrieveLogfile(String job, String filename) {
        File logFile = logProvider.getLogFile(job, filename);
        return logFile;
    }

    /**
     * Return the line number of the first line in the
     * log/file that has a matching or later timestamp.
     *
     * @param reader    The reader of the log/file
     * @param timestamp The timestamp from which to start reading
     * @return The line number (counting from 1, not zero) of the first line
     * that matches the given regular expression. -1 is returned if no
     * line matches the regular expression. -1 also is returned if
     * errors occur (file not found, io exception etc.)
     */
    private int findFirstLineAfterTimeStamp(InputStreamReader reader, Long timestamp) {

        try {
            Pattern logPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
            Pattern longPattern = Pattern.compile("\\d{14}.*");

            BufferedReader bf = new BufferedReader(reader, 8192);

            String line = null;
            int i = 1;
            while ((line = bf.readLine()) != null) {
                StringBuilder sb = new StringBuilder();
                if (logPattern.matcher(line).matches()) {
                    sb.append(line.substring(0, 4));
                    sb.append(line.substring(5, 7));
                    sb.append(line.substring(8, 10));
                    sb.append(line.substring(11, 13));
                    sb.append(line.substring(14, 16));
                    sb.append(line.substring(17, 19));
                } else if (longPattern.matcher(line).matches()) {
                    sb.append(line.substring(0, 14));
                } else {
                    //Not a timestamp
                    i++;
                    continue;
                }
                Long ldatetime = new Long(sb.toString());
                if (ldatetime >= timestamp) {
                    // Found a match
                    return i;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

}
