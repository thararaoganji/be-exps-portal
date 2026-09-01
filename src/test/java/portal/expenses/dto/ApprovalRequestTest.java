package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApprovalRequestTest {

    @Test
    void testGettersAndSetters() {
        ApprovalRequest request = new ApprovalRequest();

        request.setComments("Some comment");
        assertEquals("Some comment", request.getComments());

        request.setAction("APPROVE");
        assertEquals("APPROVE", request.getAction());

        request.setApproved(true);
        assertEquals(Boolean.TRUE, request.getApproved());

        request.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusWhenStatusIsSetDirectly() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromApprovedTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromApprovedFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionMatchingEnumName() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setAction("rejected");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());

        request.setAction("PENDING");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionFallbackApprove() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionFallbackReject() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionFallbackPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionFallbackUnknown() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");
        assertNull(request.getStatus());
    }

    @Test
    void testGetStatusWhenAllNull() {
        ApprovalRequest request = new ApprovalRequest();
        assertNull(request.getStatus());
    }
}