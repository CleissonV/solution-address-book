package br.com.solution.addressbook.infrastructure.viacep;

import br.com.solution.addressbook.api.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.api.error.DomainException;
import br.com.solution.addressbook.shared.PostalCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ViaCepService {
    private final RestClient restClient;

    public ViaCepService(RestClient viaCepRestClient) {
        this.restClient = viaCepRestClient;
    }

    @Cacheable(cacheNames = "postalCodes", key = "T(br.com.solution.addressbook.shared.PostalCode).normalize(#zipCode)")
    public PostalCodeResponse lookup(String zipCode) {
        String normalized = PostalCode.normalize(zipCode);
        if (!PostalCode.isValid(normalized)) {
            throw new DomainException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ZIP_CODE", "CEP invalido.");
        }

        try {
            ViaCepResponse response = restClient.get()
                    .uri("/{cep}/json", normalized)
                    .retrieve()
                    .body(ViaCepResponse.class);
            if (response == null || Boolean.TRUE.equals(response.error())) {
                throw new DomainException(HttpStatus.NOT_FOUND, "ZIP_CODE_NOT_FOUND", "CEP nao encontrado.");
            }
            return new PostalCodeResponse(normalized, response.logradouro(), response.bairro(),
                    response.localidade(), response.uf());
        } catch (DomainException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new DomainException(HttpStatus.BAD_GATEWAY, "POSTAL_CODE_PROVIDER_UNAVAILABLE",
                    "Servico de CEP temporariamente indisponivel.");
        }
    }
}

