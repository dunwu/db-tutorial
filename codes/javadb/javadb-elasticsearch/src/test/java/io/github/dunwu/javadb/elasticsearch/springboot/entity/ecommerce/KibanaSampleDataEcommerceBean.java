package io.github.dunwu.javadb.elasticsearch.springboot.entity.ecommerce;

import lombok.Data;

import java.util.List;

@Data
public class KibanaSampleDataEcommerceBean {
    private Geoip geoip;
    private String customerFirstName;
    private String customerPhone;
    private String type;
    private List<String> manufacturer;
    private List<ProductsItem> products;
    private String customerFullName;
    private String orderDate;
    private String customerLastName;
    private int dayOfWeekI;
    private int totalQuantity;
    private String currency;
    private double taxlessTotalPrice;
    private int totalUniqueProducts;
    private List<String> category;
    private int customerId;
    private List<String> sku;
    private int orderId;
    private String user;
    private String customerGender;
    private String email;
    private String dayOfWeek;
    private double taxfulTotalPrice;
}
