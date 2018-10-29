package com.susankya.abhinav.myrealmapplication;

public class DueHistoryDataModel {
    public String userId, userName;
    public int dueAmount;

    public DueHistoryDataModel(String userId, String userName, int dueAmount) {
        this.userId = userId;
        this.userName = userName;
        this.dueAmount = dueAmount;
    }
}
