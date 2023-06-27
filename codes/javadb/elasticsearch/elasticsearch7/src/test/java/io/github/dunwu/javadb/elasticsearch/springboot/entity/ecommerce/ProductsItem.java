package io.github.dunwu.javadb.elasticsearch.springboot.entity.ecommerce;

import lombok.Data;

@Data
public class ProductsItem {
    private int taxAmount;
    private double taxfulPrice;
    private int quantity;
    private double taxlessPrice;
    private int discountAmount;
    private double baseUnitPrice;
    private int discountPercentage;
    private String productName;
    private String manufacturer;
    private double minPrice;
    private String createdOn;
    private int unitDiscountAmount;
    private double price;
    private int productId;
    private double basePrice;
    private String id;
    private String category;
    private String sku;
}
