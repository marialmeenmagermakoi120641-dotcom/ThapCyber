from flask import Flask, request, jsonify
from dotenv import load_dotenv
import requests
import os
import base64

load_dotenv()

app = Flask(__name__)

VT_API_KEY = os.getenv("VT_API_KEY")


@app.route("/")
def home():
    return jsonify({
        "status": "online",
        "service": "ThapCyber Backend",
        "message": "Cybersecurity API is running"
    })


@app.route("/check-url", methods=["POST"])
def check_url():

    data = request.get_json()

    if not data or "url" not in data:
        return jsonify({"error": "URL is required"}), 400

    url = data["url"].strip()

    if not url:
        return jsonify({"error": "URL cannot be empty"}), 400

    if not VT_API_KEY:
        return jsonify({
            "error": "VirusTotal API key is not configured"
        }), 500

    try:
        url_id = base64.urlsafe_b64encode(
            url.encode()
        ).decode().strip("=")

        headers = {
            "x-apikey": VT_API_KEY
        }

        response = requests.get(
            f"https://www.virustotal.com/api/v3/urls/{url_id}",
            headers=headers,
            timeout=15
        )

        if response.status_code == 200:
            result = response.json()
            attributes = result["data"]["attributes"]

            stats = attributes.get("last_analysis_stats", {})

            malicious = stats.get("malicious", 0)
            suspicious = stats.get("suspicious", 0)
            harmless = stats.get("harmless", 0)
            undetected = stats.get("undetected", 0)

            if malicious > 0:
                risk = "High Risk"
                safe = False
            elif suspicious > 0:
                risk = "Medium Risk"
                safe = False
            else:
                risk = "Low Risk"
                safe = True

            return jsonify({
                "url": url,
                "safe": safe,
                "risk": risk,
                "malicious": malicious,
                "suspicious": suspicious,
                "harmless": harmless,
                "undetected": undetected,
                "message": "VirusTotal analysis completed"
            })

        elif response.status_code == 404:
            return jsonify({
                "url": url,
                "safe": None,
                "risk": "Unknown",
                "message": "URL not found in VirusTotal"
            })

        else:
            return jsonify({
                "error": "VirusTotal API error",
                "status_code": response.status_code
            }), 502

    except requests.RequestException:
        return jsonify({
            "error": "Unable to connect to VirusTotal"
        }), 503


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5001,
        debug=True
    )
