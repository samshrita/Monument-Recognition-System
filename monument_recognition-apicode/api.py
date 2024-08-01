from fastapi import FastAPI, status, File, Form, UploadFile, Response
import databases
import sqlalchemy
from sqlalchemy import Table, MetaData
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from pydantic import BaseModel
import os
import urllib
import geopy.distance
from fastapi.responses import FileResponse
from pathlib import Path
from predict.predictor import *



# class InData(BaseModel):
#     latlng: str
#     origimg: bytes

# class OutData(BaseModel):
#     predname: str
#     predconf : float
#     infTime : str
#     maskimg: bytes
   
class Item(BaseModel):
    name: str
    # description: Union[str, None] = None
    # price: float
    # tax: Union[float, None] = None


class Category(BaseModel):
    category: str
    latilongi: str





host_server = os.environ.get('host_server', '')
db_server_port = urllib.parse.quote_plus(str(os.environ.get('db_server_port', '5432')))
database_name = os.environ.get('database_name', 'mlrecog-db')
db_username = urllib.parse.quote_plus(str(os.environ.get('db_username', '')))
db_password = urllib.parse.quote_plus(str(os.environ.get('db_password', '')))
ssl_mode = urllib.parse.quote_plus(str(os.environ.get('ssl_mode','prefer')))
DATABASE_URL = 'postgresql://{}:{}@{}:{}/{}?sslmode={}'.format(db_username, db_password, host_server, db_server_port, database_name, ssl_mode)

database = databases.Database(DATABASE_URL)

def strtopoint(initcoord):
    initcoord = initcoord.replace(' ','')
    initcoord = initcoord[1:]
    initcoord = initcoord[:-1]
    initcoord = initcoord.split(',')
    intopoint = geopy.Point(float(initcoord[0]), float(initcoord[1]))
    return intopoint


def calculate_distance(initcoord):
    validclass = []
    for i, itrcoord in enumerate(monumentcolumnlist):
        if(geopy.distance.geodesic(initcoord, strtopoint(itrcoord[8])).km * 1000 < 200):
            validclass.append(((monumentcolumnlist[i])[9]))
    return validclass









engine = sqlalchemy.create_engine(
    #DATABASE_URL, connect_args={"check_same_thread": False}
    DATABASE_URL, pool_size=3, max_overflow=0
)

metadata = sqlalchemy.MetaData(bind= engine)
sqlalchemy.MetaData.reflect(metadata)

inData = metadata.tables['receiveddata']
monumentData = metadata.tables['monument-data']
nearbyservices = metadata.tables['nearbyservices']

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)
app.add_middleware(GZipMiddleware)

@app.on_event("startup")
async def startup():
    await database.connect()
    query = monumentData.select()
    query2 = nearbyservices.select()
    global coords, bustoplist, monumentcolumnlist, restroomlist, hotellist, exchangelist, atmlist, nearbyserviceslisttemp, lessthandistance
    lessthandistance = 500
    bustoplist = []
    monumentcolumnlist = []
    restroomlist = []
    hotellist = []
    exchangelist = []
    atmlist = []



  

    coords = engine.execute(query)
    nearbyserviceslisttemp = engine.execute(query2)

    for row in coords:
        monumentcolumnlist.append([row[2],row[3],row[4],row[5],row[6],row[7],row[8],row[9],row[1],row[0]])

    for row in nearbyserviceslisttemp:
        if row[3] == "bustop":
            bustoplist.append([row[0], row[1], row[2]]) 
        if row[3] == "restroom":
            restroomlist.append([row[0], row[1], row[2]]) 
        if row[3] == "atm":
            atmlist.append([row[0], row[1], row[2]]) 
        if row[3] == "exchange":
            exchangelist.append([row[0], row[1], row[2]]) 
        if row[3] == "hotel":
            hotellist.append([row[0], row[1], row[2]]) 
    
 
    init()


@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()



async def hitDb(tablename, ltlg, origimg):
    query = tablename.insert().values(latlng=ltlg, origimg= origimg)
    last_record_id = await database.execute(query)



@app.post("/predict",  response_class=Response, status_code = status.HTTP_201_CREATED)
async def create_predict_data(  ltlg : str = Form(),file: UploadFile = File()):
    readfile = file.file.read()
    classlist = calculate_distance(initcoord= strtopoint(ltlg))
    result = run(readfile, classlist)
    await hitDb(inData, ltlg = ltlg, origimg = readfile)

    if result != 0 :
        classes, confd, inftime, maskbytes = result
        for tempp in monumentcolumnlist:
            if tempp[0] == classes:
                return Response( headers = { "predname" : tempp[1], "predconf": str(confd),
                                "infTime": inftime }, content = maskbytes, media_type="image/png")
        
    else:
        return{
            "predname" : '',
            "predconf" : 0.0,
            "infTime" : '',
            "maskimg" : ''

        }



@app.post("/details", status_code = status.HTTP_201_CREATED)
async def req_detail(  item : Item):

    for temp in monumentcolumnlist:
        if temp[1] == item.name:
              return {
            "title": temp[1],
            "age": "Around " + str(temp[2]) +" Years",
            "location": temp[3],
            "description": temp[4],
            "image_name": [temp[5],temp[6],temp[7]]


            }
        


@app.post("/nearbyplaces", status_code = status.HTTP_201_CREATED)
async def req_detail(category : Category):

    if category.category == "monument":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in monumentcolumnlist:
                if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[8]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[1])
                    latlnglist.append(itemm[8])
                    addresslist.append(itemm[3])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }
    
    if category.category == "exchange":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in exchangelist:
                 if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[1]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[0])
                    latlnglist.append(itemm[1])
                    addresslist.append(itemm[2])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }

    if category.category == "hotel":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in hotellist:
                if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[1]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[0])
                    latlnglist.append(itemm[1])
                    addresslist.append(itemm[2])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }

    if category.category == "bustop":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in bustoplist:
                if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[1]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[0])
                    latlnglist.append(itemm[1])
                    addresslist.append(itemm[2])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }
    
    if category.category == "restroom":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in restroomlist:
                if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[1]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[0])
                    latlnglist.append(itemm[1])
                    addresslist.append(itemm[2])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }
    
    if category.category == "atm":
            namelist = []
            latlnglist = []
            addresslist = []
            

            for itemm in atmlist:
                if(geopy.distance.geodesic(strtopoint(category.latilongi), strtopoint(itemm[1]) ).km * 1000 < lessthandistance):
                    namelist.append(itemm[0])
                    latlnglist.append(itemm[1])
                    addresslist.append(itemm[2])

                 
            return {
                    "name": namelist,
                    "latlng": latlnglist ,
                    "address": addresslist,

                    }
       


  

@app.get("/images/{imagepath_name}")
async def read_image(imagepath_name: str):
    image_path = "static/images/" + imagepath_name.replace("-","/")
    if Path(image_path).exists():
        return FileResponse(str(image_path))
    else:
        return {"message": "Image not found"}

   
    






      
    
    
