package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.domain.TransferResult;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.AccountsService;

import lombok.SneakyThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransactionTest {

	@Autowired
	AccountsService accountsService;

	/**
	 * This test added to check multiple asyn call using executor service to check
	 * thread safety for transferBalance method so user will get notify with there
	 * actual transaction
	 * 
	 * @throws Exception
	 */
	@Test
	@SneakyThrows
	public void testTransferBalanceWithMultipleTrread() throws Exception {
		Account account1 = new Account("Id-1230");
		account1.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account1);

		assertThat(this.accountsService.getAccount("Id-1230")).isEqualTo(account1);

		Account account2 = new Account("Id-1240");
		account2.setBalance(new BigDecimal(50));
		this.accountsService.createAccount(account2);
		assertThat(this.accountsService.getAccount("Id-1240")).isEqualTo(account2);

		TransferRequest transferRequest = new TransferRequest("Id-1230", "Id-1240", BigDecimal.valueOf(10));

		int numberOfThreads = 10;
		ExecutorService service = Executors.newFixedThreadPool(10);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++) {
			service.execute(() -> {
				accountsService.transferBalances(transferRequest);
				latch.countDown();
			});
		}
		Account account3 = new Account("Id-12411");
		account3.setBalance(new BigDecimal(10));
		this.accountsService.createAccount(account3);
		assertThat(this.accountsService.getAccount("Id-12411")).isEqualTo(account3);

		TransferRequest transferRequest1 = new TransferRequest("Id-1230", "Id-12411", BigDecimal.valueOf(10));
		for (int i = 0; i < numberOfThreads; i++) {
			service.execute(() -> {
				accountsService.transferBalances(transferRequest1);
				latch.countDown();
			});
		}
	}
}
