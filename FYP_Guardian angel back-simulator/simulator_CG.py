import os
import sys
import random
import heapq
import time
import threading
import sqlite3
import json
from PIL import Image, ImageDraw
from queue import Queue
from random import randint



stations = []
width, height = 0, 0

def readFloorplan():
        conn = sqlite3.connect('Caregivers.sqlite')
        cur = conn.cursor()
        cur.executescript('''
        DROP TABLE IF EXISTS Station;
        CREATE TABLE Station (
            id     INTEGER NOT NULL PRIMARY KEY UNIQUE,
            x      INTEGER,
            y      INTEGER
        );
        ''')
        

        
        global width, height
        floorplanRGB = Image.open("floorplan.png")
        width, height = floorplanRGB.size
        floorplanRGB = floorplanRGB.load()
        floorplan = [[None for i in range(height)] for j in range(width)]
        for row in range(height):
                for col in range(width):
                        if floorplanRGB[col,row][0:3] == (255,255,255): #white     
                                floorplan[col][row] = 1
                        elif floorplanRGB[col,row][0:3] == (0,0,0): #black
                                floorplan[col][row] = 0
                        elif floorplanRGB[col,row][0:3] == (237,28,36):
                                floorplan[col][row] = 3
                        else:   #green
                                floorplan[col][row] = 2
                                
        del floorplanRGB
        for col in range(width):
                for row in range(height):
                        if floorplan[col][row] == 3:
                                print(col,row)
                                r = row
                                while r<height:
                                        if floorplan[col][r] != 3: break
                                        r += 1
                                r -= 1
                                c = col
                                while c<width:
                                        if floorplan[c][row] != 3: break
                                        c += 1
                                c -= 1
                                midR = int((row+r)/2)
                                midC = int((col+c)/2)
                                for x in range(col, c+1):
                                        for y in range(row, r+1):
                                                if x!=midC or y!= midR: floorplan[x][y] = 1

        global stations
        count = 1
        for col in range(width):
                for row in range(height):
                        if floorplan[col][row]==3: 
                                stations.append((col,row))
                                cur.execute('''INSERT OR IGNORE INTO Station (id, x, y) 
                                                                   VALUES ( ?, ?, ? )''', ( count, col, row) )
                                conn.commit()
                                count += 1
        conn.close()
        return floorplan
    
#Caregiver object
class Caregiver(object):
        def __init__(self, id, x, y):
                self.id = id
                self.x = x
                self.y = y
                self.mode = "standing"
                self.target = None
                self.targetPosition = None
                self.path = None
                self.stationTime = 100 
                self.targetStation = None
                self.lastUpdateX = 0
                self.lastUpdateY = 0
                #the time interval between the current time and the last time the agent went toilet

