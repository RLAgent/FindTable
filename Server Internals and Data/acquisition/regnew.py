'''
import SocketServer
import SimpleHTTPServer
import json
import re
import sys
import csv

classes = []
class Reply(SimpleHTTPServer.SimpleHTTPRequestHandler):
    def do_GET(self):
        # query arrives in self.path; return anything, e.g.,
        #self.wfile.write("query was %s\n" % self.path)
        global classes
        queries = self.path.lstrip('/').split('/')
        for classinfo in classes:
            printThis = True
            for strq in queries:
                if (printThis):
                    printThis = printThis and matchQuery(strq,classinfo)
            if printThis:
                strout = ''
                #dept coursenumber
                for listing in classinfo['listings']:
                    strout = strout + listing['dept'] + ' ' + listing['number'] + '/'
                
                strout = strout.rstrip('/') + ' '
                #distribution
                strout = strout + classinfo['area'] + ' '
                if (len(classinfo['classes'])>0):
                    #days
                    strout = strout + classinfo['classes'][0]['days'] + ' '
                #rest of it
                strout = strout + classinfo['descrip']
                #solution from https://piazza.com/class/hqhtb8trbbr60w?cid=439
                strout = strout.encode('utf-8')
                #(even something like strout.encode() would work)
                self.wfile.write(strout)

        
#format REsearch string, as per https://piazza.com/class/hqhtb8trbbr60w?cid=555 and 565
def getrestring(classinfo):
    strout = '' 
    if (len(classinfo['classes'])>0):
        classtime = classinfo['classes'][0]
        #time
        strout = strout + classtime['starttime'].split()[0] + '-'
        strout = strout + classtime['endtime'].split()[0] + ' '
    #title
    strout = strout + classinfo['title'] + ' '
    #profs
    for prof in classinfo['profs']:
        strout = strout + prof['name'] + '/'
    
    strout = strout.rstrip('/') + ' '
    if (len(classinfo['classes'])>0):
        #building
        strout = strout + classtime['bldg'] + ' '
        #roomnum
        strout = strout + classtime['roomnum'] + '\n'
    return strout

def matchQuery(query, classinfo):
    # check distribution codes
    matchstring = '(^la$)|(^sa$)|(^ha$)|(^em$)|(^ec$)|(^qr$)|(^stl$)|(^stn$)'
    v = re.match(matchstring,query.lower())
    if (v != None):
        if (classinfo['area'].lower() == query.lower()):
            return True

    #check days of the week
    matchstring = '(^m$)|(^t$)|(^w$)|(^th$)|(^f$)|(^mw$)|(^mwf$)|(^tth$)|(^mtwthf$)'
    v = re.match(matchstring,query.lower())
    if (v != None):
        for listing in classinfo['classes']:
            if (listing['days'].lower() == query.lower()):
                return True
    #check departmental code
    matchstring = '^[a-z][a-z][a-z]$'
    v = re.match(matchstring,query.lower())
    if (v != None):
        for listing in classinfo['listings']:
            if (listing['dept'].lower() == query.lower()):
                return True
        return False
    #check course number
    matchstring = '^\d\d\d$'
    v = re.match(matchstring,query.lower())
    if (v != None):
        for listing in classinfo['listings']:
            if (listing['number'].lower() == query.lower()):
                return True
        return False
    #check regular expressions
    if (len(query) > 3):
        if (re.search(query,classinfo['descrip'],re.IGNORECASE) != None):
            return True
        return False
    return False
    
# delete sections, as per https://piazza.com/class/hqhtb8trbbr60w?cid=437
def deletenonfirstsection():
    global classes
    for i in classes:
        # if multiple classes, delete classes
        while (len(i['classes']) > 1):
            i['classes'].pop(1)
            
#format REsearch string, as per https://piazza.com/class/hqhtb8trbbr60w?cid=555
def formatREsearch():
    global classes
    for i in classes:
        # format string, store into 'descrip' (so that we don't change JSON structure)
        i['descrip'] = getrestring(i)
        
def main():
    # read and eval reg.json
    global classes
    #classes = json.load(open("result50.json"))
    totals = 0
    buses = list()
    reload(sys)
    sys.setdefaultencoding('utf-8')
    for i in range(3400):
        classes = json.load(open("newresults/result" + str(i+1) + ".json"))
        bus = classes['businesses']
        buses = buses + bus;
        totals = totals + size(bus)
    dictionary = {"restaurants":buses,"total":totals}
    #f = open("newfinalrestaurants.json","w")
    #json.dump(dictionary,f)
    #f.close()
    f = open("restaurantinfo.json","w")
    json.dump(buses,f)
    f.close()
    #f = open("restaurantCSVs.csv","wb")
    #c = csv.writer(f)
    #c.writerow(buses[0].keys())
    #for bu in buses:
    #    c.writerow(bu.values())
    #f.close()
    # delete sections, as per https://piazza.com/class/hqhtb8trbbr60w?cid=437
    #deletenonfirstsection()
    #format REsearch string, as per https://piazza.com/class/hqhtb8trbbr60w?cid=555
    #formatREsearch()
    #if (len(sys.argv) > 1):
    #    port = sys.argv[1]
    #else:
    #    port = 8080
    #SocketServer.ForkingTCPServer(('', int(port)), Reply).serve_forever()
    '''

