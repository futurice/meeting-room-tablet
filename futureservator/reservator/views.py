from django.shortcuts import render_to_response
from django.http import Http404
from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.template import RequestContext

from django.conf import settings

from ews import django_ews, EWSException

from datetime import date, timedelta, datetime

DATE_FORMAT = "%Y-%m-%d"

def _get_room(ews, room_address):
	rooms = ews.get_rooms()

	for r in rooms:
		if r.address == room_address:
			return r

	raise Http404

def index(request):
	ews = django_ews()
	rooms = ews.get_rooms()

	c = {"title": "Room list", "rooms": rooms}

	return render_to_response('index.html', c, context_instance=RequestContext(request))

def _room(request, ews, room, view_all = False, c = None):
	reservations = ews.get_reservations(room)
	own_reservations = ews.get_own_reservations()

	# could be optimized from n^2 -> n
	acc = []
	for r in reservations:
		for o in own_reservations:
			if r == o:
				acc.append(r.link(o))
				break

		else:
			acc.append(r)

	reservations = acc

	day = date.today()

	if not view_all:
		try:
			day = datetime.strptime(request.GET["day"], DATE_FORMAT).date()
		except Exception as e:
			pass

	monday = day - timedelta(day.weekday())
	nextmonday = monday + timedelta(7)
	lastmonday = monday - timedelta(7)

	if not view_all:
		reservations = [r for r in reservations if r.start.date() >= monday and r.end.date() < nextmonday]


	if c is None:
		c = {}

	c.update({
		"title": room.name,
		"room_address": room.address,
		"reservations": reservations,
		"view_all": view_all,
		"monday": monday,
		"nextmonday": nextmonday.strftime(DATE_FORMAT),
		"lastmonday": lastmonday.strftime(DATE_FORMAT),
		})

	return render_to_response("room.html", c, context_instance=RequestContext(request))

def reservations(request, room_address, view_all = False):
	ews = django_ews()
	room = _get_room(ews, room_address)
	return _room(request, ews, room, view_all)


def cancel(request, room_address):
	ews = django_ews()

	room = _get_room(ews, room_address)

	try:
		ews.cancel(request.POST["itemid"])
	except (KeyError, EWSException) as e:
		return _room(request, ews, room, True, { "error_message": str(e) })

	return HttpResponseRedirect(reverse("reservations", kwargs={"room_address": room_address}))
