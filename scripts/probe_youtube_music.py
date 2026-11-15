"""Read-only probes of the public endpoints used by the native adapter.

Saves public metadata fixtures with --fixtures for parser contract tests.
Never reads browser cookies or Google credentials.
"""
import argparse
import datetime
import json
import pathlib
import re
import urllib.request
import urllib.parse

BASE = "https://music.youtube.com"
UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

def request(endpoint, **fields):
    version = datetime.datetime.now(datetime.timezone.utc).strftime("1.%Y%m%d.01.00")
    fields["context"] = {"client": {"clientName": "WEB_REMIX", "clientVersion": version, "hl": "en", "gl": "IN"}}
    req = urllib.request.Request(BASE + "/youtubei/v1/" + endpoint + "?alt=json", data=json.dumps(fields).encode(),
                                 headers={"User-Agent": UA, "Content-Type": "application/json", "Origin": BASE})
    with urllib.request.urlopen(req, timeout=30) as response:
        return json.load(response)

def renderers(value):
    if isinstance(value, dict):
        for key in ("musicResponsiveListItemRenderer", "musicTwoRowItemRenderer"):
            if key in value:
                yield value
                return
        for child in value.values():
            yield from renderers(child)
    elif isinstance(value, list):
        for child in value:
            yield from renderers(child)

def fixture(value):
    """Keep public renderer fields needed by the parser; omit tracking and action menus."""
    if isinstance(value, dict):
        return {k: fixture(v) for k, v in value.items() if k not in (
            "trackingParams", "clickTrackingParams", "menu", "overlay", "thumbnailOverlay", "accessibility",
            "loggingContext", "accessibilityData", "thumbnailCrop", "thumbnailScale", "displayPriority",
            "aspectRatio", "browseEndpointContextSupportedConfigs")}
    if isinstance(value, list):
        return [fixture(v) for v in value]
    return value

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--fixtures", type=pathlib.Path)
    parser.add_argument("--playback", action="store_true")
    args = parser.parse_args()
    cases = {
        "songs": ("search", {"query": "Arijit Singh", "params": "EgWKAQIIAWoMEA4QChADEAQQCRAF"}),
        "playlists": ("search", {"query": "Hindi music", "params": "Eg-KAQwIABAAGAAgACgBMABqChAEEAMQCRAFEAo%3D"}),
        "charts": ("browse", {"browseId": "FEmusic_charts", "formData": {"selectedValues": ["IN"]}}),
    }
    for name, (endpoint, body) in cases.items():
        try:
            response = request(endpoint, **body)
            items = list(renderers(response))
            print(name, "renderers:", len(items), "error:", response.get("error"), flush=True)
            if args.fixtures:
                args.fixtures.mkdir(parents=True, exist_ok=True)
                (args.fixtures / (name + ".json")).write_text(json.dumps(fixture({"contents": items[:2]}), ensure_ascii=False, indent=2), encoding="utf-8")
        except Exception as error:
            print(name, type(error).__name__, str(error), flush=True)
    if args.playback:
        try:
            def get_text(url):
                with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": UA}), timeout=30) as r:
                    return r.read().decode()
            page = get_text(BASE)
            path = re.search(r'"jsUrl"\s*:\s*"([^"]+)"', page) or re.search(r'src="([^"]+/base\.js)"', page)
            if not path:
                raise RuntimeError("No player JavaScript URL")
            script = get_text(urllib.parse.urljoin(BASE, path[1].replace("\\/", "/")))
            timestamp = int(re.search(r'signatureTimestamp\s*[:=]\s*(\d+)', script)[1])
            result = request("player", videoId="i1o1p_DD6TU", playbackContext={"contentPlaybackContext": {"signatureTimestamp": timestamp}})
            formats = result.get("streamingData", {}).get("adaptiveFormats", [])
            audio = [f for f in formats if f.get("mimeType", "").startswith("audio/") and f.get("url", "").startswith("https://")]
            print("player status:", result.get("playabilityStatus", {}).get("status"), "direct audio:", len(audio), flush=True)
            if audio:
                req = urllib.request.Request(audio[0]["url"], headers={"User-Agent": UA, "Range": "bytes=0-1023"})
                with urllib.request.urlopen(req, timeout=30) as response:
                    print("audio HTTP:", response.status, "bytes:", len(response.read(1024)), flush=True)
        except Exception as error:
            print("playback", type(error).__name__, str(error), flush=True)
