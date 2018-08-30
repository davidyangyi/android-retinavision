# coding=UTF-8
import json
import os
import tornado.web
import tornado.ioloop
import handlers

application = tornado.web.Application([
    (r"/", handlers.MainHandler),
    (r"/retina", handlers.RetinaHandler),
    (r"/echo", handlers.EchoHandler),

])

if __name__ == "__main__":
    application.listen(8888)
    tornado.ioloop.IOLoop.instance().start()
