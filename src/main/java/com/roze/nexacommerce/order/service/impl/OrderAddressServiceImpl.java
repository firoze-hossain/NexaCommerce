//package com.roze.nexacommerce.order.service.impl;
//
//import com.roze.nexacommerce.common.address.entity.Address;
//import com.roze.nexacommerce.common.address.service.AddressService;
//import com.roze.nexacommerce.order.entity.BillingAddress;
//import com.roze.nexacommerce.order.entity.ShippingAddress;
//import com.roze.nexacommerce.order.service.OrderAddressService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class OrderAddressServiceImpl implements OrderAddressService {
//    private final AddressService addressService;
//
//    @Override
//    public ShippingAddress createShippingAddress(Long addressId) {
//        Address commonAddress = addressService.getAddressEntityById(addressId);
//
//        return ShippingAddress.builder()
//                .fullName(commonAddress.getContactName())
//                .phone(commonAddress.getContactPhone())
//                .addressLine1(commonAddress.getStreet())
//                .addressLine2(commonAddress.getLandmark())
//                .city(commonAddress.getCity())
//                .state(commonAddress.getState())
//                .postalCode(commonAddress.getZipCode())
//                .country(commonAddress.getCountry())
//                .instructions("") // You can add this field to your common address if needed
//                .build();
//    }
//
//    @Override
//    public BillingAddress createBillingAddress(Long addressId) {
//        Address commonAddress = addressService.getAddressEntityById(addressId);
//
//        return BillingAddress.builder()
//                .fullName(commonAddress.getContactName())
//                .phone(commonAddress.getContactPhone())
//                .addressLine1(commonAddress.getStreet())
//                .addressLine2(commonAddress.getLandmark())
//                .city(commonAddress.getCity())
//                .state(commonAddress.getState())
//                .postalCode(commonAddress.getZipCode())
//                .country(commonAddress.getCountry())
//                .sameAsShipping(false)
//                .build();
//    }
//
//    @Override
//    public BillingAddress createBillingAddressFromShipping(ShippingAddress shippingAddress) {
//        return BillingAddress.builder()
//                .fullName(shippingAddress.getFullName())
//                .phone(shippingAddress.getPhone())
//                .addressLine1(shippingAddress.getAddressLine1())
//                .addressLine2(shippingAddress.getAddressLine2())
//                .city(shippingAddress.getCity())
//                .state(shippingAddress.getState())
//                .postalCode(shippingAddress.getPostalCode())
//                .country(shippingAddress.getCountry())
//                .sameAsShipping(true)
//                .build();
//    }
//}
