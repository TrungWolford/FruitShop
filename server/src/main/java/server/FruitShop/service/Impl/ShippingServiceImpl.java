package server.FruitShop.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.FruitShop.dto.request.Shipping.ShippingRequest;
import server.FruitShop.dto.response.Shipping.ShippingResponse;
import server.FruitShop.entity.Account;
import server.FruitShop.entity.Shipping;
import server.FruitShop.repository.AccountRepository;
import server.FruitShop.repository.ShippingRepository;
import server.FruitShop.service.ShippingService;
import server.FruitShop.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShippingServiceImpl implements ShippingService {

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public ShippingResponse createShipping(ShippingRequest request) {
        try {
            Shipping shipping = new Shipping();
            shipping.setReceiverName(request.getReceiverName());
            shipping.setReceiverPhone(request.getReceiverPhone());
            shipping.setReceiverAddress(request.getReceiverAddress());
            shipping.setCity(request.getCity());
            shipping.setShipperName(request.getShipperName());
            shipping.setShippingFee(request.getShippingFee());
            shipping.setShippedAt(request.getShippedAt());
            // Set default status = 0 (Chờ xác nhận) when creating new shipping
            shipping.setStatus(0);

            if (request.getAccountId() != null) {
                Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
                if (accountOpt.isPresent()) {
                    shipping.setAccount(accountOpt.get());
                } else {
                    throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
                }
            }

            Shipping saved = shippingRepository.save(shipping);
            return ShippingResponse.fromEntity(saved);
        } catch (Exception e) {
            throw new RuntimeException("Error creating shipping: " + e.getMessage(), e);
        }
    }

    @Override
    public ShippingResponse getShippingById(String shippingId) {
        Optional<Shipping> shippingOpt = shippingRepository.findById(shippingId);
        if (shippingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Shipping not found with id: " + shippingId);
        }
        return ShippingResponse.fromEntity(shippingOpt.get());
    }

    @Override
    public ShippingResponse updateShipping(String shippingId, ShippingRequest request) {
        Optional<Shipping> shippingOpt = shippingRepository.findById(shippingId);
        if (shippingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Shipping not found with id: " + shippingId);
        }

        Shipping shipping = shippingOpt.get();
        shipping.setReceiverName(request.getReceiverName());
        shipping.setReceiverPhone(request.getReceiverPhone());
        shipping.setReceiverAddress(request.getReceiverAddress());
        shipping.setCity(request.getCity());
        shipping.setShipperName(request.getShipperName());
        shipping.setShippingFee(request.getShippingFee());
        shipping.setShippedAt(request.getShippedAt());
        shipping.setStatus(request.getStatus());

        if (request.getAccountId() != null) {
            Optional<Account> accountOpt = accountRepository.findById(request.getAccountId());
            if (accountOpt.isPresent()) {
                shipping.setAccount(accountOpt.get());
            }
        }

        Shipping saved = shippingRepository.save(shipping);
        return ShippingResponse.fromEntity(saved);
    }

    @Override
    public void deleteShipping(String shippingId) {
        if (!shippingRepository.existsById(shippingId)) {
            throw new ResourceNotFoundException("Shipping not found with id: " + shippingId);
        }
        shippingRepository.deleteById(shippingId);
    }

    @Override
    public List<ShippingResponse> getAllShippings() {
        try {
            List<Shipping> shippings = shippingRepository.findAll();
            if (shippings == null || shippings.isEmpty()) {
                return List.of(); // Return empty list instead of null
            }
            return shippings.stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching all shippings: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }
    }

    @Override
    public Page<ShippingResponse> getAllShippingsPaginated(Pageable pageable) {
        try {
            Page<Shipping> shippingsPage = shippingRepository.findAll(pageable);
            if (shippingsPage == null || shippingsPage.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0); // Return empty page
            }
            List<ShippingResponse> responses = shippingsPage.getContent().stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
            return new PageImpl<>(responses, pageable, shippingsPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("Error fetching shippings paginated: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(List.of(), pageable, 0); // Return empty page on error
        }
    }

    @Override
    public List<ShippingResponse> getShippingsByAccountId(String accountId) {
        try {
            List<Shipping> shippings = shippingRepository.findByAccountAccountId(accountId);
            if (shippings == null || shippings.isEmpty()) {
                return List.of(); // Return empty list
            }
            return shippings.stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching shippings for accountId " + accountId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list on error
        }
    }

    @Override
    public ShippingResponse updateShippingStatus(String shippingId, int status) {
        Optional<Shipping> shippingOpt = shippingRepository.findById(shippingId);
        if (shippingOpt.isEmpty()) {
            throw new ResourceNotFoundException("Shipping not found with id: " + shippingId);
        }

        Shipping shipping = shippingOpt.get();
        shipping.setStatus(status);
        
        Shipping saved = shippingRepository.save(shipping);
        return ShippingResponse.fromEntity(saved);
    }

    @Override
    public Page<ShippingResponse> getShippingsByStatus(int status, Pageable pageable) {
        try {
            Page<Shipping> shippingsPage = shippingRepository.findByStatus(status, pageable);
            if (shippingsPage == null || shippingsPage.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            List<ShippingResponse> responses = shippingsPage.getContent().stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
            return new PageImpl<>(responses, pageable, shippingsPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("Error fetching shippings by status " + status + ": " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public Page<ShippingResponse> searchShippings(String keyword, Pageable pageable) {
        try {
            // Search by shippingId, receiverName, or receiverPhone
            Page<Shipping> shippingsPage = shippingRepository.findByShippingIdContainingIgnoreCaseOrReceiverNameContainingIgnoreCaseOrReceiverPhoneContaining(
                    keyword, keyword, keyword, pageable);
            
            if (shippingsPage == null || shippingsPage.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            
            List<ShippingResponse> responses = shippingsPage.getContent().stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(responses, pageable, shippingsPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("Error searching shippings with keyword '" + keyword + "': " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public Page<ShippingResponse> searchAndFilterShippings(String keyword, Integer status, Pageable pageable) {
        try {
            Page<Shipping> shippingsPage;
            
            if (keyword != null && !keyword.trim().isEmpty() && status != null) {
                // Both search and filter
                shippingsPage = shippingRepository.findByShippingIdContainingIgnoreCaseAndStatusOrReceiverNameContainingIgnoreCaseAndStatusOrReceiverPhoneContainingAndStatus(
                        keyword, status, keyword, status, keyword, status, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                // Only search
                shippingsPage = shippingRepository.findByShippingIdContainingIgnoreCaseOrReceiverNameContainingIgnoreCaseOrReceiverPhoneContaining(
                        keyword, keyword, keyword, pageable);
            } else if (status != null) {
                // Only filter
                shippingsPage = shippingRepository.findByStatus(status, pageable);
            } else {
                // No filter or search
                shippingsPage = shippingRepository.findAll(pageable);
            }
            
            if (shippingsPage == null || shippingsPage.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            
            List<ShippingResponse> responses = shippingsPage.getContent().stream()
                    .map(ShippingResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(responses, pageable, shippingsPage.getTotalElements());
        } catch (Exception e) {
            System.err.println("Error searching and filtering shippings: " + e.getMessage());
            e.printStackTrace();
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }
}
