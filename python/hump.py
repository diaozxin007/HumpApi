# -*- coding:utf-8 -*-
import httplib,urllib,json

url = 'xilidou.com'


def query(q,status=0):
    response = get(q,status)
    dates = json.loads(response.read())
    items = list()
    for date in dates:
        item = {}
        item['title'] = date.encode('utf-8')
        item['arg'] = date.encode('utf-8')
        item['subtitle'] = '回车复制'
        item['icon'] = getIcon()
        items.append(item)
    jsonBean = {}
    jsonBean['items'] = items
    json_str = json.dumps(jsonBean)
    if json_str:
        print json_str
    return str


def get(q,status=0):
    parameters= dict()
    parameters['q'] = q
    parameters['status'] = status

    parameters = urllib.urlencode(parameters)
    headers = {"Content-type": "application/x-www-form-urlencoded"}

    conn = httplib.HTTPSConnection(url)
    conn.request('POST','/api/hump',parameters,headers)
    response = conn.getresponse()
    return response

def getIcon():
    icon = {}
    icon['path'] = 'icon.png'
    return icon


if __name__ == '__main__':
    query('中文')