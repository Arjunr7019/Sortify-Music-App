"""Run against the dedicated emulator-5554, preserving the user's physical device."""
import json
import sys
import time
import re
import xml.etree.ElementTree as ET
import ui_smoke as u

def visible():
    print([n.get("text") or n.get("content-desc") for n in u.tree().iter("node") if n.get("text") or n.get("content-desc")], flush=True)

phase = sys.argv[1] if len(sys.argv) > 1 else "home"
if phase == "home":
    try:
        u.tap("Get started")
        u.tap("Find my music")
    except AssertionError:
        pass
    u.tap("Home")
    time.sleep(8)
    u.shot("youtube-home-phone")
    visible()
elif phase == "search":
    u.tap("Search")
    u.tap("YouTube Music")
    try: u.tap("Clear search")
    except AssertionError: pass
    u.tap("Songs, artists, albums…")
    u.adb("shell", "input", "text", "Arijit%sSingh")
    u.adb("shell", "input", "keyevent", "66")
    time.sleep(5)
    u.shot("youtube-search-phone")
    visible()
elif phase == "catalog":
    u.tap("Playlists")
    time.sleep(5)
    u.shot("youtube-playlist-search-phone")
    visible()
elif phase == "playback":
    u.tap("Songs")
    time.sleep(3)
    nodes = [n for n in u.tree().iter("node") if n.get("text")]
    title = next(n.get("text") for n in nodes if n.get("text").startswith("Gehra Hua"))
    u.tap(title)
    try: u.tap("Allow")
    except AssertionError: pass
    time.sleep(15)
    u.shot("youtube-playback-phone")
    print(u.adb("shell", "dumpsys", "media_session").decode()[-9000:], flush=True)
    visible()
elif phase == "inspect":
    visible()
elif phase == "jiosaavn":
    u.tap("Search")
    u.tap("JioSaavn")
    time.sleep(5)
    u.shot("youtube-switch-jiosaavn-phone")
    visible()
elif phase == "playlist-detail":
    u.tap("Playlists")
    time.sleep(4)
    u.shot("youtube-playlist-search-phone")
    nodes = list(u.tree().iter("node"))
    title = next(n.get("text") for n in nodes if n.get("class") == "android.widget.TextView" and n.get("text") not in (
        "Find your next\nfavourite.", "Arijit Singh", "JioSaavn", "YouTube Music", "Songs", "Artists", "Albums", "Playlists", "Home", "Search", "Your library", "Create")
        and n.get("text") and n.get("bounds", "").startswith("["))
    u.tap(title)
    time.sleep(4)
    u.shot("youtube-playlist-detail-phone")
    visible()
elif phase == "language-home":
    u.tap("Home")
    u.tap("Hindi")
    u.shot("youtube-home-hindi-phone")
    visible()
elif phase == "background":
    u.adb("shell", "input", "keyevent", "3")
    time.sleep(3)
    session = u.adb("shell", "dumpsys", "media_session").decode()
    start = session.index("package=com.sortify.app")
    assert "state=PLAYING" in session[start:start+1800], "Background playback failed"
    u.adb("shell", "am", "start", "-n", "com.sortify.app/.MainActivity")
    print("PASS: YouTube Music continues playing in the background", flush=True)
elif phase == "library":
    u.tap("Songs")
    time.sleep(3)
    u.tap('Options for Gehra Hua (From "Dhurandhar")')
    visible()
elif phase == "matrix":
    font = u.adb("shell", "settings", "get", "system", "font_scale").decode().strip()
    density = u.adb("shell", "wm", "density").decode()
    size = u.adb("shell", "wm", "size").decode()
    try:
        u.adb("shell", "settings", "put", "system", "font_scale", "1.3")
        time.sleep(2)
        u.shot("youtube-search-font130-phone")
        u.tap("Home")
        u.shot("youtube-home-font130-phone")
        u.adb("shell", "settings", "put", "system", "font_scale", font)
        u.adb("shell", "wm", "size", "1600x2560")
        u.adb("shell", "wm", "density", "240")
        time.sleep(3)
        u.shot("youtube-home-tablet")
        u.tap("Search")
        u.shot("youtube-search-tablet")
    finally:
        u.adb("shell", "settings", "put", "system", "font_scale", font)
        override_size = re.search(r"Override size: (\S+)", size)
        override_density = re.search(r"Override density: (\S+)", density)
        u.adb("shell", "wm", "size", override_size[1] if override_size else "reset")
        u.adb("shell", "wm", "density", override_density[1] if override_density else "reset")
