package com.dws.challenge.web;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.TransactionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * This controller will provide transaction between account also validate fields
 * and return custom exception
 * 
 * @author VRodge
 *
 */
@RestController
@RequestMapping("/v1/transaction")
@Slf4j
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	@PostMapping(path = "/amount", consumes = { "application/json" })
	public ResponseEntity<Object> transferMoney(@Valid @RequestBody TransferRequest request) {
		log.info("Transfer Request {}", request);
		try {
			CompletableFuture<TransferResult> result = transactionService.transferBalances(request);
			return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
		} catch (AccountNotExistException | OverDraftException | NullPointerException e) {
			return new ResponseEntity<>(e, HttpStatus.NOT_MODIFIED);
		}
	}
}
