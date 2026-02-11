package com.example.CJLInvestimentos.util;

import java.util.regex.Pattern;

/**
 * Validação de CNPJ e CPF (algoritmo dos dígitos verificadores - módulo 11).
 */
public final class DocumentoUtil {

    private static final Pattern APENAS_DIGITOS = Pattern.compile("\\D");

    /** Remove tudo que não for dígito. */
    public static String apenasDigitos(String valor) {
        if (valor == null) return "";
        return APENAS_DIGITOS.matcher(valor).replaceAll("");
    }

    /** CNPJ: 14 dígitos; primeiro DV com pesos 5,4,3,2,9,8,7,6,5,4,3,2; segundo DV com pesos 6,5,4,3,2,9,8,7,6,5,4,3,2. */
    public static boolean isValidCnpj(String cnpj) {
        String s = apenasDigitos(cnpj);
        if (s.length() != 14) return false;
        if (s.chars().distinct().count() <= 1) return false; // rejeita 11111111111111 etc

        int[] p1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] p2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (s.charAt(i) - '0') * p1[i];
        int d1 = mod11(soma);
        if (d1 != (s.charAt(12) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 13; i++) soma += (s.charAt(i) - '0') * p2[i];
        int d2 = mod11(soma);
        return d2 == (s.charAt(13) - '0');
    }

    /** CPF: 11 dígitos; primeiro DV pesos 10..2 nos 9 primeiros; segundo DV pesos 11..2 nos 9 + primeiro DV. */
    public static boolean isValidCpf(String cpf) {
        String s = apenasDigitos(cpf);
        if (s.length() != 11) return false;
        if (s.chars().distinct().count() <= 1) return false;

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (s.charAt(i) - '0') * (10 - i);
        int d1 = mod11(soma);
        if (d1 != (s.charAt(9) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += (s.charAt(i) - '0') * (11 - i);
        int d2 = mod11(soma);
        return d2 == (s.charAt(10) - '0');
    }

    /** Formato 11 - resto; se resto < 2 então dígito 0, senão 11 - resto. */
    private static int mod11(int soma) {
        int r = soma % 11;
        return r < 2 ? 0 : (11 - r);
    }

    /** Formata CNPJ como 00.000.000/0001-00. */
    public static String formatarCnpj(String cnpj) {
        String s = apenasDigitos(cnpj);
        if (s.length() != 14) return cnpj;
        return s.substring(0, 2) + "." + s.substring(2, 5) + "." + s.substring(5, 8) + "/" + s.substring(8, 12) + "-" + s.substring(12);
    }

    /** Formata CPF como 000.000.000-00. */
    public static String formatarCpf(String cpf) {
        String s = apenasDigitos(cpf);
        if (s.length() != 11) return cpf;
        return s.substring(0, 3) + "." + s.substring(3, 6) + "." + s.substring(6, 9) + "-" + s.substring(9);
    }
}
