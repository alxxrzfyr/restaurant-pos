package com.restaurant.pos.service;

import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.BusinessSettings;
import com.restaurant.pos.repository.SettingsRepository;

import java.math.BigDecimal;

public final class SettingsService {

    private static final String KEY_BUSINESS_NAME = "business.name";
    private static final String KEY_BRANCH_NAME = "business.branchName";
    private static final String KEY_ADDRESS = "business.address";
    private static final String KEY_PHONE = "business.phone";
    private static final String KEY_TIN = "business.tin";
    private static final String KEY_VAT_REG_NO = "business.vatRegNo";
    private static final String KEY_BIR_PERMIT_NO = "business.birPermitNo";
    private static final String KEY_POS_SERIAL_NO = "business.posSerialNo";
    private static final String KEY_MACHINE_NO = "business.machineNo";
    private static final String KEY_VAT_RATE = "business.vatRatePercent";

    private final SettingsRepository settingsRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public SettingsService(SettingsRepository settingsRepository, AuthService authService, AuditService auditService) {
        this.settingsRepository = settingsRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public BusinessSettings load() {
        return BusinessSettings.builder()
                .businessName(settingsRepository.get(KEY_BUSINESS_NAME).orElse(""))
                .branchName(settingsRepository.get(KEY_BRANCH_NAME).orElse(""))
                .address(settingsRepository.get(KEY_ADDRESS).orElse(""))
                .phone(settingsRepository.get(KEY_PHONE).orElse(""))
                .tin(settingsRepository.get(KEY_TIN).orElse(""))
                .vatRegNo(settingsRepository.get(KEY_VAT_REG_NO).orElse(""))
                .birPermitNo(settingsRepository.get(KEY_BIR_PERMIT_NO).orElse(""))
                .posSerialNo(settingsRepository.get(KEY_POS_SERIAL_NO).orElse(""))
                .machineNo(settingsRepository.get(KEY_MACHINE_NO).orElse(""))
                .vatRatePercent(new BigDecimal(settingsRepository.get(KEY_VAT_RATE).orElse("12.00")))
                .build();
    }

    public BigDecimal currentVatRatePercent() {
        return new BigDecimal(settingsRepository.get(KEY_VAT_RATE).orElse("12.00"));
    }

    public void save(BusinessSettings settings, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        settingsRepository.set(KEY_BUSINESS_NAME, settings.businessName());
        settingsRepository.set(KEY_BRANCH_NAME, settings.branchName());
        settingsRepository.set(KEY_ADDRESS, settings.address());
        settingsRepository.set(KEY_PHONE, settings.phone());
        settingsRepository.set(KEY_TIN, settings.tin());
        settingsRepository.set(KEY_VAT_REG_NO, settings.vatRegNo());
        settingsRepository.set(KEY_BIR_PERMIT_NO, settings.birPermitNo());
        settingsRepository.set(KEY_POS_SERIAL_NO, settings.posSerialNo());
        settingsRepository.set(KEY_MACHINE_NO, settings.machineNo());
        settingsRepository.set(KEY_VAT_RATE, settings.vatRatePercent().toPlainString());
        auditService.record(AuditEventType.SETTINGS_CHANGED, actingUserId, actingUsername, "business settings updated");
    }
}
