package com.appleyk.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appleyk.entity.TbItem;
import com.appleyk.result.ResponseMessage;
import com.appleyk.result.ResponseResult;
import com.appleyk.service.TbItemService;

@RestController
@RequestMapping("/rest/v1.0.1/database/tbitem")
public class TbItemController {

	@Autowired
	private TbItemService itemService;

	@GetMapping("/query")
	public ResponseResult GetTbItems() {
		List<TbItem> result = itemService.GetTbItems();
		return new ResponseResult(200, "查询成功，size = " + result.size(), result);
	}

	@PostMapping("/save")
	public ResponseResult SaveTbItem() {
		TbItem tbItem = new TbItem();
		if (itemService.SaveTbItems(tbItem)) {
			return new ResponseResult(ResponseMessage.OK);
		}

		return new ResponseResult(ResponseMessage.INTERNAL_SERVER_ERROR);

	}
}
