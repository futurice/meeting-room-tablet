xmlresponse = """
<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
<s:Header><h:ServerVersionInfo MajorVersion="14" MinorVersion="0" MajorBuildNumber="722" MinorBuildNumber="0" Version="Exchange2010" xmlns:h="http://schemas.microsoft.com/exchange/services/2006/types" xmlns="http://schemas.microsoft.com/exchange/services/2006/types" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"/></s:Header>
<s:Body xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
	<GetRoomListsResponse ResponseClass="Success" xmlns="http://schemas.microsoft.com/exchange/services/2006/messages">
		<ResponseCode>NoError</ResponseCode>
		<m:RoomLists xmlns:m="http://schemas.microsoft.com/exchange/services/2006/messages">
			<t:Address xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types">
				<t:Name>NonExistingRooms</t:Name>
				<t:EmailAddress>nonExistingRooms@futu.com</t:EmailAddress>
				<t:RoutingType>SMTP</t:RoutingType>
				<t:MailboxType>PublicDL</t:MailboxType>
			</t:Address>
			<t:Address xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types">
				<t:Name>NonExistingRooms2</t:Name>
				<t:EmailAddress>nonExistingRooms2@futu.com</t:EmailAddress>
				<t:RoutingType>SMTP</t:RoutingType>
				<t:MailboxType>PublicDL</t:MailboxType>
			</t:Address>
		</m:RoomLists>
	</GetRoomListsResponse>
</s:Body>
</s:Envelope>
""".strip()

import xml.sax
import xml.sax.handler

class XmlToPyHandler(xml.sax.handler.ContentHandler):
	def __init__(self):
		self.stack = []
		self.current = None
		self.obj = {}

	def startElement(self, name, attr):
		if self.current is None:
			self.current = self.obj
			return

		obj = {}

		# without this 1.
		if name not in self.current:
			self.current[name] = obj
		elif type(self.current[name]) == dict:
			self.current[name] = [self.current[name], obj]
		else:
			self.current[name].append(obj)

		self.stack.append(self.current)
		self.current = obj

	def endElement(self, name):
		if self.stack == []:
			return

		self.current = self.stack.pop()

	def characters(self, content):
		# whitespace we dont care about
		if content.strip() == "":
			return

		self.current[u"value"] = content

class Element(object):
	def __init__(self, **kargs):
		self.namespace = None
		if "namespace" in kargs:
			self.namespace = kargs["namespace"]
			del kargs["namespace"]

		self.children = {}
		for k, v in kargs.iteritems():
			v.name = k
			self.children[v.tagName(k)] = v

	def tagName(self, tagName):
		if self.namespace is not None:
			return self.namespace + ":" + tagName
		else:
			return tagName


class ElementList(Element):
	def __init__(self, **kargs):
		super(ElementList, self).__init__(**kargs)

class XmlToPyHandlerSchema(xml.sax.handler.ContentHandler):
	def __init__(self, skeleton):
		self.skeleton = skeleton
		self.obj = {}

		self.stack = []

		self.current_obj = None
		self.current_keleton = None

	def startElement(self, name, attr):
		if self.current_obj is None:
			self.current_obj = self.obj
			self.current_skeleton = self.skeleton
			return

		# skip unknown
		if self.current_skeleton is None or name not in self.current_skeleton.children:
			self.stack.append((self.current_obj, self.current_skeleton))
			self.current_obj = {}
			self.current_skeleton = None
			return

		obj = {}
		skeleton = self.current_skeleton.children[name]

		if type(skeleton) == Element:
			obj = {}
		elif type(skeleton) == ElementList:
			obj = []
		else:
			raise Exception, "unknown skeleton", skeleton

		if type(self.current_obj) == list:
			self.current_obj.append(obj)
		else:
			self.current_obj[skeleton.name] = obj

		self.stack.append((self.current_obj, self.current_skeleton))
		self.current_obj = obj
		self.current_skeleton = skeleton

	def endElement(self, name):
		if self.stack == []:
			return

		self.current_obj, self.current_skeleton = self.stack.pop()

	def characters(self, content):
		# whitespace we dont care about
		if content.strip() == "":
			return

		if (self.current_obj) == list:
			raise Exception, "data in element list"

		self.current_obj[u"value"] = content


# namespace addition
skeleton = Element(
	Body = Element(namespace = "s",
		GetRoomListsResponse = Element(
			RoomLists = ElementList(namespace = "m",
				Address = Element(namespace = "t",
					EmailAddress = Element(namespace = "t"))))))


print "Everything"
handler = XmlToPyHandler()
xml.sax.parseString(xmlresponse, handler)
print repr(handler.obj)

print "With schema"
handler = XmlToPyHandlerSchema(skeleton)
xml.sax.parseString(xmlresponse, handler)
print repr(handler.obj)
