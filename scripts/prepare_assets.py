"""Refresh the small browse-only catalog snapshot and its original cover art.

Never bundles audio. Uses curl for the build machine's proxy. The --insecure
flag is opt-in for a development proxy; Android keeps standard TLS validation.
"""
import concurrent.futures
import json
import pathlib
import subprocess
import struct
import sys
import urllib.parse

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
ASSETS.mkdir(parents=True, exist_ok=True)
(ASSETS / "artwork").mkdir(exist_ok=True)
API = "https://saavn.bycomsolutions.com/api"
QUERIES = ["Blinding Lights The Weeknd", "Levitating Dua Lipa", "Cruel Summer Taylor Swift", "Birds of a Feather Billie Eilish", "Perfect Ed Sheeran", "Starboy The Weeknd", "Husn Anuv Jain", "Kesariya Arijit Singh", "Heeriye Jasleen Royal", "Believer Imagine Dragons", "Naatu Naatu", "Singara Siriye", "Chaleya", "Espresso Sabrina Carpenter", "Golden Harry Styles", "Stay Justin Bieber"]

def fetch(url):
    args = ["curl.exe", "-f", "-sS", "-L", "--max-time", "35"]
    if "--insecure-proxy" in sys.argv:
        args += ["-k"]
    return subprocess.check_output(args + [url])

def collect(query):
    payload = json.loads(fetch(API + "/search/songs?query=" + urllib.parse.quote(query) + "&limit=3"))
    songs = payload.get("data", {}).get("results", [])
    if not songs:
        return None
    song = songs[0]
    image = song.get("image", [])[-1]["url"]
    filename = "artwork/" + song["id"].replace("/", "_") + ".jpg"
    image_data = fetch(image)
    if image_data.startswith(b"\xff\xd8"):
        origin = ("impeccable:prompt\0Source: " + image + ". Original cover artwork returned by the music metadata service; no generated pixels.").encode("utf-8")
        # JPEG COM metadata preserves the source without changing image pixels.
        image_data = image_data[:2] + b"\xff\xfe" + struct.pack(">H", len(origin) + 2) + origin + image_data[2:]
    (ASSETS / filename).write_bytes(image_data)
    song["bundledArt"] = filename
    return song

with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
    songs = []
    for query, future in zip(QUERIES, [pool.submit(collect, q) for q in QUERIES]):
        try:
            song = future.result()
            if song:
                songs.append(song)
                print(song["name"])
        except Exception as error:
            print(query + ": " + str(error), file=sys.stderr)

if not songs:
    raise SystemExit("No catalog returned; existing assets were retained.")
(ASSETS / "catalog.json").write_text(json.dumps(songs, ensure_ascii=False, indent=2), encoding="utf-8")
provenance = [{"file": s["bundledArt"], "source": s["image"][-1]["url"], "context": "Original cover artwork returned by the supplied app's compatible music metadata API. No audio bundled."} for s in songs]
(ASSETS / "provenance.json").write_text(json.dumps(provenance, indent=2), encoding="utf-8")
