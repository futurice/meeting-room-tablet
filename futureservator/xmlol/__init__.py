import xml.sax
import xml.sax.handler

from pprint import pprint

# TODO: logging

__all__ = ["Element", "ElementList", "parseString"]

class Element(object):
	def __init__(self, **kargs):
		self.namespace = None
		if "namespace" in kargs:
			self.namespace = kargs["namespace"]
			del kargs["namespace"]

		self.attributes = []
		if "attributes" in kargs:
			self.attributes = kargs["attributes"]
			del kargs["attributes"]

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
		self.current_skeleton = None

	def startElement(self, name, attr):
		if self.current_obj is None:
			self.current_obj = self.obj
			self.current_skeleton = self.skeleton
			return

		# skip unknown
		if self.current_skeleton is None or name not in self.current_skeleton.children:
			self.stack.append((self.current_obj, self.current_skeleton))
			# self.current_obj = {}
			self.current_skeleton = None
			return

		obj = {}
		skeleton = self.current_skeleton.children[name]

		if type(skeleton) == Element:
			if skeleton.children == {} and skeleton.attributes == []:
				obj = u""

			elif skeleton.attributes != []:
				obj = {}
				names = set(attr.getNames())
				for a in skeleton.attributes:
					if a in names:
						obj[a] = attr.getValue(a)

			else:
				obj = {}

		elif type(skeleton) == ElementList:
			obj = []
		else:
			raise Exception, "unknown skeleton", skeleton

		self.stack.append((self.current_obj, self.current_skeleton))
		self.current_obj = obj
		self.current_skeleton = skeleton

	def endElement(self, name):
		if self.stack == []:
			return

		obj = self.current_obj
		skeleton = self.current_skeleton

		self.current_obj, self.current_skeleton = self.stack.pop()

		if skeleton is None:
			return

		if type(self.current_obj) == list:
			self.current_obj.append(obj)
		elif type(self.current_obj) == dict:
			self.current_obj[skeleton.name] = obj
		else:
			raise Exception, "data in string element"


	def characters(self, content):
		# whitespace we dont care about, and if not in skeleton => do nothing
		if content.strip() == "" or self.current_skeleton is None:
			return

		if type(self.current_obj) != unicode:
			raise Exception, "current object is not a string -- " + self.current_skeleton.name

		self.current_obj = unicode(content.strip())


def parseString(string, schema):
	handler = XmlToPyHandlerSchema(schema)
	xml.sax.parseString(string, handler)
	return handler.obj