elif phase == "save-download":
    u.tap("Add to liked songs")
    u.tap('Options for Gehra Hua (From "Dhurandhar")')
    u.tap("Download song")
    for _ in range(80):
        prefs = ET.fromstring(u.adb("exec-out", "run-as", "com.sortify.app", "cat", "shared_prefs/sortify.xml"))
        saved = {n.get("name"): n.text for n in prefs}
        tracks = json.loads(saved.get("downloads", "[]"))
        match = next((s for s in tracks if s.get("id", "").startswith("yt:")), None)
        if match:
            assert match["provider"] == "YouTubeMusic"
            assert match["localPath"].startswith("file:")
            assert any(s["provider"] == "YouTubeMusic" for s in json.loads(saved.get("favorites", "[]")))
            print("PASS: YouTube Music likes and download persist with source and local file", flush=True)
            break
        time.sleep(3)
    else:
        visible()
        raise AssertionError("YouTube download did not finish")
elif phase == "verify-saved":
    prefs = ET.fromstring(u.adb("exec-out", "run-as", "com.sortify.app", "cat", "shared_prefs/sortify.xml"))
    saved = {n.get("name"): n.text for n in prefs}
    downloaded = [s for s in json.loads(saved.get("downloads", "[]")) if s.get("provider") == "YouTubeMusic"]
    assert downloaded and downloaded[0]["localPath"].startswith("file:")
    assert any(s.get("provider") == "YouTubeMusic" for s in json.loads(saved.get("favorites", "[]")))
    print("PASS: likes and completed downloads preserve YouTube Music identity", flush=True)
elif phase == "offline":
    wifi = u.adb("shell", "settings", "get", "global", "wifi_on").decode().strip()
    data = u.adb("shell", "settings", "get", "global", "mobile_data").decode().strip()
    try:
        u.adb("shell", "svc", "wifi", "disable")
        u.adb("shell", "svc", "data", "disable")
        u.tap("Your library")
        u.tap("Downloads", index=-1)
        u.tap('Gehra Hua (From "Dhurandhar")')
        time.sleep(3)
        session = u.adb("shell", "dumpsys", "media_session").decode()
        start = session.index("package=com.sortify.app")
        assert "state=PLAYING" in session[start:start+1800]
        u.shot("youtube-offline-phone")
        print("PASS: downloaded YouTube Music plays with Wi-Fi and mobile data disabled", flush=True)
    finally:
        if wifi == "1": u.adb("shell", "svc", "wifi", "enable")
        if data == "1": u.adb("shell", "svc", "data", "enable")
elif phase == "search-error":
    wifi = u.adb("shell", "settings", "get", "global", "wifi_on").decode().strip()
    data = u.adb("shell", "settings", "get", "global", "mobile_data").decode().strip()
    try:
        u.tap("Search")
        u.tap("YouTube Music")
        u.adb("shell", "svc", "wifi", "disable")
        u.adb("shell", "svc", "data", "disable")
        u.tap("Clear search")
        u.tap("Songs, artists, albums…")
        u.adb("shell", "input", "text", "Arijit%sSingh")
        u.adb("shell", "input", "keyevent", "66")
        time.sleep(4)
        u.find("Retry")
        u.shot("youtube-search-error-phone")
    finally:
        if wifi == "1": u.adb("shell", "svc", "wifi", "enable")
        if data == "1": u.adb("shell", "svc", "data", "enable")
    time.sleep(2)
    u.tap("Retry")
    time.sleep(4)
    u.find('Options for Gehra Hua (From "Dhurandhar")')
    u.shot("youtube-search-recovered-phone")
    print("PASS: YouTube search shows error and recovers with Retry", flush=True)
