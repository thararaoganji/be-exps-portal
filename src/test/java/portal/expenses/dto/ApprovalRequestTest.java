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

        request.setAction("approve");
        assertEquals("approve", request.getAction());

        request.setComments("Looks good");
        assertEquals("Looks good", request.getComments());

        request.setApproved(Boolean.TRUE);
        assertEquals(Boolean.TRUE, request.getApproved());
    }

    @Test
    void testGetStatusDirectStatus() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.REJECTED);
        request.setApproved(true);
        request.setAction("pending");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusFromApprovedBoolean() {
        ApprovalRequest requestTrue = new ApprovalRequest();
        requestTrue.setApproved(true);
        assertEquals(ApprovalStatus.APPROVED, requestTrue.getStatus());

        ApprovalRequest requestFalse = new ApprovalRequest();
        requestFalse.setApproved(false);
        assertEquals(ApprovalStatus.REJECTED, requestFalse.getStatus());
    }

    @Test
    void testGetStatusFromActionEnumName() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        ApprovalRequest requestLower = new ApprovalRequest();
        requestLower.setAction("rejected");
        assertEquals(ApprovalStatus.REJECTED, requestLower.getStatus());
    }

    @Test
    void testGetStatusFromActionFallbackMapping() {
        ApprovalRequest reqApprove = new ApprovalRequest();
        reqApprove.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, reqApprove.getStatus());

        ApprovalRequest reqReject = new ApprovalRequest();
        reqReject.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, reqReject.getStatus());

        ApprovalRequest reqPending = new ApprovalRequest();
        reqPending.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, reqPending.getStatus());

        ApprovalRequest reqUnknown = new ApprovalRequest();
        reqUnknown.setAction("invalid_action");
        assertNull(reqUnknown.getStatus());
    }

    @Test
    void testGetStatusAllNull() {
        ApprovalRequest request = new ApprovalRequest();
        assertNull(request.getStatus());
    }
}