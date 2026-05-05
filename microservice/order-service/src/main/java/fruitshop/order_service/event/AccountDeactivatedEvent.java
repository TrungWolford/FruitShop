package fruitshop.order_service.event;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeactivatedEvent {
    private String accountId;
    private Date deactivatedAt;
}
