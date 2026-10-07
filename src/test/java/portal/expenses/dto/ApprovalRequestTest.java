package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApprovalRequestTest {

    @Test
    void testGetStatusWhenStatusIsDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusWhenApprovedIsTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals(true, request.getApproved());
    }

    @Test
    void testGetStatusWhenApprovedIsFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
        assertEquals(false, request.getApproved());
    }

    @Test
    void testGetStatusWhenActionMatchesEnumDirectly() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals("APPROVED", request.getAction());
    }

    @Test
    void testGetStatusWhenActionIsApprove() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusWhenActionIsReject() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusWhenActionIsPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusWhenActionIsUnknown() {
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
    void testGettersAndSetters() {
        ApprovalRequest request = new ApprovalRequest();
        request.setComments("Looks good");
        assertEquals("Looks good", request.getComments());

        request.setAction("approve");
        assertEquals("approve", request.getAction());

        request.setApproved(true);
        assertEquals(true, request.getApproved());

        request.setStatus(ApprovalStatus.PENDING);
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }
}