package com.campus.trade.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
@Data
public class AddressDTO implements Serializable { // 用于新增/编辑地址
    private Integer id; // 更新时需要
    @NotBlank(message = "收件人姓名不能为空")
    private String receiverName;
    @NotBlank(message = "收件人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String receiverPhone;
    @NotBlank(message = "省份不能为空")
    private String province;
    @NotBlank(message = "城市不能为空")
    private String city;
    @NotBlank(message = "区/县不能为空")
    private String district;
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
    private Boolean isDefault = false; // 默认为非默认
}