# coding=UTF-8
import json
import os
import uuid
import opencvutils
import retinautils
import cv2
import base64
import datetime
import tornado.web
import tornado.ioloop
from retinavision.retina import Retina
from retinavision.cortex import Cortex
from retinavision import datadir
from os.path import join

#Create and load retina
R = Retina()
R.info()
R.loadLoc(join(datadir, "retinas", "ret50k_loc.pkl"))
R.loadCoeff(join(datadir, "retinas", "ret50k_coeff.pkl"))

# #Prepare retina
# x = campic.shape[1]/2
# y = campic.shape[0]/2
# fixation = (y,x)
# R.prepare(campic.shape, fixation)

#Create and prepare cortex
C = Cortex()
lp = join(datadir, "cortices", "50k_Lloc_tight.pkl")
rp = join(datadir, "cortices", "50k_Rloc_tight.pkl")
C.loadLocs(lp, rp)
C.loadCoeffs(join(datadir, "cortices", "50k_Lcoeff_tight.pkl"), join(datadir, "cortices", "50k_Rcoeff_tight.pkl"))


class RetinaHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("xxx!")
    def post(self):
        ret = {'result': 'OK'}
        upload_path =  'files'  # 文件的暂存路径
        file_metas = self.request.files.get('file', None)  # 提取表单中‘name’为‘file’的文件元数据

        if not file_metas:
            ret['result'] = 'Invalid Args'
            return ret

        for meta in file_metas:

            # filename = uuid.uuid1()
            # file_path = os.path.join(upload_path,filename)
            cvimg = opencvutils.readImgFromBytes(meta['body'])
            cvimg = cv2.resize(cvimg,(1024,768))
            begin = datetime.datetime.now()
            print("begin" + str(begin))
            x = cvimg.shape[1]/2
            y = cvimg.shape[0]/2
            fixation = (y,x)
            R.prepare(cvimg.shape, fixation)
            pre = datetime.datetime.now()
            pp=pre-begin
            pp.total_seconds()
            print("prepare" + str(pp))
            backgroundimg,corteximg,V = retinautils.retinaImg(R,fixation,C,cvimg)
            end = datetime.datetime.now()
            print("end" + str(end))
            cc=end-begin
            cc.total_seconds()
            print("time"+str(cc))
            cortimg = cv2.cvtColor(corteximg, cv2.COLOR_RGB2BGR)
            backimg = cv2.cvtColor(backgroundimg, cv2.COLOR_RGB2BGR)
            cimgbytes = opencvutils.writeImgToBytes(cortimg)
            bimgbytes = opencvutils.writeImgToBytes(backimg)
            with open('cimgout.jpg','wb') as fr:
                fr.write(cimgbytes)
            cimgbase64str = base64.b64encode(cimgbytes)
            bimgbase64str = base64.b64encode(bimgbytes)
            vbase64str = ",".join([str(i) for i in list(V)])

            result = {
                "bimg":bimgbase64str,
                "cimg":cimgbase64str,
                "V":vbase64str,
                "fix_x": float(x),
                "fix_y": float(y),
            }

            self.write(json.dumps(result))
            return



