===============================
Harvest Screenshot Configuration Guide
===============================


Introduction
============

The Web Curator Tool is able to generate screenshots of seed URLs for Target Instances. Screenshots are generated from 
the live version and harvested version of each web page that corresponds to a seed ULR in the Target Instance.

This guide shows how to deploy and configure an instance of Web Curator Tool to generate harvest screenshots.


Contents of this document
-------------------------

Following this introduction, the Rosetta DPS Configuration Guide includes the following sections:

-   **Java or Python** - Covers the different implementation options.

-   **Installation** - Covers installing necessary 3rd-party tools.

-   **Configuration** - Covers the backend configuration options.

-   **Usage** - Covers using the screenshot generation.

-   **More information** - Provides some links for more information.

*All configuration for this integration is inside `application.properties`. (This file is located in `webcurator-store.war/WEB-INF/classes/`.*


Java or Python
===================================

There are two implementation options provided for the screenshot generation - built-in Java or Python.

The built-in Java implementation uses the Selenium libraries for Java and requires minimal setup.

If a Python implementation is preferred for using Selenium, then this is also bundled with WCT. This implementation 
requires additional setup of Python and dependencies.


Installation
============

In order to use screenshot generation in WCT, minimally Chrome and the Chrome Driver must be installed.

Chrome
------

Install the latest stable release of Google Chrome for your operating system. The recent versions of Chrome now include
'headless' mode, so there is no advantage to specifically installing the headless version of Chrome.

Installing on RHEL 8 and newer

-   Setup local Chrome yum repo - create /etc/yum.repos.d/google-chrome.repo
        
        [google-chrome]
        name=google-chrome
        baseurl=https://dl.google.com/linux/chrome/rpm/stable/x86_64
        enabled=1
        gpgcheck=1
        gpgkey=https://dl.google.com/linux/linux_signing_key.pub

-   Check google-chrome-stable package

        yum info google-chrome-stable

-   Install google-chrome-stable package

        yum install google-chrome-stable

-   Confirm Chrome version installed 

        google-chrome-stable --version

Installing on Ubuntu

-   Update package manager

        sudo apt update
        
-   Download the google-chrome-stable package

        wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb

-   Install google-chrome-stable package

        sudo apt install ./google-chrome-stable_current_amd64.deb

-   Confirm Chrome version installed 

        google-chrome-stable --version
        
        



yum install google-chrome-stable


 


check installation


Installing the Chrome driver
----------------------------
Needs to be same minor version as Chrome


wget https://storage.googleapis.com/chrome-for-testing-public/122.0.6261.94/linux64/chromedriver-linux64.zip
unzip chromedriver-linux64.zip
sudo cp chromedriver-linux64/chromedriver /usr/bin/chromedriver
chmod +x /usr/bin/chromedriver

-   Confirm Chrome driver installed 

        chromedriver -v



Python implementation
---------------------
If using the Python implementation, the following must also be installed:

-   Python 3.x
-   Pip3
-   Selenium Python package (pip3 install selenium)
-   Pillow Python package (pip3 install pillow)



Testing Chrome
---------------------


Configuration
===================

Python implementation
---------------------
setting up virtual python environment for installing selenium and pillow, configure wct store to be able to use

