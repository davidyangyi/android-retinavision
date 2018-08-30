import matplotlib.pyplot as plt
import cv2
import datetime
import numpy as np
from retinavision.retina import Retina
from retinavision.cortex import Cortex
from retinavision import datadir, utils
import cPickle as pickle
from os.path import join

def retinaImg(retina,fixation,cortex,cv2img):
    starttime=datetime.datetime.now()
    img = cv2.cvtColor(cv2img, cv2.COLOR_BGR2RGB)
    V = retina.sample(img, fixation)
    samplingtime=datetime.datetime.now()
    samplingperiod=samplingtime-starttime
    samplingperiod.total_seconds()
    print("Sampling time" + str(samplingperiod)+"||")

    backprojectimg = retina.backproject_last()
    backtime = datetime.datetime.now()
    backperiod = backtime-samplingtime
    backperiod.total_seconds()
    print("Backprojected time" + str(backperiod) + "||")

    corteximg = cortex.cort_img(V)
    cortextime = datetime.datetime.now()
    cortexperiod = cortextime - samplingtime
    cortexperiod.total_seconds()
    print("Cortex time" + str(cortexperiod) + "||")

    return backprojectimg,corteximg,V

