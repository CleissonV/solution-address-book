package br.com.solution.addressbook.infrastructure.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        @JsonProperty("erro") Boolean error) {}
