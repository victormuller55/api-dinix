package br.net.convertix.dinix.mapper;

import br.net.convertix.dinix.dto.response.InstallmentResponse;
import br.net.convertix.dinix.dto.response.PurchaseItemResponse;
import br.net.convertix.dinix.dto.response.PurchaseResponse;
import br.net.convertix.dinix.dto.response.TagResponse;
import br.net.convertix.dinix.entity.Installment;
import br.net.convertix.dinix.entity.Purchase;
import br.net.convertix.dinix.entity.PurchaseItem;
import br.net.convertix.dinix.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PurchaseMapper {

    public PurchaseResponse toResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getPurchaseDate(),
                purchase.getPurchaseTime(),
                purchase.getTotalAmount(),
                purchase.getCategory() != null ? purchase.getCategory().getId() : null,
                purchase.getLocation() != null ? purchase.getLocation().getId() : null,
                purchase.getPaymentMethod(),
                purchase.getFinancialAccount() != null ? purchase.getFinancialAccount().getId() : null,
                purchase.getCreditCard() != null ? purchase.getCreditCard().getId() : null,
                purchase.getNotes(),
                purchase.getNumberOfInstallments(),
                purchase.getInstallmentAmount(),
                purchase.getFirstInstallmentDate(),
                purchase.getItems().stream().map(this::toItem).toList(),
                purchase.getInstallments().stream().map(this::toInstallment).toList(),
                purchase.getTags().stream().map(this::toTag).toList(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    public PurchaseItemResponse toItem(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getName() : null,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }

    public InstallmentResponse toInstallment(Installment installment) {
        return new InstallmentResponse(
                installment.getId(),
                installment.getInstallmentNumber(),
                installment.getTotalInstallments(),
                installment.getAmount(),
                installment.getDueDate(),
                installment.getStatus(),
                installment.getPaidAt()
        );
    }

    public TagResponse toTag(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }

    public List<PurchaseResponse> toResponseList(List<Purchase> purchases) {
        return purchases.stream().map(this::toResponse).toList();
    }
}
