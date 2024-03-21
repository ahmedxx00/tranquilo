package com.vegas.tranquilo.models;

public class SelectedRowModel {
    private String rowTitle;
    private int rowSelectedNum;
    private int rowTotalPrice;

    public SelectedRowModel(String rowTitle, int rowSelectedNum, int rowTotalPrice) {
        this.rowTitle = rowTitle;
        this.rowSelectedNum = rowSelectedNum;
        this.rowTotalPrice = rowTotalPrice;
    }

    public String getRowTitle() {
        return rowTitle;
    }

    public int getRowSelectedNum() {
        return rowSelectedNum;
    }

    public int getRowTotalPrice() {
        return rowTotalPrice;
    }
}
