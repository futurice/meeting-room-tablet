from django.shortcuts import render_to_response
from django.http import Http404

from django.conf import settings

from ews import django_ews

def index(response):
	ews = django_ews()
	rooms = ews.get_rooms()

	c = {"title": "Room list", "rooms": rooms, "CDN_URL": settings.CDN_URL}

	return render_to_response('index.html', c)

def room(response, room_address):
	ews = django_ews()

	rooms = ews.get_rooms()

	room = None
	for r in rooms:
		if r.address == room_address:
			room = r

	if room is None:
		raise Http404

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


	c = { "title": room.name, "CDN_URL": settings.CDN_URL, "reservations": reservations}

	return render_to_response('room.html', c)
