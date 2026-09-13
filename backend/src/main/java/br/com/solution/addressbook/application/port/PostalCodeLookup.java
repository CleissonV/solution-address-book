package br.com.solution.addressbook.application.port;

import br.com.solution.addressbook.application.dto.AddressDtos.PostalCodeResponse;

public interface PostalCodeLookup {
    PostalCodeResponse lookup(String zipCode);
}
