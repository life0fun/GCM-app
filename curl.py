#!/usr/bin/env python

"""
  this file provide an simple command line post request equivalent to curl.
  command line curl is hard for post request because of the quotes and nest json.
  All command line should be replaced by executable python script.

  The format of json and the keyword must be exact. channels take a json array as channels. channel takes one string name.

"""

import sys
import urllib
import urllib2
import cookielib  # create cookie jar to store cookies, and use cookie_handle.
import json


''' 
no diff to set encode('utf-8'),
'''
def createDataDict(msg):
    ''' the format of json and keyword must be exact. channels take json array, channel take one string name '''
    dat = {"channel": "", "type":"android", "expiration_interval":86400, "data": {"action":"com.colorcloud.gcm.locationchange","msg": msg}}
    #jsondata = urllib.urlencode(dat)  # this creates key=val&k2=v2 http post
    #return json.dumps(dat).encode('utf-8')
    return json.dumps(dat)

''' use urllib2 Request and urlopen API to post data to url '''
def postToParser(msg):
    url = 'https://api.parse.com/1/push'
    jsondata = createDataDict(msg)
    print 'posting to :', url, ' data: ', jsondata

    headers = {}
    headers['Content-Type'] = 'application/json'
    headers['X-Parse-Application-Id'] = '2qF1TwYOVeCRCQn0UclQKlknNJWvrCk3E8JUNIFm'
    headers['X-Parse-REST-API-Key'] = 'm7sQeLD0SEuMf5TGazKevUQJ0H4UGTfGR6ZEmrtp'

    req = urllib2.Request(url, jsondata, headers)
    f = urllib2.urlopen(req)
    res = f.read()
    print 'response : ', res
    f.close()

def post(url, jdat):
    print 'posting to ', url, jdat
    jsondata = json.dumps()
    urllib2.urlopen(url, jdat)

'''
We use cookielib to create cookie jar store multiple cookies. 
build_opener() is used to combine the http handler with the cookies handler. 
Using the opener object, simple open to that object to get a file object with the response of the server.
'''
def handleHttpAndCookie(url, data, headers):
    request = urllib2.Request(url, data, headers)
     
    cookies = cookielib.CookieJar()
    cookies.extract_cookies(response,request)
              
    cookie_handler= urllib2.HTTPCookieProcessor(cookies)
    redirect_handler= urllib2.HTTPRedirectHandler()
    opener = urllib2.build_opener(redirect_handler,cookie_handler)
                           
    response = opener.open(request)

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print 'Usage: ./curl.py url data'

    #post(sys.argv[2], sys.argv[3])
    postToParser(sys.argv[1])
