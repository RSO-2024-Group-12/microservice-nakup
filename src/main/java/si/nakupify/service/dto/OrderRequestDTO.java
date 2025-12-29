package si.nakupify.service.dto;

import java.util.List;

public class OrderRequestDTO {

    private Long userId;
    private String recipientName;
    private String street;
    private String houseNumber;
    private String city;
    private String postalCode;
    private String country;
    private String paymentMethod;
    private Boolean paid;
    private Long shippingCostCents;
    private List<OrderItemRequestDTO> items;

    public OrderRequestDTO() {}

    public OrderRequestDTO(Long userId, String recipientName, String street, String houseNumber, String city, String postalCode, String country, String paymentMethod, Boolean paid, Long shippingCostCents, List<OrderItemRequestDTO> items) {
        this.userId = userId;
        this.recipientName = recipientName;
        this.street = street;
        this.houseNumber = houseNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.shippingCostCents = shippingCostCents;
        this.items = items;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Long getShippingCostCents() {
        return shippingCostCents;
    }

    public void setShippingCostCents(Long shippingCostCents) {
        this.shippingCostCents = shippingCostCents;
    }

    public List<OrderItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDTO> items) {
        this.items = items;
    }
}
