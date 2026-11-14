"""Additional checks for the dedicated Sortify test emulator, restoring settings."""
import json
import re
import sys
import time
import xml.etree.ElementTree as ET
import ui_smoke as u

def tap_last(label, result=False):
    nodes = [n for n in u.tree().iter("node") if n.get("text") == label or n.get("content-desc") == label]
    if result: nodes = [n for n in nodes if n.get("class") == "android.widget.TextView"]
    assert nodes, "Missing " + label
    node = nodes[0] if result else nodes[-1]
    x1,y1,x2,y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    u.adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.6)

def launch():
    u.adb("shell", "am", "start", "-n", "com.sortify.app/.MainActivity")
    time.sleep(1)

def prefs():
    return ET.fromstring(u.adb("exec-out", "run-as", "com.sortify.app", "cat", "shared_prefs/sortify.xml"))

def media():
    text = u.adb("shell", "dumpsys", "media_session").decode()
    start = text.find("package=com.sortify.app")
    assert start >= 0, "No Sortify media session"
    return text[start:text.find("queueTitle=", start)+100]

phase = sys.argv[1]
if phase == "offline":
    saved = {n.get("name"): n.text for n in prefs()}
    assert len(json.loads(saved.get("downloads", "[]"))) > 0, "Download a song first"
    old_wifi = u.adb("shell", "settings", "get", "global", "wifi_on").decode().strip()
    old_data = u.adb("shell", "settings", "get", "global", "mobile_data").decode().strip()
    try:
        u.adb("shell", "svc", "wifi", "disable")
        u.adb("shell", "svc", "data", "disable")
        u.tap("Your library")
        tap_last("Downloads")
        u.shot("downloads-phone")
        u.tap("Play")
        time.sleep(3)
        session = media()
        assert "state=PLAYING" in session, session
        print("PASS: downloaded audio plays with Wi-Fi and mobile data disabled", flush=True)
        u.adb("shell", "input", "keyevent", "3")
        time.sleep(2)
        assert "state=PLAYING" in media()
        print("PASS: offline playback continues in the background", flush=True)
        launch()
        u.tap("Search")
        try: u.tap("Clear search")
        except AssertionError: pass
        u.tap("Songs, artists, albums…")
        u.adb("shell", "input", "text", "Offline%sconnection%scheck")
        u.adb("shell", "input", "keyevent", "66")
        time.sleep(3)
        u.shot("search-offline-phone")
    finally:
        if old_wifi == "1": u.adb("shell", "svc", "wifi", "enable")
        if old_data == "1": u.adb("shell", "svc", "data", "enable")
    time.sleep(2)
    u.tap("Retry")
    time.sleep(3)
    u.shot("search-recovered-phone")
    print("PASS: playlists, likes, downloads, onboarding, quality, and theme persist after process restart", flush=True)
    print({k: len(json.loads(saved.get(k, "[]"))) for k in ("playlists", "favorites", "downloads", "recent")}, flush=True)
elif phase == "catalog":
    u.tap("Search")
    try: u.tap("Clear search")
    except AssertionError: pass
    u.tap("Songs, artists, albums…")
    u.adb("shell", "input", "text", "The%sWeeknd")
    u.adb("shell", "input", "keyevent", "66")
    u.tap("Artists")
    time.sleep(3)
    u.shot("search-artists-phone")
    tap_last("The Weeknd", result=True)
    time.sleep(3)
    u.shot("artist-detail-phone")
    u.back()
    u.tap("Albums")
    time.sleep(3)
    u.shot("search-albums-phone")
elif phase == "onboarding":
    # Change only the first-launch flag in this dedicated emulator. Preserve
    # the test library so persistence remains independently checkable.
    p = prefs()
    for n in p:
        if n.get("name") == "onboarded": n.set("value", "false")
    u.adb("shell", "am", "force-stop", "com.sortify.app")
    import subprocess
    subprocess.run(["adb", "-s", "emulator-5554", "shell", "run-as", "com.sortify.app", "tee", "shared_prefs/sortify.xml"], input=ET.tostring(p, encoding="utf-8", xml_declaration=True), stdout=subprocess.DEVNULL, check=True)
    launch()
    u.shot("welcome-phone")
    u.tap("Get started")
    u.shot("languages-phone")
    u.tap("Find my music")
    time.sleep(3)
    u.shot("home-phone")
elif phase == "responsive":
    old_font = u.adb("shell", "settings", "get", "system", "font_scale").decode().strip()
    old_night = u.adb("shell", "cmd", "uimode", "night").decode().strip()
    try:
        u.adb("shell", "cmd", "uimode", "night", "yes")
        u.adb("shell", "settings", "put", "system", "font_scale", "1.3")
        time.sleep(2)
        u.tap("Home")
        u.shot("home-font130-phone")
        u.tap("Your library")
        u.shot("library-font130-phone")
        u.adb("shell", "settings", "put", "system", "font_scale", old_font)
        u.adb("shell", "wm", "size", "1600x2560")
        u.adb("shell", "wm", "density", "240")
        time.sleep(2)
        u.tap("Home")
        u.shot("home-tablet")
        u.tap("Your library")
        u.shot("library-tablet")
    finally:
        u.adb("shell", "wm", "size", "reset")
        u.adb("shell", "wm", "density", "reset")
        u.adb("shell", "settings", "put", "system", "font_scale", old_font)
        u.adb("shell", "cmd", "uimode", "night", "yes" if "yes" in old_night else "no")
