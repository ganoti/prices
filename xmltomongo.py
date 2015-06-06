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

#Server IP: 130.211.170.156
mongo_con = mng.MongoClient(host = 'localhost',
                            port = 27017)

db=mongo_con.eifozol
collection=db.test


print os.getcwd()
rootdir = "./xml/daily"

for subdir, dirs, files in os.walk(rootdir):
#    print subdir
#    print dirs
    for xml_file in files:
        if xml_file.endswith('.xml'):
            names = subdir.split('\\')
            date = 'date{}'.format(names[1])
            collection s= db[date]
            chain = names[2]
            branch = names[3]
            content_type = xml_file[0:5]
            xml_path = "{}\\{}".format(subdir,xml_file)
            with open (xml_path) as f:
                prices_dict = xtd.parse(f)
            mongo_dict = dict(date = date,
                              chain = chain,
                              branch = branch,
                              content_type = content_type,
                              content = prices_dict)
            collection.insert(mongo_dict)
            print ('file OK')

