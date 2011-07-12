from xmlol import Element, ElementList
import xmlol

import httplib
import base64

from contextlib import closing

from datetime import datetime, timedelta, time

# TODO: trace -decorator

import logging
logger = logging.getLogger("ews")

import os

from misc import prettyxml
import misc.tzinfo as mtzinfo

utc_timeformat = "%Y-%m-%dT%H:%M:%SZ"

# Data path
this_dir, this_filename = os.path.split(__file__)
TEMPLATE_DIR = os.path.join(this_dir, "templates")

get_room_lists_skeleton = Element(
	Body = Element(namespace = "s",
		GetRoomListsResponse = Element(
			RoomLists = ElementList(namespace = "m",
				Address = Element(namespace = "t",
					EmailAddress = Element(namespace = "t"))))))

get_rooms_skeleton = Element(
	Body = Element(namespace = "s",
		GetRoomsResponse = Element(
			ResponseCode = Element(),
			Rooms = ElementList(namespace = "m",
				Room = Element(namespace = "t",
					Id = Element(namespace = "t",
						Name = Element(namespace = "t"),
						EmailAddress = Element(namespace="t")))))))

find_item_calendar_skeleton = Element(
	Body = Element(namespace = "s",
		FindItemResponse = Element(namespace="m",
			ResponseMessages = ElementList(namespace="m",
				FindItemResponseMessage = Element(namespace="m",
					ResponseCode = Element(namespace="m"),
					MessageText = Element(namespace="m"),
					RootFolder = Element(namespace="m",
						Items = ElementList(namespace="t",
							CalendarItem = Element(namespace="t",
								ItemId = Element(namespace="t", attributes=[u"Id", u"ChangeKey"]),
								Location = Element(namespace="t"),
								Start = Element(namespace="t"),
								End = Element(namespace="t"),
								Subject = Element(namespace="t"),
								Organizer = Element(namespace="t",
									Mailbox = Element(namespace="t",
										Name = Element(namespace="t")))))))))))

delete_item_skeleton = Element(
	Body = Element(namespace="s",
		DeleteItemResponse = Element(namespace="m",
			ResponseMessages = ElementList(namespace="m",
				DeleteItemResponseMessage = Element(namespace="m",
					ResponseCode = Element(namespace="m"),
					MessageText = Element(namespace="m"))))))

class EWSException(Exception):
	pass

class Room(object):
	def __init__(self, name, address):
		self.name = name
		self.address = address

	def __str__(self):
		return "<Room %s %s" % (self.name, self.address)

	def __repr__(self):
		return "Room(%s,%s)" % (repr(self.name), repr(self.address))

class Reservation(object):
	def __init__(self, id, changekey, start, end, subject, location):
		self.id = id
		self.changekey = changekey
		self.start = start
		self.end = end
		self.subject = subject
		self.location = location
		self.linked = None

	def __repr__(self):
		return "Reservation(%s,%s,%s,%s,%s)" % (repr(self.start.astimezone(mtzinfo.local).isoformat()), repr(self.end.astimezone(mtzinfo.local).isoformat()), repr(self.subject), repr(self.location), repr(self.linked))

	def link(self, other):
		self.linked = other
		return self

	def __eq__(self, other):
		return self.start == other.start and self.end == self.end and other.location == self.location



