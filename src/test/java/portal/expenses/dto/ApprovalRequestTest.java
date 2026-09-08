package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApprovalRequestTest {

    @Test
    void testGetStatus_WhenStatusIsDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);
        request.setApproved(false);
        request.setAction("reject");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromApprovedTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);
        request.setAction("reject");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals(Boolean.TRUE, request.getApproved());
    }

    @Test
    void testGetStatus_DerivedFromApprovedFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
        assertEquals(Boolean.FALSE, request.getApproved());
    }

    @Test
    void testGetStatus_DerivedFromActionEnumName() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
        assertEquals("APPROVED", request.getAction());
    }

    @Test
    void testGetStatus_DerivedFromActionApprove() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromActionReject() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromActionPending() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");

        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatus_DerivedFromActionUnknown() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("invalid_action");

        assertNull(request.getStatus());
    }

    @Test
    void testGetStatus_WhenAllFieldsAreNull() {
        ApprovalRequest request = new ApprovalRequest();

        assertNull(request.getStatus());
        assertNull(request.getAction());
        assertNull(request.getComments());
        assertNull(request.getApproved());
    }

    @Test
    void testCommentsGetterAndSetter() {
        ApprovalRequest request = new ApprovalRequest();
        request.setComments("Approval comments text");

        assertEquals("Approval comments text", request.getComments());
    }
}