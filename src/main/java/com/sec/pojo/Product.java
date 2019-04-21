package com.sec.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {
    private Integer id;

    private String name;

    private String title;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private Integer sale;

    private Integer status;

    private Timestamp startTime;

    private Timestamp endTime;

    private Timestamp createTime;

    private Timestamp updateTime;


    /**
     * 附加: 图片uri的前缀
     */
    private String ImagePrefix;


    public Product(Integer id, String name, String title, String mainImage, String subImages, String detail, BigDecimal price, Integer stock, Integer sale, Integer status, Timestamp startTime, Timestamp endTime, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.mainImage = mainImage;
        this.subImages = subImages;
        this.detail = detail;
        this.price = price;
        this.stock = stock;
        this.sale = sale;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Product() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage == null ? null : mainImage.trim();
    }

    public String getSubImages() {
        return subImages;
    }

    public void setSubImages(String subImages) {
        this.subImages = subImages == null ? null : subImages.trim();
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSale() {
        return sale;
    }

    public void setSale(Integer sale) {
        this.sale = sale;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getImagePrefix() {
        return ImagePrefix;
    }

    public void setImagePrefix(String imagePrefix) {
        ImagePrefix = imagePrefix;
    }
}