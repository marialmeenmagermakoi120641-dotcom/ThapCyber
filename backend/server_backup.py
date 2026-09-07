from flask import Flask, request, jsonify

app = Flask(__name__)


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
        return jsonify({
            "error": "URL is required"
        }), 400

    url = data["url"].strip()

    if not url:
        return jsonify({
            "error": "URL cannot be empty"
        }), 400

    # Temporary response.
    # We will connect this endpoint to real threat intelligence next.
    return jsonify({
        "url": url,
        "safe": True,
        "risk": "Unknown",
        "message": "URL received successfully"
    })


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5001,
        debug=True
    )
