from django.conf import settings

def cdn(request):
	return {'CDN_URL': settings.CDN_URL}
