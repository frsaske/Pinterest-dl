"""
PinterestDL - Python Backend Module for Chaquopy

Handles real Pinterest search scraping, pagination (50 items/batch),
and high-resolution image downloading with standardized file naming:
'PinterestDL-(title_of_pic).(filetype)' with spaces replaced by underscores.
"""

import os
import re
import json
import time
import urllib.parse
import urllib.request
import http.cookiejar
from typing import List, Dict, Any, Optional

try:
    import requests
except ImportError:
    requests = None

USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"
BASE_URL = "https://www.pinterest.com"


def sanitize_filename(title: str, extension: str = "jpg") -> str:
    """
    Critical requirement:
    Downloaded file is saved as PinterestDL-(title of pic).(filetype)
    (e.g., PinterestDL-Naruto_Cosplay.jpg). Spaces in titles are replaced with underscores.
    """
    if not title or not title.strip():
        title = "Pinterest_Image"
    
    cleaned = title.strip().replace(" ", "_")
    cleaned = re.sub(r'[\/:*?"<>|\r\n\t]', '', cleaned)
    cleaned = re.sub(r'_+', '_', cleaned)
    if len(cleaned) > 80:
        cleaned = cleaned[:80].rstrip("_")

    ext = extension.lstrip(".")
    if not ext:
        ext = "jpg"
        
    return f"PinterestDL-{cleaned}.{ext}"


def scrape_real_pinterest_search(query: str, offset: int = 0, limit: int = 50, bookmark: Optional[str] = None) -> List[Dict[str, Any]]:
    """
    Scrapes real Pinterest images for the exact query (e.g., 'sasuke').
    Uses the authenticated session warm-up protocol to extract real high-res pins.
    """
    if not query or not query.strip():
        query = "sasuke"

    clean_query = query.strip()
    source_url = f"/search/pins/?q={urllib.parse.quote(clean_query)}&rs=typed"

    cookie_jar = http.cookiejar.CookieJar()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookie_jar))

    # 1. Warm-up request to get valid session cookies & CSRF token
    warmup_headers = {
        "User-Agent": USER_AGENT,
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
        "Referer": f"{BASE_URL}/"
    }
    
    try:
        warmup_req = urllib.request.Request(f"{BASE_URL}{source_url}", headers=warmup_headers)
        with opener.open(warmup_req, timeout=10) as r:
            pass
    except Exception:
        pass

    cookies = {c.name: c.value for c in cookie_jar}
    csrftoken = cookies.get("csrftoken", "")

    # 2. Build BaseSearchResource request payload
    options = {
        "applied_unified_filters": None,
        "appliedProductFilters": "---",
        "article": None,
        "auto_correction_disabled": False,
        "corpus": None,
        "customized_rerank_type": None,
        "domains": None,
        "filters": None,
        "journey_depth": None,
        "page_size": str(limit),
        "price_max": None,
        "price_min": None,
        "query_pin_sigs": None,
        "query": clean_query,
        "redux_normalize_feed": True,
        "request_params": None,
        "rs": "typed",
        "scope": "pins",
        "selected_one_bar_modules": None,
        "source_id": None,
        "source_module_id": None,
        "seoDrawerEnabled": False,
        "source_url": source_url,
        "top_pin_id": None,
        "top_pin_ids": None
    }
    if bookmark:
        options["bookmarks"] = [bookmark]

    payload = {
        "options": options,
        "context": {}
    }

    data_str = json.dumps(payload, separators=(',', ':'))
    encoded_data = urllib.parse.quote(data_str)
    epoch = int(time.time() * 1000)
    api_url = f"{BASE_URL}/resource/BaseSearchResource/get/?source_url={urllib.parse.quote(source_url)}&data={encoded_data}&_={epoch}"

    api_headers = {
        "User-Agent": USER_AGENT,
        "Accept": "application/json, text/javascript, */*, q=0.01",
        "X-Requested-With": "XMLHttpRequest",
        "X-Pinterest-Source-Url": source_url,
        "X-Pinterest-AppState": "active",
        "X-Pinterest-PWS-Handler": "www/search/[scope].js",
        "X-CSRFToken": csrftoken,
        "Referer": f"{BASE_URL}{source_url}",
        "Accept-Language": "en-US,en;q=0.9",
    }

    results: List[Dict[str, Any]] = []

    try:
        api_req = urllib.request.Request(api_url, headers=api_headers)
        with opener.open(api_req, timeout=12) as r2:
            resp_data = json.loads(r2.read().decode("utf-8"))
            pins_data = resp_data.get("resource_response", {}).get("data", {}).get("results", [])
            for idx, p in enumerate(pins_data):
                title = p.get("grid_title") or p.get("title") or p.get("description") or f"{clean_query.title()} #{idx + 1}"
                title = title.strip()
                if not title:
                    title = f"{clean_query.title()} Pin #{idx + 1}"

                imgs = p.get("images", {})
                orig = imgs.get("orig", {}).get("url")
                # Ensure high-res JPG/PNG is preferred over HEIC for mobile display
                high_res = orig if (orig and not orig.endswith(".heic")) else (imgs.get("736x", {}).get("url") or orig)
                thumb = imgs.get("236x", {}).get("url") or imgs.get("474x", {}).get("url") or high_res
                
                if high_res:
                    results.append({
                        "id": str(p.get("id") or f"pin_{idx}"),
                        "title": title,
                        "url": high_res,
                        "thumbnail": thumb,
                        "query": clean_query
                    })
    except Exception as e:
        print(f"Error fetching real Pinterest pins: {e}")

    # Fallback if Pinterest rate limits: scrape direct pinimg links from search HTML
    if not results:
        try:
            req = urllib.request.Request(f"{BASE_URL}{source_url}", headers=warmup_headers)
            with opener.open(req, timeout=10) as r:
                html = r.read().decode("utf-8")
                matches = re.findall(r'https://i\.pinimg\.com/(?:originals|736x|564x|474x|236x)/[a-zA-Z0-9/_.-]+\.(?:jpg|png|webp|jpeg)', html)
                unique_urls = list(dict.fromkeys(matches))
                for i, u in enumerate(unique_urls[:limit]):
                    orig_url = u.replace("/236x/", "/736x/").replace("/474x/", "/736x/").replace("/564x/", "/736x/")
                    results.append({
                        "id": f"pin_html_{i}",
                        "title": f"{clean_query.title()} Image #{i + 1}",
                        "url": orig_url,
                        "thumbnail": u,
                        "query": clean_query
                    })
        except Exception:
            pass

    return results[:limit]


