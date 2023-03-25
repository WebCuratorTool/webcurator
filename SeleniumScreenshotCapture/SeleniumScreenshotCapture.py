#!/usr/bin/python3

import os
import sys
from enum import Enum

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions
from PIL import Image


class WayBackType(Enum):
    wayback = "wayback"
    openwayback = "openwayback"
    pywb = "pywb"


# Set the wayback type in the capture script, so it's convenient to customize to use different wayback tools.
wayback_type = WayBackType.pywb


def create_thumbnail(input, output):
    sourceImage = Image.open(input)
    resized = sourceImage.resize((200, 150))
    resized.save(output)
    sourceImage.close()


# Main
def main(command_args):
    url = None
    filepath = None
    width = None
    height = None
    server = None
    is_wayback = False
    # Assign argument values to variables
    for arg in command_args:
        if arg == "--wayback":
            is_wayback = True
            continue
        if "SeleniumScreenshotCapture.py" in arg:
            continue
        key_vals = arg.split("=")
        if key_vals[0] == "url":
            url = key_vals[1]
        elif key_vals[0] == "filepath":
            filepath = key_vals[1]
        elif key_vals[0] == "width":
            width = key_vals[1]
        elif key_vals[0] == "height":
            height = key_vals[1]
        elif key_vals[0] == "selenium-server":
            server = key_vals[1]
        else:
            print("Warning: unrecognised argument.  Arg: " + arg)
            # sys.exit(1)

    # file_directory = Path(filepath).parent
    # file_directory.mkdir(parents=True, exist_ok=True)

    # Set up the web driver
    chrome_options = webdriver.ChromeOptions()
    chrome_options.headless = True
    if server is not None:
        driver = webdriver.Remote(command_executor=server, options=chrome_options)
    else:
        driver = webdriver.Chrome(options=chrome_options)

    if is_wayback:
        if wayback_type == WayBackType.wayback or wayback_type == WayBackType.openwayback:
            driver.get(url)
            # Remove wayback banner and modify the iframe
            wait = WebDriverWait(driver, 10)
            wait.until(expected_conditions.visibility_of_element_located((By.ID, "wm-ipp")))
            driver.execute_script("return document.getElementById('wm-ipp').style.display='none';")
        elif wayback_type == WayBackType.pywb:
            iframe_style = "display:block; overflow:hidden; height:100%; width:100%; margin:0px; border:0px; padding:0px;"
            iframe_html = f"<iframe id='replay_iframe' src='{url}' frameborder='0' style='{iframe_style}'></iframe>"
            html = f"""<!DOCTYPE html>
                    <html style='width: 100wh; height: 100vh; margin:0px; border:0px; padding:0px; overflow: hidden;'>
                    <body style='width: 100wh; height: 100vh; margin:0px; border:0px; padding:0px; overflow: hidden;'>
                    {iframe_html}
                    </body>
                    </html>"""
            driver.get(f"data:text/html;charset=utf-8,{html}")
            wait = WebDriverWait(driver, 10)
            wait.until(expected_conditions.visibility_of_element_located((By.ID, "replay_iframe")))
            driver.switch_to.frame("replay_iframe")
    else:
        driver.get(url)

    # Set screenshot size
    if width is None and height is None:
        s = lambda x: driver.execute_script('return document.body.parentNode.scroll' + x)
        driver.set_window_size(s('Width'), s('Height'))
    else:
        driver.set_window_size(width, height)

    driver.save_screenshot(filepath)
    driver.close()

    # Generate screenshot thumbnails
    size = None
    if "screen" in filepath:
        size = "screen"
    elif "fullpage" in filepath:
        size = "fullpage"

    create_thumbnail(filepath, filepath.replace(size, size + "-thumbnail"))

    sys.exit(0)


if __name__ == "__main__":
    # Export Environment
    curr_directory = os.path.dirname(__file__)
    path = os.environ.get("PATH")
    if path is None:
        path = curr_directory
    elif curr_directory not in path:
        path = path + ":" + curr_directory
    os.environ["PATH"] = path
    main(sys.argv)
