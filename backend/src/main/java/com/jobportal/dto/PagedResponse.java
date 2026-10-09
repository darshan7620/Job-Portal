package com.jobportal.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PagedResponse(List<T> content, int page, int size, long totalElements,
                         int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public static <E, R> PagedResponse<R> from(Page<E> source, Function<E, R> mapper) {
        return new PagedResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast());
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
