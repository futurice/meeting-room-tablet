import xml.dom.minidom as dom

def prettyxml(xml):
	x = dom.parseString(xml)
	return x.toprettyxml(indent="  ")

