package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRequestTest {

    @Test
    void testGetStatus_WhenStatusIsDirectlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenApprovedIsTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);

        assertTrue(request.getApproved());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenApprovedIsFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);

        assertEquals(Boolean.FALSE, request.getApproved());
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenActionMatchesEnumDirectly() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");

        assertEquals("APPROVED", request.getAction());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenActionMatchesApproveCaseInsensitive() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");

        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenActionMatchesRejectCaseInsensitive() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_WhenActionMatchesPendingCaseInsensitive() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");

        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatus_WhenActionIsUnknown() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");

        assertNull(request.getStatus());
    }

    @Test
    void testGetStatus_WhenAllFieldsAreNull() {
        ApprovalRequest request = new ApprovalRequest();

        assertNull(request.getStatus());
    }

    @Test
    void testCommentsGetterAndSetter() {
        ApprovalRequest request = new ApprovalRequest();
        request.setComments("Approval comments");

        assertEquals("Approval comments", request.getComments());
    }
}