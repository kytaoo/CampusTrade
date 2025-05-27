package com.campus.trade.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.AddressDTO;
import com.campus.trade.entity.Address;
import java.util.List;

public interface IAddressService extends IService<Address> {
    List<Address> listAddressesByUserId(Integer userId);
    Address addAddress(AddressDTO addressDTO, Integer userId);
    Address updateAddress(AddressDTO addressDTO, Integer userId) throws Exception;
    boolean deleteAddress(Integer addressId, Integer userId) throws Exception;
    boolean setDefaultAddress(Integer addressId, Integer userId) throws Exception;
}