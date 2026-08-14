package br.net.convertix.dinix.dto.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public record PageResponse<T>(
        List<T> itens,
        int numPag,
        int maxPag,
        long maxItens,
        int itensPag
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumberOfElements()
        );
    }

    public static <T> PageResponse<T> fromList(List<T> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<T> slice = start >= all.size() ? List.of() : all.subList(start, end);
        return from(new PageImpl<>(slice, pageable, all.size()));
    }
}
