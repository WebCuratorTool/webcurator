import sys
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions
from PIL import Image

def createThumbnail(input, output):
    sourceImage = Image.open(input)
    resized = sourceImage.resize((200, 150))
    resized.save(output)
    sourceImage.close()

# Main
commandArgs = sys.argv
url = None
filepath = None
width = None
height = None
isWayback = False

# Assign argument values to variables
for arg in commandArgs:
    if arg == "--wayback":
        isWayback = True
        continue
    if "SeleniumScreenshotCapture.py" in arg:
        continue
    keyVals = arg.split("=")
    if keyVals[0] == "url":
        url = keyVals[1]
    elif keyVals[0] == "filepath":
        filepath = keyVals[1]
    elif keyVals[0] == "width":
        width = keyVals[1]
    elif keyVals[0] == "height":
        height = keyVals[1]
    else:
        print("Unrecognised argument, cannot generate screenshots.  Arg: " + arg)
        sys.exit(1)

# Set up the web driver
chromeOptions = webdriver.ChromeOptions()
chromeOptions.headless = True
driver = webdriver.Chrome(options=chromeOptions)

driver.get(url)

if isWayback:
    # Remove wayback banner and modify the iframe
    element = driver.find_element_by_tag_name("wb_div")
    driver.execute_script("return document.getElementsByTagName('wb_div')[0].remove();")
    driver.execute_script("return document.getElementById('wb_iframe_div').setAttribute('style','padding:0px 0px 0px 0px');")

    # Wait for frame to be ready then switch the focus to the frame
    wait = WebDriverWait(driver, 10)
    replay_frame = wait.until(expected_conditions.visibility_of_element_located((By.ID, "replay_iframe")))
    driver.switch_to.frame("replay_iframe")

# Set screenshot size 
if width is None and height is None: 
    S = lambda X: driver.execute_script('return document.body.parentNode.scroll'+X)
    driver.set_window_size(S('Width'),S('Height'))
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

createThumbnail(filepath, filepath.replace(size, size + "-thumbnail"))

sys.exit(0)
