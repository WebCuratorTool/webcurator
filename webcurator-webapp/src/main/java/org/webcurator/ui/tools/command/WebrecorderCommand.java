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
package org.webcurator.ui.tools.command;

/**
 * The command object for handling Webrecorder sessions
 * @author hanna
 */
public class WebrecorderCommand {

    public static final String ACTION_VIEW = "view";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_RECORD = "record";

    private String recordingUrl;
    private String sessionCookie;
    private String userName;
    private String recordingName;
    private String seedUrl;


    private String actionCmd = null;

    public boolean isAction(String actionString) {
        return actionString.equals(actionCmd);
    }

    public String getActionCmd() {
        return actionCmd;
    }

    public void setActionCmd(String actionCmd) {
        this.actionCmd = actionCmd;
    }

    public String getRecordingUrl() {
        return recordingUrl;
    }

    public void setRecordingUrl(String recordingUrl) {
        this.recordingUrl = recordingUrl;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRecordingName() {
        return recordingName;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }
}
