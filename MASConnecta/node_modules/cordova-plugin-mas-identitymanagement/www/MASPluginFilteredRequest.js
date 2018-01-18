/*
 * Copyright (c) 2016 CA, Inc. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 *
 */
//
//  MASPluginFilteredRequest.js
//

var MASPluginFilteredRequest = function(){
	this.json = {
		filter:[],
		sortOrder:"",
		sortAttribute:"",
		pagination:[]
	},
	this.so = new MASPluginFilterConstant().SortOrder.ascending,
	this.soAttr = "",
	this.pageStart = 1,
	this.count = 10,
	this.filterArray = [],

	this.setPagination = function(startIndex,count){
		this.pageStart = startIndex;
		this.count = count;
	},

	this.setSortOrder = function(sortOrder,sortAttribute){
		this.so = sortOrder;
		this.soAttr = sortAttribute;
	},

	this.setFilter = function(attribute,operator,value){
		this.filterArray.push(attribute+"::"+operator+"::"+value);
	},

	this.returnFinal = function(){
		this.json.filter = this.filterArray;
		this.json.sortOrder = this.so;
		this.json.sortAttribute = this.soAttr;
		this.json.pagination.push(this.pageStart);
		this.json.pagination.push(this.count);
		return this.json;
	}
}
module.exports = MASPluginFilteredRequest;