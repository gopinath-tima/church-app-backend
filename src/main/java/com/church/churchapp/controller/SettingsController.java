package com.church.churchapp.controller;

import com.church.churchapp.entity.SystemSetting;
import com.church.churchapp.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SystemSettingRepository settingsRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_PLUS_ADMIN')")
    public List<SystemSetting> getSettings() {
        List<SystemSetting> list = settingsRepository.findAll();
        if (list.isEmpty()) {
            List<SystemSetting> defaults = Arrays.asList(
                new SystemSetting("church_name", "Grace Community Church", "General", "The public name of the church organization"),
                new SystemSetting("timezone", "UTC-5 (EST)", "General", "Default timezone for events and logs"),
                new SystemSetting("currency", "USD ($)", "General", "Default currency denomination"),
                new SystemSetting("session_timeout", "30", "Security", "Inactivity timeout in minutes before logout"),
                new SystemSetting("max_login_attempts", "5", "Security", "Maximum login attempts before lock"),
                new SystemSetting("mfa_enabled", "false", "Security", "Enforce Multi-Factor Authentication"),
                new SystemSetting("email_notifications", "true", "Communications", "Send system transaction notices via Email"),
                new SystemSetting("sms_notifications", "false", "Communications", "Send automated updates via SMS"),
                new SystemSetting("announcement_banner", "Welcome to our newly updated Church Administration deck!", "Communications", "Public system banner text")
            );
            settingsRepository.saveAll(defaults);
            return defaults;
        }
        return list;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('SUPER_PLUS_ADMIN')")
    public List<SystemSetting> updateSettings(@RequestBody Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            SystemSetting setting = settingsRepository.findById(entry.getKey())
                .orElse(new SystemSetting(entry.getKey(), entry.getValue(), "Custom", "Dynamic Configuration Setting"));
            setting.setSettingValue(entry.getValue());
            settingsRepository.save(setting);
        }
        return settingsRepository.findAll();
    }
}
