from django.conf import settings

def cdn(request):
	print "foo"
	return {'CDN_URL': "foo" + settings.CDN_URL}
