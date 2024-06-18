package com.example.ebank.dtos;

import lombok.Data;

@Data
public class BankAccountDTO {
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String type;
}
