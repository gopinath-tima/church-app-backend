package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSetting {

    @Id
    private String settingKey;

    @Column(name = "setting_value", length = 1000)
    private String settingValue;

    private String category;
    private String description;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String category, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.category = category;
        this.description = description;
    }
}
