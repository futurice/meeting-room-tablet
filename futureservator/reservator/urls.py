from django.conf.urls.defaults import patterns, include, url

urlpatterns = patterns('reservator.views',
		(r'^$', 'index'),
		(r'room/(\S+)/$', 'room')
)
