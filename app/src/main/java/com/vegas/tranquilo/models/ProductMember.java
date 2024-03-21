package com.vegas.tranquilo.models;

import java.io.Serializable;
import java.util.Comparator;

public class ProductMember implements Serializable {

    private String title;
    private int price, max_num;

    private int currentNum = 0;
    private int tot = 0;

    public ProductMember(String title, int price, int max_num) {
        this.title = title;
        this.price = price;
        this.max_num = max_num;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public int getMax_num() {
        return max_num;
    }

    //------------------------------------------------------------------


    public int getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(int currentNum) {
        this.currentNum = currentNum;
    }

    public int getTot() {
        return tot;
    }

    public void setTot(int tot) {
        this.tot = tot;
    }

    //--------------------------------------------------------------------

    public enum SortParameter {
        PRICE_ASCENDING
    }

    private static class MemberComparator implements Comparator<ProductMember> {

        private SortParameter parameter;// it could be a list

        MemberComparator(SortParameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public int compare(ProductMember t1, ProductMember t2) {

            int comparison;
            if (parameter.equals(SortParameter.PRICE_ASCENDING)) {
                comparison = t1.price - t2.price;
                return comparison;
            }
            return 0;
        }
    }


    public static Comparator<ProductMember> getComparator(SortParameter sortParameter) {
        return new MemberComparator(sortParameter);
    }

    //--------------------------------------------------------------------

}
