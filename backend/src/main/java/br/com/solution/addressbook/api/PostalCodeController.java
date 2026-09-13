package br.com.solution.addressbook.api;

import br.com.solution.addressbook.application.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.application.port.PostalCodeLookup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postal-codes")
public class PostalCodeController {
    private final PostalCodeLookup postalCodeLookup;

    public PostalCodeController(PostalCodeLookup postalCodeLookup) {
        this.postalCodeLookup = postalCodeLookup;
    }

    @GetMapping("/{zipCode}")
    ResponseEntity<PostalCodeResponse> lookup(@PathVariable String zipCode) {
        return ResponseEntity.ok(postalCodeLookup.lookup(zipCode));
    }
}
