import uvicorn
import firebase_admin
import pyrebase
import json
from firebase_admin import credentials, auth
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import HTTPException

cred = credentials.Certificate('famileat-1cd47-firebase-adminsdk-1tdvb-547c7e6997.json')
firebase = firebase_admin.initialize_app(cred)
load = json.load(open('google-services.json'))
api_key = load["client"][0]["api_key"][0]["current_key"]
try:
    autoDomain = load["authDomain"]
except KeyError:
    autoDomain = "your-auth-domain.firebaseapp.com"
try:
    database = load["databaseURL"]
except KeyError:
    database = "https://your-project-id.firebaseio.com"

pb = pyrebase.initialize_app({
    "apiKey": api_key,
    "authDomain": autoDomain,
    "databaseURL": database,
    "projectId": "famileat-1cd47",
    "storageBucket": "famileat-1cd47.appspot.com",
})
app = FastAPI()
allow_all = ['*']
app.add_middleware(
    CORSMiddleware,
    allow_origins=allow_all,
    allow_credentials=True,
    allow_methods=allow_all,
    allow_headers=allow_all
)

# signup endpoint
# signup endpoint
@app.post("/signup", include_in_schema=False)
async def signup(request: Request):
   req = await request.json()
   email = req['email']
   password = req['password']
   if email is None or password is None:
       return HTTPException(detail={'message': 'Error! Missing Email or Password'}, status_code=400)
   try:
       user = auth.create_user(
           email=email,
           password=password
       )
       return JSONResponse(content={'message': f'Successfully created user {user.uid}'}, status_code=200)
   except:
       return HTTPException(detail={'message': 'Error Creating User'}, status_code=400)
# login endpoint
@app.post("/login", include_in_schema=False)
async def login(request: Request):
   req_json = await request.json()
   email = req_json['email']
   password = req_json['password']
   try:
       user = pb.auth().sign_in_with_email_and_password(email, password)
       jwt = user['localId']
       print(user)
       return JSONResponse(content={'token': jwt}, status_code=200)
   except:
       return HTTPException(detail={'message': 'There was an error logging in'}, status_code=400)
# ping endpoint

if __name__ == "__main__":
    uvicorn.run("main:app")