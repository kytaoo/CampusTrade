package com.campus.trade.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.AddressDTO;
import com.campus.trade.entity.Address;
import com.campus.trade.mapper.AddressMapper;
import com.campus.trade.service.IAddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> listAddressesByUserId(Integer userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault) // 默认地址排在前面
                .orderByDesc(Address::getUpdatedAt)); // 最近更新的排在前面
    }

    @Override
    @Transactional // 可能涉及更新其他地址为非默认
    public Address addAddress(AddressDTO addressDTO, Integer userId) {
        Address address = new Address();
        BeanUtils.copyProperties(addressDTO, address);
        address.setUserId(userId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            // 如果新增的是默认地址，先将该用户其他地址设为非默认
            addressMapper.setAllNotDefault(userId);
        } else {
            // 如果不是默认，检查用户是否还没有地址，是则将此地址设为默认
            long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
            if (count == 0) {
                address.setIsDefault(true);
            } else {
                 address.setIsDefault(false); // 确保非默认
            }
        }
        addressMapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public Address updateAddress(AddressDTO addressDTO, Integer userId) throws Exception {
        Address existingAddress = addressMapper.selectById(addressDTO.getId());
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new Exception("地址不存在或无权修改");
        }

        BeanUtils.copyProperties(addressDTO, existingAddress);

        if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
            // 如果更新为默认地址，先将其他地址设为非默认
            addressMapper.setAllNotDefault(userId);
        }
        // 注意：这里没有处理 "将原本是默认的地址改为非默认" 后，是否需要自动设置另一个为默认的情况，
        // 简单的处理是允许用户没有默认地址。

        addressMapper.updateById(existingAddress);
        return existingAddress;
    }

    @Override
    public boolean deleteAddress(Integer addressId, Integer userId) throws Exception {
        Address existingAddress = addressMapper.selectById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new Exception("地址不存在或无权删除");
        }
        // 可选：如果删除的是默认地址，需要处理（例如将最新的地址设为默认，或允许没有默认）
        // if (Boolean.TRUE.equals(existingAddress.getIsDefault())) { ... }
        return addressMapper.deleteById(addressId) > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Integer addressId, Integer userId) throws Exception {
        Address existingAddress = addressMapper.selectById(addressId);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            throw new Exception("地址不存在或无权设置");
        }
        // 1. 将所有地址设为非默认
        addressMapper.setAllNotDefault(userId);
        // 2. 将当前地址设为默认
        existingAddress.setIsDefault(true);
        return addressMapper.updateById(existingAddress) > 0;
    }
}