from ews import EWS

import logging
logging.basicConfig(level=logging.DEBUG, format="%(levelname)-8s %(name)-8s %(message)s")

from datetime import date

ews = EWS("10.4.2.214", "test", "Password1")

rooms = ews.get_rooms()

for room in rooms:
	print room
	reservations = ews.get_reservations(room)

	print "All"
	for reservation in reservations:
		print reservation

	print "Today"
	for reservation in reservations:
		if reservation.start.date() == date.today():
			print reservation

	print "---"
