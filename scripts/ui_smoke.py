"""Drive the installed debug app through adb's accessibility tree.

Run on a dedicated test emulator. Leaves the test playlist in the app, does
not clear data, and stores original emulator screenshots under docs/screenshots.
"""
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / ".impeccable/review"
OUT.mkdir(parents=True, exist_ok=True)

def adb(*args):
    return subprocess.check_output(["adb", "-s", "emulator-5554", *args])

def tree():
    for attempt in range(4):
        output = adb("shell", "uiautomator", "dump", "/sdcard/sortify-ui.xml")
        if b"dumped to" in output:
            return ET.fromstring(adb("exec-out", "cat", "/sdcard/sortify-ui.xml"))
        time.sleep(.5)
    raise AssertionError("Android accessibility tree did not become ready")

def find(label, partial=False, index=0):
    nodes = list(tree().iter("node"))
    for key in ("content-desc", "text"):
        matches = [n for n in nodes if (label in n.get(key, "") if partial else n.get(key) == label)]
        if matches:
            return matches[index]
    raise AssertionError("Missing UI control: " + label + "; visible: " + str([n.get("text") or n.get("content-desc") for n in nodes if n.get("text") or n.get("content-desc")]))

def tap(label, partial=False, index=0):
    n = find(label, partial, index)
    x1,y1,x2,y2 = map(int, re.findall(r"\d+", n.get("bounds")))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.5)

def shot(name):
    snapshot = tree()
    time.sleep(.8)
    (OUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p"))
    (OUT / (name + ".xml")).write_bytes(ET.tostring(snapshot))
    print("Captured", name, flush=True)

def back():
    adb("shell", "input", "keyevent", "4")
    time.sleep(.4)

if __name__ == "__main__":
    import sys
    phase = sys.argv[1] if len(sys.argv) > 1 else "onboarding"
    if phase == "onboarding":
        shot("welcome-phone")
        tap("Get started")
        shot("languages-phone")
        tap("Find my music")
        time.sleep(4)
        shot("home-phone")
    elif phase == "library":
        tap("Create")
        shot("create-empty-phone")
        node = next(n for n in tree().iter("node") if n.get("class") == "android.widget.EditText")
        x1,y1,x2,y2 = map(int, re.findall(r"\d+", node.get("bounds")))
        adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
        adb("shell", "input", "text", "The%slong%sway%shome")
        back()
        tap("Blinding Lights")
        tap("Levitating")
        shot("create-filled-phone")
        tap("Create playlist", partial=True)
        shot("playlist-phone")
        tap("Options for Blinding Lights")
        shot("song-options-phone")
        tap("Add to liked songs")
        tap("Your library")
        shot("library-phone")
        tap("Liked songs")
        shot("liked-phone")
        back()
        tap("Downloads", index=-1)
        shot("downloads-empty-phone")
        tap("Home")
    elif phase == "playback":
        tap("Your daily mix")
        tap("Play")
        time.sleep(3)
        try: tap("Allow")
        except AssertionError: pass
        shot("playlist-playing-phone")
        tap("Blinding Lights")
        # The row toggles; use mini-player title with a lower coordinate instead.
        adb("shell", "input", "tap", "410", "2070")
        shot("player-phone")
    elif phase == "search":
        tap("Search")
        shot("search-browse-phone")
        tap("Songs, artists, albums…")
        adb("shell", "input", "text", "Blinding%sLights")
        adb("shell", "input", "keyevent", "66")
        time.sleep(5)
        shot("search-results-phone")
    elif phase == "settings":
        tap("Home")
        tap("Settings")
        shot("settings-phone")
        tap("Audio quality")
        shot("quality-phone")
        tap("320kbps")
        tap("Appearance")
        tap("Light")
        shot("settings-light-phone")
        tap("Appearance")
        tap("Dark")
        back()
