from django.conf.urls.defaults import *
from django.http import HttpResponse

def index(request):
    return HttpResponse("Hello, world. You're at the poll index.")

urlpatterns = patterns('',
   (r'^reservation/$', 'views.reservation.index'),
)

