from django.http import HttpResponse

def index(request):
    if request.method == 'GET':
        return get(request)
    elif request.method == 'POST':
        return post(request)

def get(request):
    return HttpResponse("Hello, world. You're at the poll index.")

def post(request):
    return HttpResponse("Hello, world. You're at the poll index.")