class EWS(object):
	def __init__(self, server, username, password):
		self.server   = server
		self.username = username
		self.password = password

		self.templates = {}

	def _http_post(self, data):
		# Basic authentication
		base64string = base64.b64encode('%s:%s' % (self.username, self.password))

		# Making connection
		with closing(httplib.HTTPSConnection(self.server)) as conn:
			conn.request("POST", "/EWS/Exchange.asmx", data, {"Content-Type": "text/xml", "Authorization": "Basic %s" % base64string})
			resp = conn.getresponse()

			# Checkign return code
			logger.debug("Response %d %s", resp.status, resp.reason)
			# headers resp.getheaders()

			if resp.status != 200:
				raise EWSException, resp.reason

			return resp.read()

	def _read_template(self, tpl):
		if tpl not in self.templates:
			with file(os.path.join(TEMPLATE_DIR, tpl)) as f:
				self.templates[tpl] = f.read()

		return self.templates[tpl]

	def _get_template(self, tpl, **params):
		tpl = self._read_template(tpl)
		for k, v in params.iteritems():
			tpl = tpl.replace("{"+k+"}", v)
		return tpl

	def _get_room_lists(self):
		tpl = self._read_template("GetRoomLists.xml")
		res = self._http_post(tpl)
		parsed = xmlol.parseString(res, get_room_lists_skeleton)

		#print prettyxml(res)
		#print parsed

		return map(lambda x: x["EmailAddress"], parsed["Body"]["GetRoomListsResponse"]["RoomLists"])

	def _get_rooms(self, address):
		tpl = self._get_template("GetRooms.xml", RoomListAddress=address)
		res = self._http_post(tpl)
		parsed = xmlol.parseString(res, get_rooms_skeleton)

		#print prettyxml(res)
		#print parsed

		response = parsed["Body"]["GetRoomsResponse"]

		if response["ResponseCode"] != "NoError":
			raise EWSException, "error"

		return map(lambda x: Room(x["Id"]["Name"], x["Id"]["EmailAddress"]), response["Rooms"])

	def get_rooms(self):
		rooms = []
		lists = self._get_room_lists()
		for l in lists:
			rooms += self._get_rooms(l)
		return rooms

	def _get_reservations(self, template, **tpl_params):
		now = datetime.now(tz=mtzinfo.utc).replace(microsecond=0)

		start = now - timedelta(7)
		end = now + timedelta(40)

		tpl_params["StartTime"] = start.strftime(utc_timeformat)
		tpl_params["EndTime"] = end.strftime(utc_timeformat)

		tpl = self._get_template(template, **tpl_params)
		res = self._http_post(tpl)
		parsed = xmlol.parseString(res, find_item_calendar_skeleton)

		#print tpl
		#print prettyxml(res)
		#print parsed

		message = parsed["Body"]["FindItemResponse"]["ResponseMessages"][0]

		# print message

		if message["ResponseCode"] != "NoError":
			raise EWSException, message["MessageText"]

		def parse_datetime(s):
			return datetime.strptime(s, utc_timeformat).replace(tzinfo=mtzinfo.utc).astimezone(mtzinfo.local)

		def reservation_parse(x):
			itemid    = x["ItemId"]["Id"]
			changekey = x["ItemId"]["ChangeKey"]
			subject   = x["Subject"]
			try:
				location  = x["Location"]
			except KeyError:
				location = ""

			start = parse_datetime(x["Start"])
			end = parse_datetime(x["End"])

			# splitting many-days-spanning reservations
			acc = []
			while start.date() != end.date():
				tmp = datetime.combine(start.date() + timedelta(1), time(0, 0, tzinfo=mtzinfo.local))
				acc.append(Reservation(itemid, changekey, start, tmp, subject, location))
				start = tmp

			if start != end:
				acc.append(Reservation(itemid, changekey, start, end, subject, location))

			return acc

		# Sum, as we could split reservation into many
		r = sum(map(reservation_parse, message["RootFolder"]["Items"]), [])
		r.sort(key= lambda x: x.start)
		return r

	def get_reservations(self, room):
		return self._get_reservations("FindItemCalendar.xml", RoomAddress=room.address)

	def get_own_reservations(self):
		return self._get_reservations("FindItemCalendarOwn.xml")

	def cancel(self, itemid):
		tpl = self._get_template('DeleteItem.xml', ItemId=itemid)
		res = self._http_post(tpl)
		parsed = xmlol.parseString(res, delete_item_skeleton)

		#print tpl
		#print prettyxml(res)
		#print parsed

		message = parsed["Body"]["DeleteItemResponse"]["ResponseMessages"][0]

		if message["ResponseCode"] != "NoError":
			raise EWSException, message["MessageText"]


def django_ews():
	from django.conf import settings
	return EWS(settings.RESERVATOR["server"], settings.RESERVATOR["username"], settings.RESERVATOR["password"])