def get_pinterest_images_json(query: str, offset: int = 0, limit: int = 50) -> str:
    """JSON API called by Kotlin Chaquopy bridge."""
    items = scrape_real_pinterest_search(query=query, offset=offset, limit=limit)
    return json.dumps(items)


def download_image(image_url: str, title: str, download_dir: Optional[str] = None) -> str:
    try:
        ext = "jpg"
        clean_url = image_url.split("?")[0]
        if clean_url.endswith(".png"): ext = "png"
        elif clean_url.endswith(".webp"): ext = "webp"
            
        file_name = sanitize_filename(title, ext)
        if not download_dir:
            download_dir = os.path.expanduser("~/Pictures/PinterestDL")
            
        os.makedirs(download_dir, exist_ok=True)
        target_path = os.path.join(download_dir, file_name)
        
        counter = 1
        base_name, file_ext = os.path.splitext(file_name)
        while os.path.exists(target_path):
            target_path = os.path.join(download_dir, f"{base_name}_{counter}{file_ext}")
            counter += 1
            
        file_name = os.path.basename(target_path)
        
        headers = {"User-Agent": USER_AGENT, "Referer": f"{BASE_URL}/"}
        req = urllib.request.Request(image_url, headers=headers)
        with urllib.request.urlopen(req, timeout=20) as response, open(target_path, "wb") as out_file:
            out_file.write(response.read())
                
        file_size = os.path.getsize(target_path)
        return json.dumps({
            "success": True,
            "file_path": target_path,
            "file_name": file_name,
            "file_size": file_size,
            "title": title,
            "error": ""
        })
    except Exception as e:
        return json.dumps({
            "success": False,
            "file_path": "",
            "file_name": "",
            "file_size": 0,
            "title": title,
            "error": str(e)
        })
