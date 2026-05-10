package fruitshop.order_service.event;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDeactivatedEvent {
    private String accountId;
    private Date deactivatedAt;
}
