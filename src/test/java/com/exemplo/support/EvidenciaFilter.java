package com.exemplo.support;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import io.cucumber.java.Scenario;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Filtro do Rest Assured que captura cada requisição/resposta e a anexa
 * como evidência no passo atual do cenário do Cucumber.
 */
public class EvidenciaFilter implements Filter {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Set<String> CAMINHOS_IGNORADOS = Set.of("/api/health");

    private final Scenario scenario;

    public EvidenciaFilter(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public Response filter(FilterableRequestSpecification requisicao,
                           FilterableResponseSpecification especificacaoResposta,
                           FilterContext contexto) {
        long inicio = System.currentTimeMillis();
        Response resposta = contexto.next(requisicao, especificacaoResposta);
        long duracao = System.currentTimeMillis() - inicio;

        if (deveIgnorar(requisicao)) {
            return resposta;
        }

        String corpoRequisicao = requisicao.getBody() == null
                ? "(vazio)"
                : requisicao.getBody().toString();
        String corpoResposta = resposta.getBody().asString().isBlank()
                ? "(vazio)"
                : resposta.getBody().asPrettyString();

        String evidencia = """
                ===== REQUISIÇÃO =====
                Data/hora: %s
                %s %s

                Headers:
                %s

                Body:
                %s

                ===== RESPOSTA =====
                %s  (%d ms)

                Headers:
                %s

                Body:
                %s
                """.formatted(
                LocalDateTime.now().format(FORMATO_DATA),
                requisicao.getMethod(), requisicao.getURI(),
                requisicao.getHeaders(),
                corpoRequisicao,
                resposta.getStatusLine(), duracao,
                resposta.getHeaders(),
                corpoResposta);

        scenario.attach(evidencia.getBytes(StandardCharsets.UTF_8), "text/plain",
                requisicao.getMethod() + " " + requisicao.getDerivedPath()
                        + " -> " + resposta.getStatusCode());

        return resposta;
    }

    /** Chamadas técnicas (como o health check) não geram evidência. */
    private boolean deveIgnorar(FilterableRequestSpecification requisicao) {
        return CAMINHOS_IGNORADOS.contains(requisicao.getDerivedPath());
    }
}
