package com.showmethemoney.transaction.infrastructure;

import com.showmethemoney.transaction.domain.Transaction;
import com.showmethemoney.transaction.application.TransactionListRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TransactionMapper {

    void insert(Transaction transaction);

    List<Transaction> findAll(Long userId, TransactionListRequest request, int offset);

    Transaction findById(Long id);

    void update(Transaction transaction);

    void softDelete(Long id);
}
