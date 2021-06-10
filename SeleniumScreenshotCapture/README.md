Selenium Screenshot Tool

This is the default screenshot tool to be used with WCT for screenshot generation.
It uses selenium which requires a web driver.  In this case, it uses chromedriver.  

To use it with WCT, download chromedriver from  https://chromedriver.chromium.org/downloads and save it to the same location that you have placed WCT store jar.
Then either copy the python file to the same location that you have placed the WCT Store jar
or run gradle clean build and copy the jar from build/libs to the same location that you have the WCT Store jar.

This tool assumes that WCT uses pywb as it's wayback access tool.
Generating screenshots with selenium has been problematic because the focus will be on the wayback banner.  
To get around this, the tool changes the focus to the iframe_replay and removes the wayback banner.

The arguments that it requires are the filepath for the image, the url for the seed, and optionally the screen size for the image.
Another optional argument is --wayback, which tells the tool to change the focus and remove the wayback banner.
