package br.net.convertix.dinix.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record Paginacao(int numPag, int itensPag) {

    public static final int ITENS_PADRAO = 20;
    public static final int ITENS_MAXIMO = 100;

    public Pageable toPageable() {
        int pagina = Math.max(numPag, 1);
        int tamanho = itensPag < 1 ? ITENS_PADRAO : Math.min(itensPag, ITENS_MAXIMO);
        return PageRequest.of(pagina - 1, tamanho);
    }
}
