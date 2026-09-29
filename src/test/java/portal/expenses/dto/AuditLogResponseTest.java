package portal.expenses.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditLogResponseTest {

    @Test
    void testGettersAndSetters() {
        AuditLogResponse response = new AuditLogResponse();

        assertNull(response.getId());
        assertNull(response.getAction());
        assertNull(response.getOldStatus());
        assertNull(response.getNewStatus());
        assertNull(response.getComment());
        assertNull(response.getPerformedBy());
        assertNull(response.getCreatedAt());

        Long id = 100L;
        String action = "APPROVE";
        String oldStatus = "PENDING";
        String newStatus = "APPROVED";
        String comment = "Looks good";
        String performedBy = "manager@example.com";
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

        response.setId(id);
        response.setAction(action);
        response.setOldStatus(oldStatus);
        response.setNewStatus(newStatus);
        response.setComment(comment);
        response.setPerformedBy(performedBy);
        response.setCreatedAt(createdAt);

        assertEquals(id, response.getId());
        assertEquals(action, response.getAction());
        assertEquals(oldStatus, response.getOldStatus());
        assertEquals(newStatus, response.getNewStatus());
        assertEquals(comment, response.getComment());
        assertEquals(performedBy, response.getPerformedBy());
        assertEquals(createdAt, response.getCreatedAt());
    }
}