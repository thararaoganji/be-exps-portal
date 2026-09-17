package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApprovalRequestTest {

    @Test
    void testGetStatus_whenStatusIsExplicitlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_whenApprovedIsTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);
        assertEquals(true, request.getApproved());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_whenApprovedIsFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);
        assertEquals(false, request.getApproved());
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_whenActionMatchesEnumValueDirectly() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals("APPROVED", request.getAction());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_whenActionIsApproveLowerCase() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatus_whenActionIsRejectLowerCase() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatus_whenActionIsPendingLowerCase() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatus_whenActionIsUnknown() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");
        assertNull(request.getStatus());
    }

    @Test
    void testGetStatus_whenAllNull() {
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