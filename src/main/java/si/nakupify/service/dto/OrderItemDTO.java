package si.nakupify.service.dto;

public class OrderItemDTO {

    private Long id;
    private Long productId;
    private Integer quantity;
    private Long unitPriceCents;
    private Long totalPriceCents;

    public OrderItemDTO() {
    }

    public OrderItemDTO(Long id, Long productId, Integer quantity, Long unitPriceCents, Long totalPriceCents) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
        this.totalPriceCents = totalPriceCents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getUnitPriceCents() {
        return unitPriceCents;
    }

    public void setUnitPriceCents(Long unitPriceCents) {
        this.unitPriceCents = unitPriceCents;
    }

    public Long getTotalPriceCents() {
        return totalPriceCents;
    }

    public void setTotalPriceCents(Long totalPriceCents) {
        this.totalPriceCents = totalPriceCents;
    }
}
