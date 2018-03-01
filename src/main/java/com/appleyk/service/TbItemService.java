package com.appleyk.service;

import java.util.List;

import com.appleyk.entity.TbItem;

public interface TbItemService {
    
	/**
	 * 查询全部商品
	 * @return
	 */
	List<TbItem> GetTbItems();
	
	/**
	 * 根据商品ID查询
	 * @param id
	 * @return
	 */
	TbItem GetTbItem(Long id);
	
	/**
	 * 保存商品
	 * @param tbItem
	 * @return
	 */
	boolean     SaveTbItems(TbItem tbItem);
}
