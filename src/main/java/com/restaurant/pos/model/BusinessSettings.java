package com.restaurant.pos.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class BusinessSettings {

    private final String businessName;
    private final String branchName;
    private final String address;
    private final String phone;
    private final String tin;
    private final String vatRegNo;
    private final String birPermitNo;
    private final String posSerialNo;
    private final String machineNo;
    private final BigDecimal vatRatePercent;

    private BusinessSettings(Builder builder) {
        this.businessName = Objects.requireNonNull(builder.businessName, "businessName is required");
        this.branchName = builder.branchName == null ? "" : builder.branchName;
        this.address = builder.address == null ? "" : builder.address;
        this.phone = builder.phone == null ? "" : builder.phone;
        this.tin = builder.tin == null ? "" : builder.tin;
        this.vatRegNo = builder.vatRegNo == null ? "" : builder.vatRegNo;
        this.birPermitNo = builder.birPermitNo == null ? "" : builder.birPermitNo;
        this.posSerialNo = builder.posSerialNo == null ? "" : builder.posSerialNo;
        this.machineNo = builder.machineNo == null ? "" : builder.machineNo;
        this.vatRatePercent = Objects.requireNonNull(builder.vatRatePercent, "vatRatePercent is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .businessName(businessName)
                .branchName(branchName)
                .address(address)
                .phone(phone)
                .tin(tin)
                .vatRegNo(vatRegNo)
                .birPermitNo(birPermitNo)
                .posSerialNo(posSerialNo)
                .machineNo(machineNo)
                .vatRatePercent(vatRatePercent);
    }

    public String businessName() {
        return businessName;
    }

    public String branchName() {
        return branchName;
    }

    public String address() {
        return address;
    }

    public String phone() {
        return phone;
    }

    public String tin() {
        return tin;
    }

    public String vatRegNo() {
        return vatRegNo;
    }

    public String birPermitNo() {
        return birPermitNo;
    }

    public String posSerialNo() {
        return posSerialNo;
    }

    public String machineNo() {
        return machineNo;
    }

    public BigDecimal vatRatePercent() {
        return vatRatePercent;
    }

    public static final class Builder {
        private String businessName;
        private String branchName = "";
        private String address = "";
        private String phone = "";
        private String tin = "";
        private String vatRegNo = "";
        private String birPermitNo = "";
        private String posSerialNo = "";
        private String machineNo = "";
        private BigDecimal vatRatePercent;

        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public Builder branchName(String branchName) {
            this.branchName = branchName;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder tin(String tin) {
            this.tin = tin;
            return this;
        }

        public Builder vatRegNo(String vatRegNo) {
            this.vatRegNo = vatRegNo;
            return this;
        }

        public Builder birPermitNo(String birPermitNo) {
            this.birPermitNo = birPermitNo;
            return this;
        }

        public Builder posSerialNo(String posSerialNo) {
            this.posSerialNo = posSerialNo;
            return this;
        }

        public Builder machineNo(String machineNo) {
            this.machineNo = machineNo;
            return this;
        }

        public Builder vatRatePercent(BigDecimal vatRatePercent) {
            this.vatRatePercent = vatRatePercent;
            return this;
        }

        public BusinessSettings build() {
            return new BusinessSettings(this);
        }
    }
}
