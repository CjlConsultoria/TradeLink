package com.example.CJLInvestimentos.utils;

/**
 * Utilitários para validação de documentos brasileiros (CPF).
 */
public final class DocumentoUtil {

    private DocumentoUtil() {}

    /** Remove todos os caracteres que não são dígitos. */
    public static String apenasDigitos(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("\\D", "");
    }

    /**
     * Valida um CPF brasileiro usando o algoritmo oficial dos dois dígitos verificadores.
     * Aceita tanto formatado (XXX.XXX.XXX-XX) quanto somente dígitos (11 dígitos).
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null) return false;
        String digits = apenasDigitos(cpf);
        if (digits.length() != 11) return false;

        // Rejeita sequências iguais (000.000.000-00, 111.111.111-11, etc.)
        boolean todosIguais = true;
        for (int i = 1; i < 11; i++) {
            if (digits.charAt(i) != digits.charAt(0)) {
                todosIguais = false;
                break;
            }
        }
        if (todosIguais) return false;

        // Primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (digits.charAt(i) - '0') * (10 - i);
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : (11 - resto);
        if ((digits.charAt(9) - '0') != dv1) return false;

        // Segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (digits.charAt(i) - '0') * (11 - i);
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : (11 - resto);
        return (digits.charAt(10) - '0') == dv2;
    }
}
