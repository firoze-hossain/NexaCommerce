// ReturnPolicyService.java
package com.roze.nexacommerce.returns.service;

import com.roze.nexacommerce.returns.dto.request.ReturnPolicyCreateRequest;
import com.roze.nexacommerce.returns.dto.request.ReturnPolicyUpdateRequest;
import com.roze.nexacommerce.returns.dto.response.ReturnPolicyResponse;

import java.util.List;

public interface ReturnPolicyService {
    ReturnPolicyResponse createPolicy(ReturnPolicyCreateRequest request);
    ReturnPolicyResponse getPolicyById(Long policyId);
    List<ReturnPolicyResponse> getAllPolicies();
    List<ReturnPolicyResponse> getActivePolicies();
    ReturnPolicyResponse getDefaultPolicy();
    ReturnPolicyResponse updatePolicy(Long policyId, ReturnPolicyUpdateRequest request);
    void deletePolicy(Long policyId);
    ReturnPolicyResponse togglePolicyStatus(Long policyId);
    ReturnPolicyResponse setDefaultPolicy(Long policyId);
}