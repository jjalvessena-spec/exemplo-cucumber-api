# exemplo-cucumber-api

Projeto de exemplo de testes de API com **Cucumber** (BDD) e **Rest Assured**, validando uma API de pedidos fictícia.

A API é simulada com **WireMock**, que sobe automaticamente antes de cada cenário. Por isso, o projeto roda sem nenhum serviço externo.

## Pré-requisitos

- Java 17 ou superior
- Maven 3.8 ou superior

Para conferir as versões instaladas:

```bash
java -version
mvn -version
```

## Como rodar

### Pela linha de comando

Na raiz do projeto:

```bash
mvn test
```

Para rodar apenas os cenários de uma feature ou linha específica:

```bash
mvn test -Dcucumber.features=src/test/resources/features/pedidos.feature:10
```

### Pelo Eclipse ou IntelliJ

1. Importe o projeto como **projeto Maven existente**.
2. Abra `src/test/java/com/exemplo/runner/CucumberRunner.java`.
3. Clique com o botão direito e escolha **Run As > JUnit Test** (Eclipse) ou **Run 'CucumberRunner'** (IntelliJ).

O runner usa `stepNotifications = true`, então na aba JUnit cada passo (Dado/Quando/Então) aparece como um item próprio, com resultado individual.

## Relatórios

Ao final da execução, os relatórios ficam em:

| Arquivo                         | Conteúdo                         |
|---------------------------------|----------------------------------|
| `target/cucumber-report.html`   | Relatório HTML para abrir no navegador |
| `target/cucumber.json`          | Resultado em JSON (útil para CI) |

### Evidências das chamadas

Cada requisição feita pelo Rest Assured é anexada automaticamente ao passo que a executou no relatório HTML, por meio da classe `EvidenciaFilter`. Para ver a evidência, abra o relatório, expanda o passo e clique no anexo, que tem o nome no formato `POST /api/pedidos -> 201`. O anexo contém:

- data/hora da chamada;
- método, URL, headers e body da requisição;
- status, tempo de resposta, headers e body da resposta.

Chamadas técnicas, como o health check (`GET /api/health`), não geram evidência. Para ignorar outros caminhos, adicione-os ao conjunto `CAMINHOS_IGNORADOS` em `EvidenciaFilter`.

## Estrutura

```
exemplo-cucumber-api/
├── pom.xml
└── src/test/
    ├── java/com/exemplo/
    │   ├── runner/CucumberRunner.java   # Runner (JUnit 4 + @CucumberOptions)
    │   ├── steps/PedidoSteps.java       # Steps com Rest Assured + mock WireMock
    │   └── support/EvidenciaFilter.java # Anexa requisição/resposta no relatório
    └── resources/features/
        └── pedidos.feature              # Cenários em português (Gherkin)
```

## Cenários cobertos

| Cenário                         | Requisição              | Resultado esperado            |
|---------------------------------|-------------------------|-------------------------------|
| Criar um novo pedido            | `POST /api/pedidos`     | 201, status `CRIADO`          |
| Consultar um pedido existente   | `GET /api/pedidos/1`    | 200, status `ENVIADO`         |
| Consultar um pedido inexistente | `GET /api/pedidos/999`  | 404                           |

## Testando contra uma API real

Para apontar os testes para uma API de verdade em vez do mock:

1. Em `PedidoSteps.java`, remova a criação do `WireMockServer` no método `@Before` e a chamada `registrarStubs()`.
2. Ajuste `RestAssured.baseURI`, `RestAssured.port` e `RestAssured.basePath` para o endereço da API.
3. Opcionalmente, remova a dependência `wiremock-standalone` do `pom.xml`.

## Observação sobre acentos no console

No PowerShell/CMD do Windows, os acentos podem aparecer trocados na saída do Maven. É apenas a codificação do terminal; os arquivos estão em UTF-8. Para corrigir, rode antes:

```powershell
chcp 65001
```
