import requests
import json


def login(email: str, password: str):
    body = {
        "email": email,
        "password": password
    }
    response = requests.post(url="http://127.0.0.1:9921/login", json=body)
    return json.loads(response.text)["token"]


token = login("segev95@gmail.com", "111111")
print(token)
