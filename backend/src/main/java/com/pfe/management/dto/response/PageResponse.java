package com.pfe.management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Réponse paginée générique")
public class PageResponse<T> {

    @Schema(description = "Éléments de la page actuelle")
    private List<T> content;

    @Schema(description = "Numéro de la page (0-indexé)", example = "0")
    private int pageNumber;

    @Schema(description = "Taille de la page", example = "10")
    private int pageSize;

    @Schema(description = "Nombre total d'éléments", example = "42")
    private long totalElements;

    @Schema(description = "Nombre total de pages", example = "5")
    private int totalPages;

    @Schema(description = "Indique si c'est la dernière page", example = "false")
    private boolean last;

    @Schema(description = "Indique si c'est la première page", example = "true")
    private boolean first;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last, boolean first) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = first;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}
