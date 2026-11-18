package com.exemplo.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.exemplo.support.EvidenciaFilter;
import com.github.tomakehurst.wiremock.WireMockServer;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PedidoSteps {

    private WireMockServer servidor;
    private Response resposta;

    @Before
    public void iniciarApiFicticia(Scenario scenario) {
        // Sobe um servidor WireMock em porta livre para simular a API de pedidos
        servidor = new WireMockServer(wireMockConfig().dynamicPort());
        servidor.start();
        registrarStubs();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = servidor.port();
        RestAssured.basePath = "/api";

        // Anexa requisição e resposta de cada chamada no relatório do Cucumber
        RestAssured.filters(new EvidenciaFilter(scenario));
    }

    @After
    public void pararApiFicticia() {
        if (servidor != null) {
            servidor.stop();
        }
        RestAssured.reset();
    }

    @Dado("que a API de pedidos está disponível")
    public void queAApiDePedidosEstaDisponivel() {
        given().when().get("/health").then().statusCode(200);
    }

    @Quando("eu criar um pedido para o cliente {string} com o produto {string} e quantidade {int}")
    public void euCriarUmPedido(String cliente, String produto, int quantidade) {
        String corpo = """
                {
                  "cliente": "%s",
                  "itens": [ { "produto": "%s", "quantidade": %d } ]
                }
                """.formatted(cliente, produto, quantidade);

        resposta = given()
                .contentType(ContentType.JSON)
                .body(corpo)
            .when()
                .post("/pedidos");
    }

    @Quando("eu consultar o pedido de id {int}")
    public void euConsultarOPedido(int id) {
        resposta = given()
                .accept(ContentType.JSON)
            .when()
                .get("/pedidos/{id}", id);
    }

    @Então("o status da resposta deve ser {int}")
    public void oStatusDaRespostaDeveSer(int statusEsperado) {
        resposta.then().statusCode(statusEsperado);
    }

    @Então("o pedido retornado deve ter o status {string}")
    public void oPedidoRetornadoDeveTerOStatus(String status) {
        resposta.then().body("status", equalTo(status));
    }

    @Então("o pedido retornado deve conter o cliente {string}")
    public void oPedidoRetornadoDeveConterOCliente(String cliente) {
        resposta.then().body("cliente", equalTo(cliente));
    }

    private void registrarStubs() {
        servidor.stubFor(get(urlEqualTo("/api/health"))
                .willReturn(aResponse().withStatus(200)));

        servidor.stubFor(post(urlEqualTo("/api/pedidos"))
                .withRequestBody(matchingJsonPath("$.cliente"))
                .withRequestBody(matchingJsonPath("$.itens[0].produto"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 42,
                                  "cliente": "{{jsonPath request.body '$.cliente'}}",
                                  "status": "CRIADO"
                                }
                                """)
                        .withTransformers("response-template")));

        servidor.stubFor(get(urlEqualTo("/api/pedidos/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "id": 1, "cliente": "João Souza", "status": "ENVIADO" }
                                """)));

        servidor.stubFor(get(urlEqualTo("/api/pedidos/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "erro": "Pedido não encontrado" }
                                """)));
    }
}
