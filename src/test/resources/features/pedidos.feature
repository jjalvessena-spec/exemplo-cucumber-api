# language: pt
Funcionalidade: API de Pedidos
  Como cliente da loja
  Quero gerenciar meus pedidos pela API
  Para acompanhar minhas compras

  Contexto:
    Dado que a API de pedidos está disponível

  Cenário: Criar um novo pedido
    Quando eu criar um pedido para o cliente "Maria Silva" com o produto "Notebook" e quantidade 2
    Então o status da resposta deve ser 201
    E o pedido retornado deve ter o status "CRIADO"
    E o pedido retornado deve conter o cliente "Maria Silva"

  Cenário: Consultar um pedido existente
    Quando eu consultar o pedido de id 1
    Então o status da resposta deve ser 200
    E o pedido retornado deve ter o status "ENVIADO"

  Cenário: Consultar um pedido inexistente
    Quando eu consultar o pedido de id 999
    Então o status da resposta deve ser 404
