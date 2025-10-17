package com.uade.comedor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.comedor.service.ExternalApiService;

import lombok.RequiredArgsConstructor;

  

@RestController
@RequiredArgsConstructor
@RequestMapping("/ext")
public class ExternalApiController {



  private final ExternalApiService external;

  @GetMapping("/ping")
  public ResponseEntity<String> ping(@RequestParam String url) {
    return ResponseEntity.ok(external.getPing(url));
  }
      @PostMapping("/ping")
  public ResponseEntity<String> ping2(@RequestParam String url) {
    return ResponseEntity.ok(external.getPing2(url));
  }
}
