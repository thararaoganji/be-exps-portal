package portal.expenses.dto;

import org.junit.jupiter.api.Test;
import portal.expenses.entity.ApprovalStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRequestTest {

    @Test
    void testGetStatusWhenStatusIsExplicitlySet() {
        ApprovalRequest request = new ApprovalRequest();
        request.setStatus(ApprovalStatus.APPROVED);
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromApprovedTrue() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(true);
        assertTrue(request.getApproved());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromApprovedFalse() {
        ApprovalRequest request = new ApprovalRequest();
        request.setApproved(false);
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionExactEnumMatch() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("APPROVED");
        assertEquals("APPROVED", request.getAction());
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setAction("REJECTED");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());

        request.setAction("PENDING");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionLowerCaseKeywords() {
        ApprovalRequest request = new ApprovalRequest();
        
        request.setAction("approve");
        assertEquals(ApprovalStatus.APPROVED, request.getStatus());

        request.setAction("reject");
        assertEquals(ApprovalStatus.REJECTED, request.getStatus());

        request.setAction("pending");
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
    }

    @Test
    void testGetStatusDerivedFromActionUnknownReturnsNull() {
        ApprovalRequest request = new ApprovalRequest();
        request.setAction("unknown_action");
        assertNull(request.getStatus());
    }

    @Test
    void testGetStatusWhenAllFieldsNullReturnsNull() {
        ApprovalRequest request = new ApprovalRequest();
        assertNull(request.getStatus());
        assertNull(request.getAction());
        assertNull(request.getApproved());
        assertNull(request.getComments());
    }

    @Test
    void testCommentsGetterAndSetter() {
        ApprovalRequest request = new ApprovalRequest();
        request.setComments("Looks good to me");
        assertEquals("Looks good to me", request.getComments());
    }
}