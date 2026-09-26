package com.project.vision.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {
    
    @Id
    private String sap_id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", referencedColumnName = "id")
    private Admin admin;
    private String name;
    private String reference_photo_path;
    private String password;


    public String getSapId() {
        return sap_id;
    }

    public void setSapId(String sapId) {
        this.sap_id = sapId;
    }

    public String getAdminId() {
        return admin == null ? null : admin.getId();
    }

    public void setAdminId(String adminId) {
        if (adminId == null || adminId.isBlank()) {
            this.admin = null;
            return;
        }
        Admin assignedAdmin = new Admin();
        assignedAdmin.setId(adminId);
        this.admin = assignedAdmin;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferencePhotoPath() {
        return reference_photo_path;
    }

    public void setReferencePhotoPath(String reference_photo_path) {
        this.reference_photo_path = reference_photo_path;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
