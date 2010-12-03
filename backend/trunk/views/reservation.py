from django.shortcuts import render_to_response

def index(request):
    if request.method == 'GET':
        return get(request)
    elif request.method == 'POST':
        return post(request)

def get(request):
    return render_to_response('get-reservation-test-data.json')

def post(request):
    return render_to_response('post-reservation-test-data.json')
