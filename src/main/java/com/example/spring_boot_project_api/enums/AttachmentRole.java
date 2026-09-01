package com.example.spring_boot_project_api.enums;

/**
 * Optional semantic role of an attachment on its owning entity (e.g. a hotel).
 * Stored as a free-form string on the junction row so adding new roles does not
 * require a schema change, but kept as an enum for convenient constants.
 */
public enum AttachmentRole {
    COVER,
    GALLERY,
    DOCUMENT,
    PROFILE,
    LOGO,
    MENU;

    public static boolean isValid(String value) {
        if (value == null) {
            return true;
        }
        for (AttachmentRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
