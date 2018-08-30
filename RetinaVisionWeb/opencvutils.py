import numpy as np
import cv2
from cv2 import cv

def readImgFromBytes(imgdata):
    nparr = np.fromstring(imgdata, np.uint8)
    img_np = cv2.imdecode(nparr, cv2.CV_LOAD_IMAGE_COLOR) # cv2.IMREAD_COLOR in OpenCV 3.1
    return img_np

def writeImgToBytes(cv2img):
    r = cv2.imencode('.jpg',cv2img)[1]
    return r.tostring()

if __name__=='__main__':
    fr = open('a.jpg','rb')
    data = fr.read()
    cv2.imshow('xx',readImgFromBytes(data))
    cv2.waitKey()