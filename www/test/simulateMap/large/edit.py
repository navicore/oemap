#!/usr/bin/python2

names = [line.strip().rstrip(' \xc2\xa0') for line in open('names.txt')]

for n in names:
    print n
