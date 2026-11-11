"""Check a running Courtly API using only the Python standard library."""
import json
import os
import urllib.error
import urllib.request

BASE_URL = os.environ.get("API_URL", "http://127.0.0.1:8080")


def request(method, path, body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(BASE_URL + path, data=data, method=method,
                                 headers={"Content-Type": "application/json"})
    try:
        response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        content = response.read()
        return response.status, json.loads(content) if content else None


def main():
    status, courts = request("GET", "/courts")
    assert status == 200 and len(courts) == 2, (status, courts)
    created_ids = []
    body = {"courtId": 1, "date": "2099-01-01", "startTime": "10:00", "endTime": "11:00"}
    try:
        status, first = request("POST", "/bookings", body)
        assert status == 201, (status, first)
        created_ids.append(first["id"])
        status, error = request("POST", "/bookings", body)
        assert status == 409, (status, error)
        for change in ({"courtId": 2}, {"date": "2099-01-02"}):
            status, booking = request("POST", "/bookings", body | change)
            assert status == 201, (status, booking)
            created_ids.append(booking["id"])
        status, slots = request("GET", "/courts/1/availability?date=2099-01-01")
        assert status == 200 and len(slots) == 28, (status, slots)
        assert slots[4]["bookingId"] == first["id"] and not slots[4]["available"]
        status, error = request("POST", "/bookings", body | {"startTime": "10:15"})
        assert status == 400, (status, error)
        status, error = request("POST", "/bookings", {})
        assert status == 400, (status, error)
        status, _ = request("DELETE", "/bookings/" + str(first["id"]))
        assert status == 204, status
        created_ids.remove(first["id"])
        status, _ = request("GET", "/bookings/" + str(first["id"]))
        assert status == 404, status
        status, slots = request("GET", "/courts/1/availability?date=2099-01-01")
        assert status == 200 and slots[4]["available"] and slots[5]["available"]
        print("API smoke checks passed.")
    finally:
        for booking_id in created_ids:
            request("DELETE", "/bookings/" + str(booking_id))


if __name__ == "__main__":
    main()
