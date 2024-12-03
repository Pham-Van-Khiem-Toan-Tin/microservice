package com.ecommerce.payentservice.service.impl;

import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.entity.BillingEntity;
import com.ecommerce.payentservice.form.UpdateBillingForm;
import com.ecommerce.payentservice.mapper.PaymentMapper;
import com.ecommerce.payentservice.repository.PaymentRepository;
import com.ecommerce.payentservice.service.PaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    PaymentMapper paymentMapper;
    @Override
    public PaymentDTO getProfile(String userId) {
        BillingEntity payment = paymentRepository.findByCustomerId(userId);
        if (payment == null)
            payment = new BillingEntity();
        return paymentMapper.toPaymentDTO(payment);
    }

    @Override
    public PaymentDTO updateProfile(UpdateBillingForm form) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        BillingEntity billing = paymentRepository.findByCustomerId(userId);
        if (billing == null) {
            BillingEntity billingEntity = new BillingEntity();
            BeanUtils.copyProperties(form, billingEntity, getNullPropertyNames(form));
            billingEntity.setCustomerId(userId);
            billingEntity.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(billingEntity);
            return paymentMapper.toPaymentDTO(billingEntity);
        } else {
            BeanUtils.copyProperties(form, billing, getNullPropertyNames(form));
            billing.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(billing);
            return paymentMapper.toPaymentDTO(billing);
        }
    }
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        return emptyNames.toArray(new String[0]);
    }
}
