package com.vegas.tranquilo.models;

import java.io.Serializable;
import java.util.List;

public class ProductGroup implements Serializable {

    private String group_name;
    private List<ProductMember> products;

    public ProductGroup(String group_name, List<ProductMember> products) {
        this.group_name = group_name;
        this.products = products;
    }

    public String getGroup_name() {
        return group_name;
    }

    public List<ProductMember> getProducts() {
        return products;
    }
}
