#!/usr/bin/python3

import os
import sys

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions
from PIL import Image


def create_thumbnail(input, output):
    source_image = Image.open(input)
    resized = source_image.resize((200, 150))
    resized.save(output)
    source_image.close()


# Main
def main(command_args):
    url = None
    filepath = None
    width = None
    height = None
    server = None
    is_wayback = False
    wayback_type = None
    wayback_version = None

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
        elif key_vals[0] == "wayback-name":
            wayback_type = key_vals[1]
        elif key_vals[0] == "wayback-version":
            wayback_version = key_vals[1]
        else:
            print("Warning: unrecognised argument.  Arg: " + arg)
            # sys.exit(1)

    # file_directory = Path(filepath).parent
    # file_directory.mkdir(parents=True, exist_ok=True)

    # Set up the web driver
    chrome_options = webdriver.ChromeOptions()
    chrome_options.add_argument("--headless")

    if server is not None:
        driver = webdriver.Remote(command_executor=server, options=chrome_options)
    else:
        driver = webdriver.Chrome(options=chrome_options)

    if is_wayback:
        if wayback_type is None:
            wayback_type = "pywb"
        else:
            wayback_type = str(wayback_type).lower()
        if wayback_version is None:
            wayback_version = "2.7.3"
        else:
            wayback_version = str(wayback_version)

        if wayback_type == "owb":
            driver.get(url)
            # Remove wayback banner and modify the iframe
            wait = WebDriverWait(driver, 10)
            wait.until(expected_conditions.visibility_of_element_located((By.ID, "wm-ipp")))
            driver.execute_script("return document.getElementById('wm-ipp').style.display='none';")
        elif wayback_type == "pywb":
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
            print(f"Not supported.")
            sys.exit(-1)
    else:
        driver.get(url)

    driver.execute_script("window.scrollTo(0, 0)")

    # Set screenshot size
    if width is None and height is None:
        driver.fullscreen_window()
        s = lambda x: driver.execute_script('return document.body.parentNode.scroll' + x)
        driver.set_window_size(s('Width') + 20, s('Height'))
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

    # sys.exit(0)


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
