package si.nakupify.service.dto;

import java.time.Instant;
import java.util.List;

public class OrderDTO {

    private Long id;

    private Long userId;

    private String recipientName;

    private String street;

    private String houseNumber;

    private String city;

    private String postalCode;

    private String country;

    private Long totalPriceCents;

    private Long shippingCostCents;

    private Boolean paid;

    private String paymentMethod;

    private Long shipmentId;

    private String trackingNumber;

    private OrderStatusDTO status;

    private Instant createdAt;

    private Instant updatedAt;

    private List<OrderItemDTO> items;

    public OrderDTO() {
    }

    public OrderDTO(Long id, Long userId, String recipientName, String street, String houseNumber, String city, String postalCode, String country, Long totalPriceCents, Long shippingCostCents, Boolean paid, String paymentMethod, Long shipmentId, String trackingNumber, OrderStatusDTO status, Instant createdAt, Instant updatedAt, List<OrderItemDTO> items) {
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.totalPriceCents = totalPriceCents;
        this.shippingCostCents = shippingCostCents;
        this.paid = paid;
        this.paymentMethod = paymentMethod;
        this.shipmentId = shipmentId;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getTotalPriceCents() {
        return totalPriceCents;
    }

    public void setTotalPriceCents(Long totalPriceCents) {
        this.totalPriceCents = totalPriceCents;
    }

    public Long getShippingCostCents() {
        return shippingCostCents;
    }

    public void setShippingCostCents(Long shippingCostCents) {
        this.shippingCostCents = shippingCostCents;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public OrderStatusDTO getStatus() {
        return status;
    }

    public void setStatus(OrderStatusDTO status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
