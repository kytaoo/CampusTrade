package com.campus.trade.vo; // VO 包
import lombok.Data;
import java.io.Serializable;
@Data
public class AddressVO implements Serializable { // 地址展示用
    private Integer id;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Boolean isDefault;
}