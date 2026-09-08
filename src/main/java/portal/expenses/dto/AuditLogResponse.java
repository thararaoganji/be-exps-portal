package portal.expenses.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogResponse {

    private Long id;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String comment;
    private String performedBy;
    private LocalDateTime createdAt;
}