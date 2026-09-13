package br.com.solution.addressbook.api;

import br.com.solution.addressbook.api.dto.AddressDtos.PostalCodeResponse;
import br.com.solution.addressbook.infrastructure.viacep.ViaCepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/postal-codes")
public class PostalCodeController {
    private final ViaCepService viaCepService;

    public PostalCodeController(ViaCepService viaCepService) { this.viaCepService = viaCepService; }

    @GetMapping("/{zipCode}")
    ResponseEntity<PostalCodeResponse> lookup(@PathVariable String zipCode) {
        return ResponseEntity.ok(viaCepService.lookup(zipCode));
    }
}
