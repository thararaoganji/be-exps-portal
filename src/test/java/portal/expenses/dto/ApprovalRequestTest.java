package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRequestTest {

    @Test
    void testGettersAndSetters() {
        ApprovalRequest request = new ApprovalRequest();

        request.setComments("Looks good");
        assertEquals("Looks good", request.getComments());

        request.setAction("approve");
        assertEquals("approve", request.getAction());

        request.setApproved(true);
        assertTrue(request.getApproved());

        request.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenStatusExplicitlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.REJECTED);
        request.setApproved(true); // Should be ignored because status is non-null

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromApprovedBoolean() {
        ApprovalRequest requestTrue = new ApprovalRequest();
        requestTrue.setApproved(true);
        assertEquals(ApprovalStatus.APPROVED, requestTrue.getStatus());

        ApprovalRequest requestFalse = new ApprovalRequest();
        requestFalse.setApproved(false);
        assertEquals(ApprovalStatus.REJECTED, requestFalse.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromActionEnumName() {
        ApprovalRequest requestApproved = new ApprovalRequest();
        requestApproved.setAction("APPROVED");
        assertEquals(ApprovalStatus.APPROVED, requestApproved.getStatus());

        ApprovalRequest requestRejected = new ApprovalRequest();
        requestRejected.setAction("rejected");
        assertEquals(ApprovalStatus.REJECTED, requestRejected.getStatus());

        ApprovalRequest requestPending = new ApprovalRequest();
        requestPending.setAction("PENDING");
        assertEquals(ApprovalStatus.PENDING, requestPending.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromActionCommonNames() {
        ApprovalRequest requestApprove = new ApprovalRequest();
        requestApprove.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, requestApprove.getStatus());

        ApprovalRequest requestReject = new ApprovalRequest();
        requestReject.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, requestReject.getStatus());

        ApprovalRequest requestPending = new ApprovalRequest();
        requestPending.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, requestPending.getStatus());

        ApprovalRequest requestUnknown = new ApprovalRequest();
        requestUnknown.setAction("unknown_action");
        assertNull(requestUnknown.getStatus());
    }

    @Test
    void testGetStatus_AllFieldsNull() {
        ApprovalRequest request = new ApprovalRequest();
        assertNull(request.getStatus());
    }
}