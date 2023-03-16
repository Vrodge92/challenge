package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.OverDraftException;
import com.dws.challenge.service.AccountsService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransactionControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	public void testTransferBalance() throws Exception {

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1234\",\"balance\":100}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1235\",\"balance\":200}")).andExpect(status().isCreated());

		this.mockMvc
				.perform(post("/v1/transaction/amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-1234\",\"accountToId\":\"Id-1235\",\"amount\":10}"))
				.andExpect(status().isAccepted());

		Account account = accountsService.getAccount("Id-1234");
		assertThat(account.getAccountId()).isEqualTo("Id-1234");
		assertThat(account.getBalance()).isEqualByComparingTo("90");

	}

	@Test
	public void testTransferNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1234\",\"balance\":100}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1235\",\"balance\":200}")).andExpect(status().isCreated());

		this.mockMvc
				.perform(post("/v1/transaction/amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-1234\",\"accountToId\":\"Id-1235\",\"amount\":-1}"))
				.andExpect(status().isBadRequest());

		Account account = accountsService.getAccount("Id-1234");
		assertThat(account.getAccountId()).isEqualTo("Id-1234");
		assertThat(account.getBalance()).isEqualByComparingTo("100");
	}

	@Test
	public void testOverdraftBalance() throws OverDraftException, AccountNotExistException, Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1234\",\"balance\":100}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1235\",\"balance\":200}")).andExpect(status().isCreated());

		this.mockMvc
				.perform(post("/v1/transaction/amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-1234\",\"accountToId\":\"Id-1235\",\"amount\":101}"))
				.andExpect(status().isNotModified());
	}

	@Test
	public void testTransferBalanceWithEmptyFromId() throws Exception {

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1234\",\"balance\":100}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1235\",\"balance\":200}")).andExpect(status().isCreated());

		this.mockMvc
				.perform(post("/v1/transaction/amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-123412\",\"accountToId\":\"Id-1235\",\"amount\":10}"))
				.andExpect(status().isNotModified());

	}

	@Test
	public void testTransferBalanceWithEmptyToId() throws Exception {

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1234\",\"balance\":100}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-1235\",\"balance\":200}")).andExpect(status().isCreated());

		this.mockMvc
				.perform(post("/v1/transaction/amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"accountFromId\":\"Id-1234\",\"accountToId\":\"Id-123512\",\"amount\":10}"))
				.andExpect(status().isNotModified());

	}
}