def CGmovement(q_CG, floorplan, stations, caregivers, caregiver, id):
        global height, width, speed, numberOfCGAgents

        def distance(x,y, targetX,targetY):
                return ((x-targetX)**2 + (y- targetY)**2)**0.5

        def aStarPath(targetX, targetY):
                openStack = []
                currentState = (0, 0,(caregiver.x, caregiver.y), None)
                visited = set()
                visited.add((caregiver.x, caregiver.y))
                #format of state: (heuristic, distance from starting point, (x, y), preivous)
                heapq.heappush(openStack, currentState)
                while len(openStack)>0:
                        state = heapq.heappop(openStack)
                        x = state[2][0]
                        y = state[2][1]
                        if x == targetX and y == targetY: break
                        dist = state[1]
                        for i in (-1,0,1):
                                for j in (-1,0,1):
                                        if i==0 and j==0: continue
                                        if x+i >= width or x+i<0 or y+j>=height or y+j<0:continue
                                        if floorplan[x+i][y+j] ==0 : continue
                                        if (x+i, y+j) in visited: continue
                                        newDistance = dist + distance(x,y,x+i,y+j)
                                        newHeuristic = newDistance + distance(x+i, y+j, targetX, targetY)
                                        newState = (newHeuristic, newDistance, (x+i, y+j), state)
                                        visited.add((x+i, y+j))
                                        heapq.heappush(openStack, newState)
                path = []
                while state is not None:
                        x = state[2][0]
                        y = state[2][1]
                        path.append((x,y))
                        state = state[3]
                path.reverse()
                #im = Image.open("floorplan.png")
                #draw = ImageDraw.Draw(im)
                #for i in range(len(path)-1):
                        #last = path[i]
                        #current = path[i+1]
                        #draw.line((last[0], last[1], current[0], current[1]), width=5, fill=128)
                #im.save("floorplan_path_"+str(randint(0,10000))+".png")
                #del draw
                return path


                                

        def aStarPathDistance(targetX, targetY):
                openStack = []
                currentState = (0, 0,(caregiver.x, caregiver.y), None) 
                visited = set()
                visited.add((caregiver.x, caregiver.y))
                #format of state: (heuristic, distance from starting point, (x, y), preivous)
                heapq.heappush(openStack, currentState) 
                while len(openStack)>0:
                        state = heapq.heappop(openStack)
                        x = state[2][0]
                        y = state[2][1]
                        if x == targetX and y == targetY: return state[1]
                        dist = state[1]
                        for i in (-1,0,1):
                                for j in (-1,0,1):
                                        if i==0 and j==0: continue
                                        if x+i >= width or x+i<0 or y+j>=height or y+j<0:continue
                                        if floorplan[x+i][y+j] != 1: continue
                                        if (x+i, y+j) in visited: continue
                                        newDistance = dist + distance(x,y,x+i,y+j)
                                        newHeuristic = newDistance + distance(x+i, y+j, targetX, targetY)
                                        newState = (newHeuristic, newDistance, (x+i, y+j), state)
                                        visited.add((x+i, y+j))
                                        heapq.heappush(openStack, newState)


        def patrol():
                if caregiver.mode != "patrolling":
                        caregiver.mode = "patrolling"
                        x = random.randint(0, width-1)
                        y = random.randint(0, height-1)
                        while floorplan[x][y] != 1 or (x== caregiver.x and y == caregiver.y):
                                x = random.randint(0, width-1)
                                y = random.randint(0, height-1)
                        caregiver.targetPosition = (x,y)
                        caregiver.path = aStarPath(x,y)
                        print("Id: ", caregiver.id, "decided to go to ", caregiver.targetPosition)
                nextPosition = caregiver.path.pop(0)
                walkingTime = distance(caregiver.x , caregiver.y, nextPosition[0], nextPosition[1])/speed
                time.sleep(walkingTime)
                caregiver.stationTime += walkingTime
                caregiver.x = nextPosition[0]
                caregiver.y = nextPosition[1]
                if distance(caregiver.x, caregiver.y, caregiver.lastUpdateX, caregiver.lastUpdateY)>5:
                        q_CG.put(("UPDATE CGLocation  SET x = %s, y = %s, status = 'patrolling' WHERE id = %s;" % ( caregiver.x, caregiver.y, caregiver.id)))
                        caregiver.lastUpdateX = caregiver.x
                        caregiver.lastUpdateY = caregiver.y
                if (caregiver.x, caregiver.y) == caregiver.targetPosition:
                        print("Id: ", caregiver.id, "arrived at ", caregiver.targetPosition)
                        caregiver.mode = "standing"
                        q_CG.put(("UPDATE CGLocation  SET status = 'standing' WHERE id = %s;" % ( caregiver.id)))
                        caregiver.targetPosition = None
                        caregiver.path = None

        
        def goStation():
                if caregiver.mode != "going back to station":
                        q_CG.put(("UPDATE CGLocation  SET  status = 'going back to station' WHERE id = %s;" % ( caregiver.id)))
                        caregiver.mode = "going back to station"
                        dist = None
                        for station in stations:
                                tempDist = distance(caregiver.x, caregiver.y, station[0], station[1])
                                if dist is None or tempDist < dist:
                                        dist = tempDist
                                        caregiver.targetStation = station
                        print("Id: ", caregiver.id, " going back to station at ", caregiver.targetStation)
                        caregiver.path = aStarPath(caregiver.targetStation[0], caregiver.targetStation[1])
                        print("Length of list before: ", len(caregiver.path)) 
                        caregiver.path.pop(0) #not sure what this pop is for
                        print("Length of list after: ", len(caregiver.path)) 
                        
                if caregiver.path[0] == caregiver.targetStation:
                        while True:
                                nobodyInside = True
                                for CGagent in caregivers:
                                        if CGagent.x == caregiver.targetStation[0] and CGagent.y == caregiver.targetStation[1]:
                                                nobodyInside = False
                                                break
                                if nobodyInside:
                                        print("Id: ", caregiver.id, "is in the station at ", caregiver.targetStation)
                                        caregiver.x = caregiver.targetStation[0]
                                        caregiver.y = caregiver.targetStation[1]
                                        caregiver.mode = "at station"
                                        q_CG.put(("UPDATE CGLocation  SET  status = 'at station' WHERE id = %s;" % ( caregiver.id)))
                                        time.sleep(random.random()*5)
                                        caregiver.station = 0
                                        caregiver.mode = "patrolling"
                                        q_CG.put(("UPDATE CGLocation  SET status = 'patrolling' WHERE id = %s;" % (caregiver.id)))
                                        x = random.randint(0, width-1)
                                        y = random.randint(0, height-1)
                                        while floorplan[x][y] != 1 or (x== caregiver.x and y == caregiver.y):
                                                x = random.randint(0, width-1)
                                                y = random.randint(0, height-1)
                                        caregiver.targetPosition = (x,y)
                                        caregiver.path = aStarPath(x,y)
                                        return
                nextPosition = caregiver.path.pop(0)
                walkingTime = distance(caregiver.x , caregiver.y, nextPosition[0], nextPosition[1])/speed
                time.sleep(walkingTime)
                caregiver.x = nextPosition[0]
                caregiver.y = nextPosition[1]
                if distance(caregiver.x, caregiver.y, caregiver.lastUpdateX, caregiver.lastUpdateY)>5:
                        q_CG.put(("UPDATE CGLocation  SET x = %s, y = %s WHERE id = %s;" % ( caregiver.x, caregiver.y, caregiver.id)))
                        caregiver.lastUpdateX = caregiver.x
                        caregiver.lastUpdateY = caregiver.y
                return

        while True:
                        
                if caregiver.mode == "standing":
                        if caregiver.stationTime> 100:
                                rand = random.random()
                                if rand < 2.718**(-(100/caregiver.stationTime)):
                                        goStation()
                                        continue
                        rand = random.random()
                        if rand < 0.6: 
                                waitTime = 10 * random.random()
                                caregiver.stationTime += waitTime
                                time.sleep(waitTime) #continue to stand
                        else:
                                patrol()

                if caregiver.mode == "patrolling":
                        patrol()

                if caregiver.mode == "going back to station":
                        goStation()