import json
import MySQLdb as mdb
import sys

def other_main():
    # read and eval reg.json
    global classes
    reload(sys)
    sys.setdefaultencoding('utf-8')
    try:
        con = mdb.connect('localhost', 'query', 'antequ', 'findatable');
    
        cur = con.cursor()
        #cur.execute("SELECT VERSION()")
        cur.execute("SELECT max( restaurant_id ) FROM `restaurant_data`");
        ver = cur.fetchone();
        v = str(ver);
        rid = int(v[1:v.find('L,')]);
        sys.stderr.write("Max RID: " + str(rid) + "\n");

        #sys.exit();
        #ver = cur.fetchone()
        #print "Database version: %s " % ver
        
        
        totalfilecount = 3400
        for i in range(totalfilecount):
            classes = json.load(open("newresults/result" + str(i+1) + ".json"))
            bus = classes['businesses']
            print "json " + str(i) + " loaded!"

            for restaurant in bus:
                rid += 1
                try:
                    restaurant.pop('gift_certificates')
                except KeyError, e:
                    pass
                
                try:
                    restaurant.pop('deals')
                except KeyError, e:
                    pass
                
                try:
                    location = restaurant.pop('location')
                    for k in location.keys():
                        restaurant['location_' + k] = location[k]
                except KeyError, e:
                    pass
                
                try:
                    categories = restaurant.pop('categories')
                    catstring = ''
                    for cat in categories:
                        if (len(cat) > 0):
                            catstring += cat[0] + ','
                    if (catstring):
                        catstring = catstring[:-1]
                    restaurant['categories'] = catstring
                    restaurant['type'] = catstring
                except KeyError, e:
                    pass
                for k in restaurant.keys():
                    if isinstance(restaurant[k],list):
                        catstring = ''
                        for cat in restaurant[k]:
                            catstring += str(cat) + ';'
                        if (catstring):
                            catstring = catstring[:-1]
                        restaurant[k] = catstring
                    else:
                        restaurant[k] = str(restaurant[k])
                
                restaurant['restaurant_id'] = str(rid)
                execstr = "INSERT INTO restaurant_data ("
                for key in restaurant.keys():
                    execstr += str(key) + ","
                    restaurant[key] = str(mdb.escape_string(restaurant[key]))
                
                execstr = execstr[:-1] + ") VALUES ("
                
                for key in restaurant.keys():
                    execstr += '"' + restaurant[key] + '",'
                
                execstr = execstr[:-1] + ") ON DUPLICATE KEY UPDATE "
                restaurant.pop('restaurant_id');
                for key in restaurant.keys():
                    execstr += str(key) + '="' + restaurant[key] + '",'
                
                execstr = execstr[:-1] #+ ";"
                #print execstr
                cur.execute(execstr)
                
                
                
            con.commit()
            print str(cur.rowcount) + ":" + str(cur.fetchone());
            sys.stderr.write("Loaded file " + str(i) + " of " + str(totalfilecount) + ". \n")
            
            
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])
        sys.exit(1)
        
    finally:    
        
        if con:    
            con.close()

    #totals = 0
    #buses = list()
    #reload(sys)
    #sys.setdefaultencoding('utf-8')
    #for i in range(3400):
    #    classes = json.load(open("newresults/result" + str(i+1) + ".json"))
    #    bus = classes['businesses']
    #    buses = buses + bus;
    #    totals = totals + size(bus)
    #dictionary = {"restaurants":buses,"total":totals}
    #f = open("newfinalrestaurants.json","w")
    #json.dump(dictionary,f)
    #f.close()
    #f = open("restaurantinfo.json","w")
    #json.dump(buses,f)
    #f.close()
other_main()