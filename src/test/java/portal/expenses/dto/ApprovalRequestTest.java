package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalRequestTest {

    @Test
    void testGetStatusWhenStatusIsDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromApprovedTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertTrue(request.getApproved());
    }

    @Test
    void testGetStatusDerivedFromApprovedFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
        assertFalse(request.getApproved());
    }

    @Test
    void testGetStatusDerivedFromActionValueOfMatch() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals("APPROVED", request.getAction());
    }

    @Test
    void testGetStatusDerivedFromActionApprove() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionReject() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");

        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionDefaultNull() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");

        assertNull(request.getStatus());
    }

    @Test
    void testGetStatusWhenAllFieldsNull() {
        ApprovalRequest request = new ApprovalRequest();

        assertNull(request.getStatus());
    }

    @Test
    void testCommentsGetterAndSetter() {
        ApprovalRequest request = new ApprovalRequest();
        request.setComments("Looks good to me");

        assertEquals("Looks good to me", request.getComments());
    }
}