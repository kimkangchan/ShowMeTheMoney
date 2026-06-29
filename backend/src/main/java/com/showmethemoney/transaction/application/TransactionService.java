package com.showmethemoney.transaction.application;

import com.showmethemoney.category.application.CategoryService;
import com.showmethemoney.common.BusinessException;
import com.showmethemoney.common.ErrorCode;
import com.showmethemoney.transaction.domain.Transaction;
import com.showmethemoney.transaction.infrastructure.TransactionMapper;
import com.showmethemoney.transaction.interfaces.dto.CreateTransactionRequest;
import com.showmethemoney.transaction.interfaces.dto.TransactionResponse;
import com.showmethemoney.transaction.interfaces.dto.UpdateTransactionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final CategoryService categoryService;

    public TransactionService(TransactionMapper transactionMapper, CategoryService categoryService) {
        this.transactionMapper = transactionMapper;
        this.categoryService = categoryService;
    }

    @Transactional
    public void create(Long userId, CreateTransactionRequest request) {
        Long categoryId = categoryService.validateAndGetId(request.categoryCode(), request.type());
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setCategoryId(categoryId);
        tx.setType(request.type());
        tx.setAmount(request.amount());
        tx.setMemo(request.memo());
        tx.setTransactionDate(request.transactionAt());
        transactionMapper.insert(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getList(Long userId, TransactionListRequest request) {
        int offset = request.page() * request.size();
        return transactionMapper.findAll(userId, request, offset).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getOne(Long userId, Long id) {
        Transaction tx = findOwnedTransaction(userId, id);
        return toResponse(tx);
    }

    @Transactional
    public void update(Long userId, Long id, UpdateTransactionRequest request) {
        Transaction existing = findOwnedTransaction(userId, id);

        Long categoryId = null;
        if (request.categoryCode() != null || request.type() != null) {
            String newCode = request.categoryCode() != null ? request.categoryCode() : existing.getCategoryCode();
            Integer newType = request.type() != null ? request.type() : existing.getType();
            categoryId = categoryService.validateAndGetId(newCode, newType);
        }

        Transaction update = new Transaction();
        update.setId(id);
        update.setType(request.type());
        update.setCategoryId(categoryId);
        update.setAmount(request.amount());
        update.setMemo(request.memo());
        update.setTransactionDate(request.transactionAt());
        transactionMapper.update(update);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        findOwnedTransaction(userId, id);
        transactionMapper.softDelete(id);
    }

    private Transaction findOwnedTransaction(Long userId, Long id) {
        Transaction tx = transactionMapper.findById(id);
        if (tx == null || tx.getDeletedAt() != null) throw new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND);
        if (!tx.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        return tx;
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(tx.getId(), tx.getType(), tx.getCategoryCode(),
                tx.getCategoryName(), tx.getAmount(), tx.getMemo(), tx.getTransactionDate());
    }
}
