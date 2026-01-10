package si.nakupify.service.dto;

import java.util.List;

public class PaymentOrderDTO {

    private Long id_buyer;

    private String recipient;

    private String recipient_email;

    private Long id_seller;

    private String store;

    private Float amount;

    private String currency;

    private String id_order;

    private String redirect_url;

    private String return_url;

    private String cancel_url;

    private String street;

    private String house_number;

    private String city;

    private String postal_code;

    private String country;

    private List<ElementDTO> items;

    public PaymentOrderDTO() {}

    public PaymentOrderDTO(Long id_buyer, String recipient, String recipient_email, Long id_seller, String store, Float amount, String currency,
               String id_order, String redirect_url, String return_url, String cancel_url,
               String street, String house_number, String city, String postal_code, String country,
               List<ElementDTO> items) {
        this.id_buyer = id_buyer;
        this.recipient = recipient;
        this.recipient_email = recipient_email;
        this.id_seller = id_seller;
        this.store = store;
        this.amount = amount;
        this.currency = currency;
        this.id_order = id_order;
        this.redirect_url = redirect_url;
        this.return_url = return_url;
        this.cancel_url = cancel_url;
        this.street = street;
        this.house_number = house_number;
        this.city = city;
        this.postal_code = postal_code;
        this.country = country;
        this.items = items;
    }

    public Long getId_buyer() {
        return id_buyer;
    }

    public void setId_buyer(Long id_buyer) {
        this.id_buyer = id_buyer;
    }

    public Long getId_seller() {
        return id_seller;
    }

    public void setId_seller(Long id_seller) {
        this.id_seller = id_seller;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getId_order() {
        return id_order;
    }

    public void setId_order(String id_order) {
        this.id_order = id_order;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getCancel_url() {
        return cancel_url;
    }

    public void setCancel_url(String cancel_url) {
        this.cancel_url = cancel_url;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse_number() {
        return house_number;
    }

    public void setHouse_number(String house_number) {
        this.house_number = house_number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<ElementDTO> getItems() {
        return items;
    }

    public void setItems(List<ElementDTO> items) {
        this.items = items;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getRecipient_email() {
        return recipient_email;
    }

    public void setRecipient_email(String recipient_email) {
        this.recipient_email = recipient_email;
    }
}
