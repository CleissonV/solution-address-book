package br.com.solution.addressbook.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {
    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "11144477735", "123.456.789-09"})
    void acceptsValidCpf(String cpf) {
        assertThat(Cpf.isValid(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "11111111111", "52998224724", "123"})
    void rejectsInvalidCpf(String cpf) {
        assertThat(Cpf.isValid(cpf)).isFalse();
    }
}

