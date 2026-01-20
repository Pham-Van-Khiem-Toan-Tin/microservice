package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.constants.Constants;
import com.ecommerce.paymentservice.dto.response.ApiResponse;
import com.ecommerce.paymentservice.dto.response.TransactionDto;
import com.ecommerce.paymentservice.dto.response.WalletDto;
import com.ecommerce.paymentservice.service.SeaPayService;
import com.ecommerce.paymentservice.service.WalletService;
import com.ecommerce.paymentservice.utils.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    @Autowired
    private WalletService walletService;
    @Autowired
    private SeaPayService seaPayService;
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getMyBalance() {
        BigDecimal balance = walletService.getBalanceByUserId();
        return new ResponseEntity<>(balance, HttpStatus.OK);
    }
    @GetMapping("/me/transactions")
    public Page<TransactionDto> getMyTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = AuthenticationUtils.getUserId();
        return walletService.getTransactionHistory(userId, page, size);
    }
    @GetMapping("/me")
    public WalletDto getMyWallet() {
        return walletService.getWalletInfo();
    }


}
