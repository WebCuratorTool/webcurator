# import re
# from base64 import b32encode
# from hashlib import sha1

# import altair as alt
# import arrow
# import pandas as pd
import requests
from selenium import webdriver


# from selenium.webdriver.common.by import By


# from tqdm.auto import tqdm

def get_latest_cdx():
    params1 = {"url": "https://www.qq.com/", "limit": 10, "from": "20220613", "to": "20220617", "output": "json"}
    params2 = {"url": "https://www.163.com/", "limit": 10}

    response = requests.get("http://localhost:1080/my-web-archive/cdx", params=params1)
    print(response.text)


def screen_shot():
    options = webdriver.ChromeOptions()
    # options.add_argument("--headless=new")

    driver = webdriver.Chrome(options=options)

    url = "http://localhost:1080/my-web-archive/20230207222650mp_/https://www.rnz.co.nz/"
    html = f"""<!DOCTYPE html>
    <html>
    <body style='width: 100wh; height: 100vh; overflow: hidden;'>
      <iframe id='wrapped_replay_iframe' src='{url}' frameborder='0' style='overflow:hidden;height:100%;width:100%' height='100%' width='100%'></iframe>
    </body>
    </html>"""

    driver.get("data:text/html;charset=utf-8," + html)

    # driver.get("https://www.selenium.dev/selenium/web/web-form.html")

    # title = driver.title
    # assert title == "Web form"
    #
    # driver.implicitly_wait(0.5)
    #
    # text_box = driver.find_element(by=By.NAME, value="my-text")
    # submit_button = driver.find_element(by=By.CSS_SELECTOR, value="button")
    #
    # text_box.send_keys("Selenium")
    # submit_button.click()
    #
    # message = driver.find_element(by=By.ID, value="message")
    # value = message.text
    # assert value == "Received!"

    S = lambda X: driver.execute_script('return document.body.parentNode.scroll' + X)
    a, b = S("Width"), S("Height")
    driver.set_window_size(S('Width'), S('Height'))

    driver.save_screenshot("/tmp/a.png")

    driver.quit()


if __name__ == "__main__":
    # get_latest_cdx()
    screen_shot()
