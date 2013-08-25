#!/usr/bin/python2

names = [line.strip().rstrip(' \xc2\xa0') for line in open('names_seed.txt')]

firstNames = []
lastNames = []
newNames = []

middleNames = ["Ken", "Mori", "Alex", "Shepard", "Pie", "Money"]
newNamesWithMiddle = []

for n in names:
    (first, last) = n.split(' ')
    firstNames.append(first)
    lastNames.append(last)

for f in firstNames:
    for l in lastNames:
        newNames.append("%s %s" % (f, l))

for l in lastNames:
    for f in firstNames:
        newNames.append("%s %s" % (l, f))

for idx, val in enumerate(newNames):
    (f, l) = val.split(' ')
    for m in middleNames:
        newNamesWithMiddle.append("%s %s %s" % (f, m, l))
        #print "%s %s %s" % (f, m, l)

for n in list(set(newNames)):
    print n

for n in list(set(newNamesWithMiddle)):
    print n
