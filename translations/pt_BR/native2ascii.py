#!/usr/bin/python3

import os

rootdir = r'/Users/rgb/Projects/Zettelkasten/src/main/resources/de/danielluedecke/zettelkasten/'

for subdir, dirs, files in os.walk(rootdir):
    for file in files:
        filepath = subdir + os.sep + file

        if filepath.endswith("_pt_BR.properties"):
            print('{}'.format(repr(filepath)))
            os.system("native2ascii -encoding 8859_1 " + filepath + " " + filepath)
