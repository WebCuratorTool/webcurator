import re
from base64 import b32encode
from hashlib import sha1

# import altair as alt
# import arrow
import pandas as pd
import requests


# from tqdm.auto import tqdm

def get_latest_cdx():
    params1 = {"url": "https://www.qq.com/", "limit": 10, "from": "20220613", "to": "20220617", "output": "json"}
    params2 = {"url": "https://www.163.com/", "limit": 10}

    response = requests.get("http://localhost:9090/my-web-archive/cdx", params=params1)
    print(response.text)


if __name__ == "__main__":
    get_latest_cdx()
