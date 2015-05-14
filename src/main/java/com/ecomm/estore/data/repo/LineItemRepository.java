package com.ecomm.estore.data.repo;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.ecomm.estore.data.LineItem;

public interface LineItemRepository extends PagingAndSortingRepository<LineItem, Long> {

}
