package fruitshop.ai_service.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fruitshop.ai_service.orchestrator.client.CatalogClient;
import org.springframework.stereotype.Service;

@Service
public class FruitInventoryTool {

    private final CatalogClient catalogClient;
    private final ObjectMapper objectMapper;

    public FruitInventoryTool(CatalogClient catalogClient, ObjectMapper objectMapper) {
        this.catalogClient = catalogClient;
        this.objectMapper = objectMapper;
    }

    public String checkInventory(String fruitName) {
        try {
            String rawJson = catalogClient.searchProducts(fruitName, 0, 5);

            if (rawJson == null || rawJson.isBlank()) {
                return "Không tìm thấy thông tin về '" + fruitName + "' trong kho.";
            }

            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode content = root.path("content");

            if (content.isMissingNode() || content.isEmpty()) {
                return "Hiện tại cửa hàng không có '" + fruitName + "' trong danh sách sản phẩm.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Thông tin tồn kho cho '").append(fruitName).append("':\n");

            for (JsonNode product : content) {
                String id = product.path("productId").asText("");
                String name = product.path("productName").asText("Không rõ tên");
                long stock = product.path("stock").asLong(0);
                long price = product.path("price").asLong(0);
                int status = product.path("status").asInt(0);

                if (status == 1) { // chỉ hiển thị sản phẩm đang bán
                    sb.append("- [ID: ").append(id).append("] ").append(name)
                      .append(": còn ").append(stock).append(" sản phẩm")
                      .append(", giá ").append(String.format("%,d", price)).append(" VNĐ\n");
                }
            }

            return sb.length() > sb.indexOf("\n") + 1 ? sb.toString()
                    : "Hiện tại '" + fruitName + "' đang tạm hết hàng hoặc chưa được bán.";

        } catch (Exception e) {
            System.err.println("[FruitInventoryTool] Error: " + e.getMessage());
            return "Lỗi khi truy vấn kho: " + e.getMessage();
        }
    }
}
