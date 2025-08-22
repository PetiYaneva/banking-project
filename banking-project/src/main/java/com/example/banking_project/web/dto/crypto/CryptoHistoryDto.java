package com.example.banking_project.web.dto.crypto;

import java.util.List;
public record CryptoHistoryDto(String id, String vsCurrency, List<CryptoHistoryPointDto> points) {}
