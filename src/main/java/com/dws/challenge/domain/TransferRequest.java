package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {

	@NotEmpty
	private String accountFromId;

	@NotEmpty
	private String accountToId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal amount;

	@JsonCreator
	public TransferRequest(@NotNull @JsonProperty("accountFromId") String accountFromId,
			@NotNull @JsonProperty("accountToId") String accountToId,
			@NotNull @Min(value = 0, message = "Transfer amount can not be less than zero") @JsonProperty("amount") BigDecimal amount) {
		super();
		this.accountFromId = accountFromId;
		this.accountToId = accountToId;
		this.amount = amount;
	}

	@JsonCreator
	public TransferRequest() {
		super();
	}
}
