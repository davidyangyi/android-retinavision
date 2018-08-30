# coding=UTF-8
import json
import os
import tornado.web
import tornado.ioloop

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("Retina Service is Running!")
