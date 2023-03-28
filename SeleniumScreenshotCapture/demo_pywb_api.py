import SeleniumScreenshotCapture as capture


def screen_shot(url, file_path, wayback_options=None, fullpage_size=None):
    command_args = ["SeleniumScreenshotCapture.py", f"url={url}", f"filepath={file_path}"]

    if fullpage_size is not None:
        width, height = fullpage_size
        command_args.append(f"width={width}")
        command_args.append(f"height={height}")

    if wayback_options is not None:
        wayback_type, wayback_version, wayback_url = wayback_options
        command_args.append("--wayback")
        command_args.append(f"wayback-name={wayback_type}")
        command_args.append(f"wayback-version={wayback_version}")
    capture.main(command_args)
    print(command_args)


def main():
    fullpage_size = (1400, 800)
    root_file_path = "/tmp"
    seed_url = "https://www.rnz.co.nz/news"

    file_path = f"{root_file_path}/live_screen.png"
    screen_shot(seed_url, file_path, fullpage_size=fullpage_size)

    file_path = f"{root_file_path}/live_fullpage.png"
    screen_shot(seed_url, file_path)

    all_wayback = [
        ("pywb", "2.7.3", "http://localhost:1080/my-web-archive/20230207222650mp_"),
        ("pywb", "2.6.7", "http://localhost:2080/my-web-archive/20230207222650mp_"),
        ("owb", "2.4.0", "http://localhost:8080/wayback/20230207222650"),
    ]

    for wayback_options in all_wayback:
        wayback_type, wayback_version, wayback_url = wayback_options

        url = f"{wayback_url}/{seed_url}"
        file_path = f"{root_file_path}/wayback_{wayback_type}_{wayback_version}_screen.png"
        screen_shot(url, file_path, wayback_options=wayback_options, fullpage_size=fullpage_size)

        file_path = f"{root_file_path}/wayback_{wayback_type}_{wayback_version}_fullpage.png"
        screen_shot(url, file_path, wayback_options=wayback_options)


def pywb():
    fullpage_size = (1400, 800)
    root_file_path = "/tmp"
    seed_url = "https://www.rnz.co.nz/news"

    wayback_options = (
        "pywb", "2.7.3", "http://localhost:1080/my-web-archive/20230207222650mp_"
    )
    wayback_type, wayback_version, wayback_url = wayback_options
    url = f"{wayback_url}/{seed_url}"
    file_path = f"{root_file_path}/wayback_{wayback_type}_{wayback_version}_screen.png"
    screen_shot(url, file_path, wayback_options=wayback_options, )


if __name__ == "__main__":
    main()
    # pywb()
