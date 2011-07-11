from django.conf.urls.defaults import patterns, include, url
from reservator import views

urlpatterns = patterns('',
		url(r'^$', views.index, name="index"),
		url(r'reservations/(?P<room_address>[^\/\s]+)/$', views.reservations, {"view_all": False}, name="reservations"),
		url(r'reservations/all/(?P<room_address>[^\/\s]+)/$', views.reservations, {"view_all": True}, name="reservations-all"),
		url(r'cancel/(?P<room_address>\S+)/$', views.cancel, name="cancel"),
)
