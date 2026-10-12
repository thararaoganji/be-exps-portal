package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApprovalRequestTest {

    @Test
    void testGettersAndSetters() {
        ApprovalRequest request = new ApprovalRequest();

        request.setStatus(ApprovalStatus.APPROVED);
        request.setAction("reject");
        request.setComments("Looks good");
        request.setApproved(true);

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals("reject", request.getAction());
        assertEquals("Looks good", request.getComments());
        assertEquals(Boolean.TRUE, request.getApproved());
    }

    @Test
    void testGetStatusDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.PENDING);
        request.setApproved(false);
        request.setAction("reject");

        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusFromApprovedTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusFromApprovedFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusFromActionEnumValue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approved");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusFromActionApprove() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusFromActionReject() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusFromActionPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");

        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusFromActionUnknown() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");

        assertNull(request.getStatus());
    }

    @Test
    void testGetStatusDefaultNull() {
        ApprovalRequest request = new ApprovalRequest();

        assertNull(request.getStatus());
    }
}