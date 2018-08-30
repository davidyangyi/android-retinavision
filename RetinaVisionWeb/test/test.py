import base64
import requests
import json
import datetime
from PIL import Image

if __name__=='__main__':

    a=b''
    a="sdf"


    aa = base64.b64encode("abcdef")
    bb = base64.b64decode('YWJjZGVm')

    img = Image.open('c.jpg')
    print img.size
    # img = img.resize((1024,800))
    img = img.resize((max(img.size[0],img.size[1]),min(img.size[0],img.size[1])))
    print img.size

    img.save('a.jpg')
    url = "http://localhost:8888/retina"
    files = {'file': open('a.jpg','rb')}
    values = {'DB': 'photcat', 'OUT': 'csv', 'SHORT': 'short'}
    begin = datetime.datetime.now()
    r = requests.post(url, files=files, data=values)
    result = json.loads(r.content)
    end = datetime.datetime.now()
    k = end - begin
    k.total_seconds()
    print("Time" + k)
    bimg = base64.b64decode(result['bimg'])
    cimg = base64.b64decode(result['cimg'])
    fix_x=result["fix_x"]
    fix_y=result["fix_y"]
    V = result["V"]

    with open('bimg.jpg','wb') as fr:
        fr.write(bimg)
    with open('cimg.jpg','wb') as fr:
        fr.write(cimg)