numberOfCGAgents = 12
speed = 50 #pixel per second
floorplan = readFloorplan()


#initialize the CG database
conn = sqlite3.connect('Caregivers.sqlite')
cur = conn.cursor()
cur.executescript('''
DROP TABLE IF EXISTS CGLocation;
CREATE TABLE CGLocation (
    id     INTEGER NOT NULL PRIMARY KEY UNIQUE,
    x      INTEGER,
    y      INTEGER,
    status TEXT
);
''')


caregivers = []
for i in range(numberOfCGAgents):
        x = random.randint(0, width-1)
        y = random.randint(0, height-1)
        while floorplan[x][y] != 1:
                x = random.randint(0, width-1)
                y = random.randint(0, height-1)
        caregivers.append(Caregiver(i, x, y))
q_CG = Queue()
for id, caregiver in enumerate(caregivers):
        newThread2 = threading.Thread(target = CGmovement, args = (q_CG, floorplan, stations,caregivers, caregiver, i,))
        newThread2.start()
        cur.execute('''INSERT OR IGNORE INTO CGLocation (id, x, y, status) 

                                   VALUES ( ?, ?, ?, ? )''', ( id, caregiver.x, caregiver.y, "standing") )
conn.commit()






while True:
        if q_CG.qsize() == 0:
                time.sleep(0.1)
        else:
                # print(q.qsize())
                command = q_CG.get()
                cur.execute(command)
                conn.commit()
