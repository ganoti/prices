# -*- coding: utf-8 -*-
"""============================================================================
Name: 


@author: Dean Langsam
@Created: Fri Jun 05 12:33:09 2015
============================================================================"""

import xmltodict as xtd
import pymongo as mng
import os

def dict_to_db():
    pass
mongo_con = mng.MongoClient(host = 'localhost',
                            port = 27017)

db=mongo_con.eifozol
collection=db.test


print os.getcwd()
with open ("./raw/daily/20150603/osher/010/PricesFull7290103152017-010-201506030800.xml",'r') as prices_file:
    prices_dict = xtd.parse(prices_file)

date = '20150603'
chain = 'osher'
branch = '010'
mongo_dict = dict(date = date,
                  chain = chain,
                  branch = branch,
                  content = prices_dict['Root'])

collection.insert(mongo_dict)
print mongo_dict['content']['Items']['Item'][0]['ManufacturerItemDescription']#.encode('utf8')
mongo_dict['content']['Items']['Item'][0]['ManufacturerItemDescription'] = mongo_dict['content']['Items']['Item'][0]['ManufacturerItemDescription'].encode('utf8')
print mongo_dict['content']['Items']['Item'][0]
