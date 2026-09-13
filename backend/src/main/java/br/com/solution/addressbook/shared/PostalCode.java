package br.com.solution.addressbook.shared;

public final class PostalCode {
    private PostalCode() {}

    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static boolean isValid(String value) {
        return normalize(value).matches("\\d{8}");
    }
}

