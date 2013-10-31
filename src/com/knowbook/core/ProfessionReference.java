package com.knowbook.core;

public interface ProfessionReference {

    String getCurrentFieldName();

    String getCurrentBranchName();

    String getCurrentProfessionName();

    boolean readNextProfession();

    void start();

    void finish();

}
