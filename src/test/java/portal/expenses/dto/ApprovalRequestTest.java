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
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setComments("Looks good");
        assertEquals("Looks good", request.getComments());

        request.setAction("approve");
        assertEquals("approve", request.getAction());

        request.setApproved(true);
        assertEquals(Boolean.TRUE, request.getApproved());
    }

    @Test
    void testGetStatusDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.REJECTED);
        request.setApproved(true);
        request.setAction("approve");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
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
    void testGetStatusFromActionEnumMatch() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setAction("rejected");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());

        request.setAction("Pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusFromActionSwitchCases() {
        ApprovalRequest request = new ApprovalRequest();

        request.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());

        request.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());

        request.setAction("unknown_action");
        assertNull(request.getStatus());
    }

    @Test
    void testGetStatusWhenAllNull() {
        ApprovalRequest request = new ApprovalRequest();
        assertNull(request.getStatus());
    }
}