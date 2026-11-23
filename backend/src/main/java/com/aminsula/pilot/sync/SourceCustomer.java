package com.aminsula.pilot.sync;

public class SourceCustomer {

    private final String code;
    private final String name;
    private final String address;

    public SourceCustomer(String code, String name, String address) {
        this.code = code;
        this.name = name;
        this.address = address;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
