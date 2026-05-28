### A cool way of teaching web requisitions!

there are some challenges coded that expect JSON object to be solved

### /melancia
expects an object like

{
  "nome": yourName
  "resposta": 1024
}

for it to be solved


### /abobora
expects

{
  "nome": yourName
  "resposta": 13
}

 
###/tomato 
should be solved using

response = requests.get("https:localhost:4500/tomate").json()

dados = {
    "nome": yourName,
    "id": response["id"],
    "resposta": response["numero"]**2
}

code should send an server event to a front-end showing the winners for every challenge
