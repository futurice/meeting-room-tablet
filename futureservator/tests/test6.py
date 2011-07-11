from datetime import datetime

if True:
	s = "2011-07-06T13:00:00Z"
	f = "%Y-%m-%dT%H:%M:%SZ"

	print datetime.strptime(s, f)

if True:
	s = "2011-07-06T13:00:00"
	f = "%Y-%m-%dT%H:%M:%S"

	print datetime.strptime(f, s)

