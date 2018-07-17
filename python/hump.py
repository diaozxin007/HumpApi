# -*- coding:utf-8 -*-
import httplib,urllib,json

url = 'xilidou.com'


def query(q,status=0):
    response = get(q,status)
    dates = json.loads(response.read())
    items = list()
    for date in dates:
        item = {}
        item['title'] = beautify(date.encode('utf-8'))
        item['arg'] = beautify(date.encode('utf-8'))
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


def beautify(ss=None):
    ss = ss.replace('__', '_')
    ss = ss.replace('--', '-')

    if '_' in ss or '-' in ss:
        ss = ss.lower()

    if ss.startswith('the_'):
        ss = ss.replace('the_', '', 1)
    if ss.startswith('a_'):
        ss = ss.replace('a_', '', 1)

    if ss.startswith('the-'):
        ss = ss.replace('the-', '', 1)
    if ss.startswith('a-'):
        ss = ss.replace('a-', '', 1)

    if ss.startswith('the'):
        if ss[3].lower != ss[3]:
            ss = ss.replace('the', '', 1)
            ss = ss[0].lower() + ss[1:]
    if ss.startswith('a'):
        if ss[1].lower != ss[1]:
            ss = ss.replace('a', '', 1)
            ss = ss[0].lower() + ss[1:]

    return ss


def getIcon():
    icon = {}
    icon['path'] = 'icon.png'
    return icon


if __name__ == '__main__':
    query('中文')
