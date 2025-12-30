// ReturnPolicyServiceImpl.java
package com.roze.nexacommerce.returns.service.impl;

import com.roze.nexacommerce.exception.DuplicateResourceException;
import com.roze.nexacommerce.exception.ResourceNotFoundException;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyCreateRequest;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyUpdateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnPolicyResponse;
import com.roze.nexacommerce.returns.entity.ReturnPolicy;
import com.roze.nexacommerce.returns.mapper.ReturnMapper;
import com.roze.nexacommerce.returns.repository.ReturnPolicyRepository;
import com.roze.nexacommerce.returns.service.ReturnPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnPolicyServiceImpl implements ReturnPolicyService {
    
    private final ReturnPolicyRepository returnPolicyRepository;
    private final ReturnMapper returnMapper;
    
    @Override
    @Transactional
    public ReturnPolicyResponse createPolicy(ReturnPolicyCreateRequest request) {
        // Validate unique name
        if (returnPolicyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Return Policy", "name", request.getName());
        }
        
        // If this is being set as default, unset any existing default
        if (Boolean.TRUE.equals(request.getDefaultPolicy())) {
            returnPolicyRepository.findDefaultPolicy().ifPresent(existingDefault -> {
                existingDefault.setDefaultPolicy(false);
                returnPolicyRepository.save(existingDefault);
            });
        }
        
        ReturnPolicy policy = returnMapper.toEntity(request);
        ReturnPolicy savedPolicy = returnPolicyRepository.save(policy);
        
        log.info("Created return policy: {}", savedPolicy.getName());
        return returnMapper.toResponse(savedPolicy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReturnPolicyResponse getPolicyById(Long policyId) {
        ReturnPolicy policy = returnPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "id", policyId));
        return returnMapper.toResponse(policy);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReturnPolicyResponse> getAllPolicies() {
        return returnPolicyRepository.findAll().stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReturnPolicyResponse> getActivePolicies() {
        return returnPolicyRepository.findByActiveTrue().stream()
                .map(returnMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReturnPolicyResponse getDefaultPolicy() {
        ReturnPolicy policy = returnPolicyRepository.findDefaultPolicy()
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "default", "true"));
        return returnMapper.toResponse(policy);
    }
    
    @Override
    @Transactional
    public ReturnPolicyResponse updatePolicy(Long policyId, ReturnPolicyUpdateRequest request) {
        ReturnPolicy policy = returnPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "id", policyId));
        
        // Validate unique name if changed
        if (request.getName() != null && !request.getName().equals(policy.getName()) &&
            returnPolicyRepository.existsByNameAndIdNot(request.getName(), policyId)) {
            throw new DuplicateResourceException("Return Policy", "name", request.getName());
        }
        
        // If setting as default, unset existing default
        if (Boolean.TRUE.equals(request.getDefaultPolicy())) {
            returnPolicyRepository.findDefaultPolicy().ifPresent(existingDefault -> {
                if (!existingDefault.getId().equals(policyId)) {
                    existingDefault.setDefaultPolicy(false);
                    returnPolicyRepository.save(existingDefault);
                }
            });
        }
        
        returnMapper.updateEntity(request, policy);
        ReturnPolicy updatedPolicy = returnPolicyRepository.save(policy);
        
        log.info("Updated return policy: {}", updatedPolicy.getName());
        return returnMapper.toResponse(updatedPolicy);
    }
    
    @Override
    @Transactional
    public void deletePolicy(Long policyId) {
        ReturnPolicy policy = returnPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "id", policyId));
        
        // Cannot delete default policy
        if (Boolean.TRUE.equals(policy.getDefaultPolicy())) {
            throw new IllegalStateException("Cannot delete default return policy");
        }
        
        returnPolicyRepository.delete(policy);
        log.info("Deleted return policy: {}", policy.getName());
    }
    
    @Override
    @Transactional
    public ReturnPolicyResponse togglePolicyStatus(Long policyId) {
        ReturnPolicy policy = returnPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "id", policyId));
        
        policy.setActive(!policy.getActive());
        ReturnPolicy updatedPolicy = returnPolicyRepository.save(policy);
        
        log.info("Toggled return policy status: {} to {}", 
                updatedPolicy.getName(), updatedPolicy.getActive() ? "active" : "inactive");
        return returnMapper.toResponse(updatedPolicy);
    }
    
    @Override
    @Transactional
    public ReturnPolicyResponse setDefaultPolicy(Long policyId) {
        // Unset current default
        returnPolicyRepository.findDefaultPolicy().ifPresent(existingDefault -> {
            existingDefault.setDefaultPolicy(false);
            returnPolicyRepository.save(existingDefault);
        });
        
        // Set new default
        ReturnPolicy policy = returnPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Return Policy", "id", policyId));
        
        policy.setDefaultPolicy(true);
        ReturnPolicy updatedPolicy = returnPolicyRepository.save(policy);
        
        log.info("Set default return policy: {}", updatedPolicy.getName());
        return returnMapper.toResponse(updatedPolicy);
    }
}