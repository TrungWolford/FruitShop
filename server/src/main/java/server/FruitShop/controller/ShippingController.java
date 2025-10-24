package server.FruitShop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.dto.response.Shipping.ShippingResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Shipping;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.ShippingRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "*")
public class ShippingController {

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("")
    public ResponseEntity<List<ShippingResponse>> getAll() {
        List<ShippingResponse> result = shippingRepository.findAll()
                .stream().map(ShippingResponse::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ShippingResponse>> getByAccount(@PathVariable String accountId) {
    List<ShippingResponse> result = shippingRepository.findByAccountAccountId(accountId)
        .stream()
        .map(ShippingResponse::fromEntity)
        .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> getById(@PathVariable String shippingId) {
        Optional<Shipping> opt = shippingRepository.findById(shippingId);
        return opt.map(s -> ResponseEntity.ok(ShippingResponse.fromEntity(s))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("")
    public ResponseEntity<ShippingResponse> create(@RequestBody ShippingRequest request) {
        try {
            Shipping s = new Shipping();
            s.setReceiverName(request.getReceiverName());
            s.setReceiverPhone(request.getReceiverPhone());
            s.setReceiverAddress(request.getReceiverAddress());
            s.setCity(request.getCity());
            s.setShippingFee(request.getShippingFee());
            s.setShippedAt(request.getShippedAt());
            s.setStatus(request.getStatus());

            if (request.getAccountId() != null) {
                Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
                accountOpt.ifPresent(s::setAccount);
            }

            Shipping saved = shippingRepository.save(s);
            return ResponseEntity.ok(ShippingResponse.fromEntity(saved));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{shippingId}")
    public ResponseEntity<ShippingResponse> update(@PathVariable String shippingId, @RequestBody ShippingRequest request) {
        Optional<Shipping> opt = shippingRepository.findById(shippingId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Shipping s = opt.get();
        s.setReceiverName(request.getReceiverName());
        s.setReceiverPhone(request.getReceiverPhone());
        s.setReceiverAddress(request.getReceiverAddress());
        s.setCity(request.getCity());
        s.setShippingFee(request.getShippingFee());
        s.setShippedAt(request.getShippedAt());
        s.setStatus(request.getStatus());

        if (request.getAccountId() != null) {
            Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
            accountOpt.ifPresent(s::setAccount);
        }

        Shipping saved = shippingRepository.save(s);
        return ResponseEntity.ok(ShippingResponse.fromEntity(saved));
    }

    @DeleteMapping("/{shippingId}")
    public ResponseEntity<Void> delete(@PathVariable String shippingId) {
        shippingRepository.deleteById(shippingId);
        return ResponseEntity.ok().build();
    }
}
