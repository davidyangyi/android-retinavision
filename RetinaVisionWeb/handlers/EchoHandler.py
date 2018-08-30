# coding=UTF-8

import tornado.web
import tornado.ioloop



class EchoHandler(tornado.web.RequestHandler):
    def get(self):
        str = self.get_argument('str')
        self.write(str)

    def post(self):
        str = self.get_argument('str')
        self.write(str)

