### A cool way of teaching web requisitions!

Basically, running this softwarem you will have some endpoints serving as challenges, expecting JSON formatted values. You should present them in a pretty way for people, making them have a feedback for their HTTP requests.

## Endpoints (can be tested in the url https://oficina.daviipkp.org/):

### POST /melancia

expects
{
  "nome": yourName
  "resposta": 1024
}


### POST /abobora

expects
{
  "nome": yourName
  "resposta": 13
}

 
### POST /tomato 

should be solved using

response = requests.get("https://oficina.daviipkp.org/tomate").json()

dados = {
    "nome": yourName,
    "id": response["id"],
    "resposta": response["numero"]**2
}


After solving any of the challenges, this back-end will send an event to everyone connected in the URLs
https://oficina-viewer.daviipkp.org/<challenge>

For example:
You solved /melancia sending a post to https://oficina.daviipkp.org/melancia
You can visualize the result of if accessing https://oficina-viewer.daviipkp.org/melancia
