import urllib2
import json

# Get the JSON
req = urllib2.Request("http://bots.myrobots.com/channels/619/feed.json?status=true&location=true")
opener = urllib2.build_opener()
objs = json.load(opener.open(req))

# Gets only the most recent status
print objs['feeds'][-1]['status']
