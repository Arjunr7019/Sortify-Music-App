"""Exercise player gestures on the dedicated Sortify API35 test emulator.

Requires the bundled catalog and an already completed first launch. Keeps
normal Android animation settings and restores any reduced-motion override.
"""
import re
import subprocess
import time
import ui_smoke as u

def labels(snapshot=None):
    return [n.get("text") for n in (snapshot or u.tree()).iter("node") if n.get("text")]

def bounds(node):
    return list(map(int, re.findall(r"\d+", node.get("bounds"))))

def assert_expanded(expected):
    visible = labels()
    assert ("Now playing" in visible) == expected, "Unexpected player expansion state"
    if expected:
        assert "Home" not in visible and "Your library" not in visible, "Covered browsing controls remain accessible"

def mini_center():
    # The lowest transport button belongs to the mini-player. Find its row
    # rather than relying on a title that can change as the queue advances.
    nodes = [n for n in u.tree().iter("node") if n.get("content-desc") in ("Play", "Pause")]
    assert nodes, "No mini-player transport button"
    x1,y1,x2,y2 = bounds(max(nodes, key=lambda n: bounds(n)[1]))
    return x1//2, (y1+y2)//2

def swipe_mini(distance, duration=500):
    x,y = mini_center()
    u.adb("shell", "input", "swipe", str(x), str(y), str(x), str(y-distance), str(duration))
    time.sleep(.5)

def browse_signature():
    return [(n.get("text"), n.get("bounds")) for n in u.tree().iter("node")
            if n.get("text") in ("Your daily mix", "Cruel Summer", "Perfect", "Starboy", "Levitating")]

def session():
    text = u.adb("shell", "dumpsys", "media_session").decode()
    start = text.index("package=com.sortify.app")
    part = text[start:text.index("queueTitle=", start)]
    match = re.search(r"state=PLAYING\(3\), position=(\d+)", part)
    assert match, part
    return int(match.group(1))

u.tap("Home")
u.tap("Your daily mix")
u.tap("Play")
time.sleep(1)
u.adb("shell", "input", "swipe", "400", "1700", "400", "1050", "450")
before = browse_signature()
position = session()
u.shot("gesture-browse-before-phone")

swipe_mini(55, 800)
assert_expanded(False)
x,y = mini_center()
u.adb("shell", "input", "swipe", "280", str(y), "560", str(y), "450")
assert_expanded(False)
print("PASS: short and horizontal mini-card gestures do not open the player", flush=True)

# A real device recording preserves the movement between the resting captures.
recorder = subprocess.Popen(["adb", "-s", "emulator-5554", "shell", "screenrecord", "--time-limit", "12", "/sdcard/sortify-player-gestures.mp4"], stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
time.sleep(.5)
swipe_mini(800)
assert_expanded(True)
u.shot("gesture-expanded-phone")
u.adb("shell", "input", "swipe", "500", "145", "500", "950", "550")
time.sleep(.4)
assert_expanded(False)
assert browse_signature() == before, "Playlist scroll position changed"
assert session() > position, "Playback did not advance through the transition"
u.shot("gesture-collapsed-phone")
recorder.wait(timeout=20)
u.adb("pull", "/sdcard/sortify-player-gestures.mp4", str(u.OUT / "player-gestures.mp4"))
print("PASS: swipe up opens, header swipe down closes, scroll position and playback persist", flush=True)

u.adb("shell", "input", "tap", *map(str, mini_center()))
assert_expanded(True)
u.adb("shell", "input", "swipe", "500", "145", "500", "190", "800")
assert_expanded(True)
slider = next(n for n in u.tree().iter("node") if n.get("class") == "android.widget.SeekBar")
x1,y1,x2,y2 = bounds(slider)
u.adb("shell", "input", "swipe", str(x1+(x2-x1)//5), str((y1+y2)//2), str((x1+x2)//2), str((y1+y2)//2), "500")
assert_expanded(True)
u.adb("shell", "input", "swipe", "400", "700", "400", "1350", "550")
time.sleep(.4)
assert_expanded(False)
print("PASS: tap opens; small downward drag stays open; seeking is independent; artwork swipe closes", flush=True)

swipe_mini(750)
u.tap("Open queue")
assert "Play queue" in labels()
u.back()
assert_expanded(True)
u.back()
assert_expanded(False)
assert browse_signature() == before
print("PASS: queue and system Back return through the player to the original playlist", flush=True)

u.tap("Pause")
assert_expanded(False)
u.tap("Play", index=-1)
assert_expanded(False)
session()
print("PASS: floating play/pause buttons remain independent of expansion", flush=True)

old_scale = u.adb("shell", "settings", "get", "global", "animator_duration_scale").decode().strip()
try:
    u.adb("shell", "settings", "put", "global", "animator_duration_scale", "0")
    swipe_mini(750)
    assert_expanded(True)
    u.adb("shell", "input", "swipe", "500", "145", "500", "950", "550")
    assert_expanded(False)
finally:
    if old_scale == "null": u.adb("shell", "settings", "delete", "global", "animator_duration_scale")
    else: u.adb("shell", "settings", "put", "global", "animator_duration_scale", old_scale)
print("PASS: reduced-motion gestures work; original animation setting restored", flush=True)

# Keep the search keyboard open while invoking the player.
u.tap("Search")
try: u.tap("Clear search")
except AssertionError: pass
field = next(n for n in u.tree().iter("node") if n.get("class") == "android.widget.EditText")
x1,y1,x2,y2 = bounds(field)
u.adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
u.adb("shell", "input", "text", "Blinding%sLights")
time.sleep(1)
swipe_mini(750)
assert_expanded(True)
u.tap("Close player")
assert_expanded(False)
field = next(n for n in u.tree().iter("node") if n.get("class") == "android.widget.EditText")
assert field.get("text") == "Blinding Lights", "Search query changed"
assert field.get("focused") == "false", "Search field regained focus after collapse"
assert "mInputShown=false" in u.adb("shell", "dumpsys", "input_method").decode(), "Search keyboard is still open"
u.shot("gesture-search-preserved-phone")
print("PASS: Search query persists and keyboard focus clears when opening the player", flush=True)
