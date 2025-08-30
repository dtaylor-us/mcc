package us.dtaylor.mcpserver.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Asset {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true)
    private String qrCode;
    private String name;
    private String model;
    private String serialNumber;
    private String brand;
    private String manualPath; // e.g. file:/opt/manuals/abc.txt or s3://...
    private Instant installedAt;
    private String qrImagePath;  // e.g., "https://cdn.example.com/qr/QR-12345.png"
    private String assetType;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManualPath() {
        return manualPath;
    }

    public void setManualPath(String manualPath) {
        this.manualPath = manualPath;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(Instant installedAt) {
        this.installedAt = installedAt;
    }

    public String getQrImagePath() {
        return qrImagePath;
    }

    public void setQrImagePath(String qrImagePath) {
        this.qrImagePath = qrImagePath;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

}
