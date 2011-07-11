import sys


server = "10.4.2.214"
url = "/EWS/Exchange.asmx"
username = "test"
password = "Password1"

# non existense of blank space at the beginning is significant - wtf
getRoomListsXML = """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
               xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types"
               xmlns:m="http://schemas.microsoft.com/exchange/services/2006/messages">
  <soap:Header>
    <t:RequestServerVersion Version ="Exchange2010"/>
  </soap:Header>
  <soap:Body>
    <m:GetRoomLists />
  </soap:Body>
</soap:Envelope>
"""

if True:
	import httplib, urllib
	import base64

	base64string = base64.b64encode('%s:%s' % (username, password))
	print base64string

	conn = httplib.HTTPSConnection(server)
	conn.request("POST", "/EWS/Exchange.asmx", getRoomListsXML, {"Content-Type": "text/xml", "Authorization": "Basic %s" % base64string})
	resp = conn.getresponse()

	print resp.status, resp.reason
	print resp.getheaders()

	if resp.status != 200:
		raise Exception, resp.reason

	xmlresponse = resp.read()

	sys.exit(1)

class Element(object):
	pass

class Envelope(Element):
	def __init__(self):
		self.body = annotate(Body())

class Body(Element):
	pass

def annotate(element):
	return element


xmlresponse = """
<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"><s:Header><h:ServerVersionInfo MajorVersion="14" MinorVersion="0" MajorBuildNumber="722" MinorBuildNumber="0" Version="Exchange2010" xmlns:h="http://schemas.microsoft.com/exchange/services/2006/types" xmlns="http://schemas.microsoft.com/exchange/services/2006/types" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"/></s:Header><s:Body xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><GetRoomListsResponse ResponseClass="Success" xmlns="http://schemas.microsoft.com/exchange/services/2006/messages"><ResponseCode>NoError</ResponseCode><m:RoomLists xmlns:m="http://schemas.microsoft.com/exchange/services/2006/messages"><t:Address xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types"><t:Name>NonExistingRooms</t:Name><t:EmailAddress>nonExistingRooms@futu.com</t:EmailAddress><t:RoutingType>SMTP</t:RoutingType><t:MailboxType>PublicDL</t:MailboxType></t:Address></m:RoomLists></GetRoomListsResponse></s:Body></s:Envelope>
""".strip()

print xmlresponse

from xml.dom import minidom
from xml import dom

xml = minidom.parseString(xmlresponse)

addresses = xml.getElementsByTagName("t:EmailAddress")
for address in addresses:
	print address.firstChild.nodeValue

print xml.toprettyxml()
print xml.childNodes

def myparse(domNode, model):
	if domNode.nodeName != type(model).__name__:
		raise Exception, "parse error"

	for child in domNode.childNodes:
		if child.nodeType == child.ELEMENT_NODE:
			print "ELEMENT!"

		print child
	print dir(model)

	return model

# env = myparse(xml.firstChild, Envelope())
# print repr(env)

if False:
	import xml.etree.ElementTree
	etree = xml.etree.ElementTree.fromstring(xmlresponse)

	xml.etree.ElementTree.dump(etree)
	roomlists = list(etree.iter("Envelope"))

	print roomlists

print "----"

sys.exit(0)


import urllib2

try:
	req = urllib2.Request('https://' + server + url, getRoomListsXML)
	req.add_header('Content-Type', 'text/xml')

	pass_mgr = urllib2.HTTPPasswordMgrWithDefaultRealm()
	# this creates a password manager
	pass_mgr.add_password(None, '/', username, password)
	# because we have put None at the start it will always
	# use this username/password combination for  urls
	# for which `url` is a super-url

	auth_handler = urllib2.HTTPBasicAuthHandler(pass_mgr)
	# create the AuthHandler

	opener = urllib2.build_opener(auth_handler)

	# open the page
	pagehandle = opener.open(req)

	print page

except urllib2.HTTPError as e:
	print e
	sys.exit(1)

print r
