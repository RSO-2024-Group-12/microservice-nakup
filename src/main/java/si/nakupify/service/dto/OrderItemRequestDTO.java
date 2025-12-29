package si.nakupify.service.dto;

public class OrderItemRequestDTO {

    private Long productId;
    private Integer quantity;
    private Long unitPriceCents;

    public OrderItemRequestDTO() {}

    public OrderItemRequestDTO(Long productId, Integer quantity, Long unitPriceCents) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;
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
}
