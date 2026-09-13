package br.com.solution.addressbook.shared;

public final class Cpf {
    private Cpf() {}

    public static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static boolean isValid(String value) {
        String cpf = normalize(value);
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) return false;
        try {
            int first = digit(cpf, 9, 10);
            int second = digit(cpf, 10, 11);
            return first == Character.digit(cpf.charAt(9), 10)
                    && second == Character.digit(cpf.charAt(10), 10);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static int digit(String cpf, int length, int weight) {
        int sum = 0;
        for (int i = 0; i < length; i++) sum += Character.digit(cpf.charAt(i), 10) * (weight - i);
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}

