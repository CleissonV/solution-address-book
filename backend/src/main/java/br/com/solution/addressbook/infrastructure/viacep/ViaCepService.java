package br.com.solution.addressbook.infrastructure.viacep;

import br.com.solution.addressbook.application.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.application.error.DomainException;
import br.com.solution.addressbook.application.port.PostalCodeLookup;
import br.com.solution.addressbook.shared.PostalCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ViaCepService implements PostalCodeLookup {
    private final RestClient restClient;

    public ViaCepService(RestClient viaCepRestClient) {
        this.restClient = viaCepRestClient;
    }

    @Cacheable(
            cacheNames = "postalCodes",
            key = "T(br.com.solution.addressbook.shared.PostalCode).normalize(#zipCode)")
    @Override
    public PostalCodeResponse lookup(String zipCode) {
        String normalized = PostalCode.normalize(zipCode);
        if (!PostalCode.isValid(normalized)) {
            throw new DomainException("INVALID_ZIP_CODE", "CEP invalido.");
        }

        try {
            ViaCepResponse response =
                    restClient
                            .get()
                            .uri("/{cep}/json", normalized)
                            .retrieve()
                            .body(ViaCepResponse.class);
            if (response == null || Boolean.TRUE.equals(response.error())) {
                throw new DomainException("ZIP_CODE_NOT_FOUND", "CEP nao encontrado.");
            }
            return new PostalCodeResponse(
                    normalized,
                    response.logradouro(),
                    response.bairro(),
                    response.localidade(),
                    response.uf());
        } catch (DomainException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new DomainException(
                    "POSTAL_CODE_PROVIDER_UNAVAILABLE",
                    "Servico de CEP temporariamente indisponivel.");
        }
    }
}
