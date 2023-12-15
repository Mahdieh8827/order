package com.payment.service;

import com.payment.entity.Merchant;
import com.payment.repository.MerchantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    public void processMerchant(Merchant merchant)
    {
        merchantRepository.save(merchant);

    }
}
