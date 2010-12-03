from django.conf.urls.defaults import *
from django.http import HttpResponse
import os

def index(request):
    return HttpResponse("Hello, world. You're at the poll index.")

urlpatterns = patterns(
    '',
    (r'^reservation/$', 'views.reservation.index'),
    (r'^static/(?P<path>.*)$', 'django.views.static.serve',
     {'document_root': os.path.abspath('ui/'), 'show_indexes': True}),
)

