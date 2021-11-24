var popupModifyViews={};

class CustomizedAgGrid{
	constructor(jobId, harvestResultNumber, gridContainer, gridOptions, menuItems){
		this.isGrid=true;
		this.jobId=jobId;
		this.harvestResultNumber=harvestResultNumber;
		this.gridContainer=gridContainer;
		this.gridOptions=gridOptions;
		this.menuItems=menuItems;
		this.grid=new agGrid.Grid(document.querySelector(this.gridContainer), this.gridOptions);
		this.dataMap={};

		if(menuItems){
			var customizedAgGridInstance=this;
			$.contextMenu({
	            selector: customizedAgGridInstance.gridContainer + ' .ag-row',
	            callback: function(key, options) {
					var rowId=$(this).attr('row-id');
					// var rowNode = customizedAgGridInstance.grid.gridOptions.api.getDisplayedRowAtIndex(rowId);
					var rowNode = customizedAgGridInstance.grid.gridOptions.api.getRowNode(rowId);
					contextMenuCallback(key, rowNode.data, customizedAgGridInstance, gPopupModifyHarvest);
	            },
	            items: customizedAgGridInstance.menuItems
	        });
		}

		popupModifyViews[gridContainer]=this;
	}

	getSelectedNodes(){
		var rows=this.grid.gridOptions.api.getSelectedRows();
		if(!rows || rows.length === 0){
			alert("Please select some rows!")
			return;
		}
		return rows;
	}

	getAllNodes(){
		var data=[];
		this.grid.gridOptions.api.forEachNode(function(node, index){
			data.push(node.data);
		});
		return data;
	}

	getNodeByIndex(index){
        return this.grid.gridOptions.api.getDisplayedRowAtIndex(index);
    }

    getRowByIndex(index){
        var node=this.getNodeByIndex(index);
        if (node) {
            return node.data;
        }else{
            return null;
        }
    }

	getNodeByDataIndex(rowIndex){
		var result;
		this.grid.gridOptions.api.forEachNode(function(node, index){
			if(node.data.index===rowIndex){
				result=node.data;
				return;
			}
		});
		return result;
	}

	getNodeByDataId(id){
		var result;
		this.grid.gridOptions.api.forEachNode(function(node, index){
			if(node.data.id===id){
				result=node.data;
				return;
			}
		});
		return result;
	}

	getNodeByUrl(url){
		var result;
		this.grid.gridOptions.api.forEachNode(function(node, index){
			if(node.data.url===url){
				result=node.data;
				return;
			}
		});
		return result;
	}

	clearAll(){
		this.grid.gridOptions.api.setRowData([]);
	}

	clear(dataset){
		this.grid.gridOptions.api.updateRowData({remove: dataset});
	}

	remove(dataMap){
		var dataset=[];
		this.grid.gridOptions.api.forEachNode(function(node, index){
			if(dataMap[node.data.id]){
				dataset.push(node.data);
			}
		});
		this.grid.gridOptions.api.updateRowData({remove: dataset});
	}

	removeByDataIndex(rowIndex){
		var node=this.getNodeByDataIndex(rowIndex);
		if(!node){
			return;
		}
		var dataset=[];
		dataset.push(node);
		this.clear(dataset);
	}

	insert(dataset){
		this.grid.gridOptions.api.updateRowData({add: dataset});
	}

	filter(keyWord){
		this.grid.gridOptions.api.setQuickFilter(keyWord);
	}

	setRowData(dataset){
		this.grid.gridOptions.api.setRowData(dataset);
	}

	deselectAll(){
		this.grid.gridOptions.api.deselectAll();
	}
}

function formatLazyloadData(dataset, isDomain){
	if (!dataset) {return;}

	for(var i=0;i<dataset.length;i++){
		var e=dataset[i];
		e.title=e.url;  
	    if (!isDomain && e.outlinks && e.outlinks.length > 0) {
	    	e.lazy = true;
	    	e.folder = true;
	    }

	    delete e['children'];
	}

	return dataset;
}


const treeOptionsBasic={
	extensions: ["table", "wide", "filter"],
	quicksearch: true,
	checkbox: true,
	table: {checkboxColumnIdx: 0, nodeColumnIdx: 1},
	source: [],
	selectMode: 3,
	renderColumns: function(event, treeNode) {
		var nodeData=treeNode.node.data;

		var $tdList = $(treeNode.node.tr).find(">td");

		if (nodeData.contentType && nodeData.contentType!=='unknown') {
			$tdList.eq(2).text(nodeData.contentType);
		}

		if (nodeData.statusCode > 0) {
			$tdList.eq(3).text(nodeData.statusCode);
		}
		
		if (nodeData.contentLength > 0){
			$tdList.eq(4).text(formatContentLength(nodeData.contentLength));
		}

		$tdList.eq(5).text(nodeData.totUrls);
		$tdList.eq(6).text(nodeData.totSuccess);
		$tdList.eq(7).text(nodeData.totFailed);
		$tdList.eq(8).text(formatContentLength(nodeData.totSize));

		$(treeNode.node.tr).attr("key", ""+treeNode.node.key);

		// $(treeNode.node.tr).attr("data", JSON.stringify(nodeData));
		if (nodeData.id > 0) {
			$(treeNode.node.tr).attr("idx", ""+nodeData.id);

			var toBeModifiedNode=gPopupModifyHarvest.gridToBeModified.getNodeByDataId(nodeData.id);
			if (toBeModifiedNode) {
				var classOfTreeRow=gPopupModifyHarvest.getTreeNodeStyle(toBeModifiedNode.option);
				$(treeNode.node.tr.children).addClass(classOfTreeRow);
			}
		}

		// if (nodeData.viewType && nodeData.viewType===2 && nodeData.id===-1){
		// 	$(treeNode.node.tr).attr("menu", "folder");
		// }else{
		// 	$(treeNode.node.tr).attr("menu", "url");
		// }
	}
};


const treeOptionsHarvestStruct=Object.assign({
	icon: function(event, data){
		if (data.node.folder) {
			return "fas fa-share-alt text-dark";
		}else{
			return "fas fa-link text-link";
		}
	},

	lazyLoad: function(event, data) {
		var nodeData=data.node.data;
	    var deferredResult = jQuery.Deferred();
	    var result = [];
	    var isDomain=nodeData.isDomain;
	    var viewType=nodeData.viewType;
	    var urlOutlinks = "/networkmap/get/outlinks?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber + "&id=" + nodeData.id;

	    fetchHttp(urlOutlinks, {}, function(response){
	    	var dataset=[];
			if (response.rspCode != 0) {
				alert(response.rspMsg);
	        }else{
	        	dataset=JSON.parse(response.payload);

	        	if (!isDomain && (!viewType || viewType!==2)) {
	        		dataset=formatLazyloadData(dataset, false);
	        	}
	        }
	        
			deferredResult.resolve(dataset);
		});

	    data.result = deferredResult;
	},
}, treeOptionsBasic);


const treeOptionsCascadedPath=Object.assign({
	icon: function(event, data){
		if (data.node.folder) {
			return "fa fa-folder-open text-dark";
		}else{
			return "fas fa-link text-link";
		}
	},

}, treeOptionsBasic);


class HierarchyTree{
	constructor(container, jobId, harvestResultNumber, options){
		var hierarchyTreeInstance=this;
		this.isTree=true;
		this.dataset=[];
		this.container=container;
		this.jobId=jobId;
		this.harvestResultNumber=harvestResultNumber;
		this.sourceUrlRootUrls="/networkmap/get/hierarchy/urls?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber;
		this.options=options;
        $.contextMenu({
		        selector: this.container + ' tr', 
		        trigger: 'right',
		        reposition: true,
		        callback: function(key, options) {
		            // var node=JSON.parse($(this).attr('data'));
		            var treeNodeKey=$(this).attr('key');
		            var treeNode=$.ui.fancytree.getTree(hierarchyTreeInstance.container).getNodeByKey(treeNodeKey);
		            var nodeData=hierarchyTreeInstance._getDataFromNode(treeNode);

		            contextMenuCallback(key, nodeData, hierarchyTreeInstance, gPopupModifyHarvest);
		        },
		        items: contextMenuItemsUrlTree
    	});
    	popupModifyViews[container]=this;
	}

	_getDataFromNode(treeNode){
		var nodeData=treeNode.data;
		nodeData.url=treeNode.title;
		nodeData.folder=treeNode.folder;
		return nodeData;
	}

	draw(dataset){
		dataset=formatLazyloadData(dataset);
		if($.ui.fancytree.getTree(this.container)){
			$.ui.fancytree.getTree(this.container).destroy();
		}

		this.options.source=dataset;
		$(this.container).fancytree(this.options);
	}

	drawWithDomain(dataset){
		if($.ui.fancytree.getTree(this.container)){
			$.ui.fancytree.getTree(this.container).destroy();
		}

		this.options.source=dataset;
		$(this.container).fancytree(this.options);
	}

	//Sort the domain names
	sortDomainByNames(dataset){
		for(var i=0; i<dataset.length; i++){
			for(var j=i+1; j<dataset.length; j++){
				if(dataset[i].title.localeCompare(dataset[j].title) > 0){
					var c=dataset[i];
					dataset[i]=dataset[j];
					dataset[j]=c;
				}
			}
		}
	}

	setRowData(dataset){
		if($.ui.fancytree.getTree(this.container)){
			$.ui.fancytree.getTree(this.container).destroy();
		}
		this.formatDataForTreeGrid(dataset);
		this.options.source=dataset;
  		$(this.container).fancytree(this.options);
	}

	formatDataForTreeGrid(dataset){
	  if (!dataset) {return;}
	  for(var i=0;i<dataset.length;i++){
	    var e=dataset[i];
	    e.title=e.url;
	    delete e['lazy'];
	    delete e['outlinks'];
	    this.formatDataForTreeGrid(e.children);
	  }
	}

	getSelectedNodes(){
		var selData=[];
		var selNodes = $.ui.fancytree.getTree(this.container).getSelectedNodes();
		for(var i=0; i<selNodes.length; i++){
			var treeNode=selNodes[i];
			var nodeData=this._getDataFromNode(treeNode);
			if ((this.container==='#hierachy-tree-url-names' && treeNode.folder!=null && treeNode.folder===false)
			|| (this.container==='#hierachy-tree-harvest-struct' && (!treeNode.folder || treeNode.folder===false))) {
				selData.push(nodeData);
			}
			// selData.push(selNodes[i].data);
			// $.ui.fancytree.getTree(this.container).applyCommand('indent', selNodes[i]);
		}

		// console.log(selData);
		return selData;
	}

	getAllNodes(){
		var dataset=[];
		var rootNode= $.ui.fancytree.getTree(this.container).getRootNode();
		this._walkAllNodes(dataset, rootNode);
		return dataset;
	}

	_walkAllNodes(dataset, treeNode){
		var nodeData=this._getDataFromNode(treeNode);

		if ((this.container==='#hierachy-tree-url-names' && treeNode.folder!=null && treeNode.folder===false)
			|| (this.container==='#hierachy-tree-harvest-struct' && treeNode.folder!==null)) {
			dataset.push(nodeData);
		}

		var childrenNodes=treeNode.children;
		if (!childrenNodes) {
			return;
		}
		for(var i=0; i<childrenNodes.length; i++){
			this._walkAllNodes(dataset, childrenNodes[i]);
		}
	}

	clearAll(){
		if($.ui.fancytree.getTree(this.container)){
			$.ui.fancytree.getTree(this.container).destroy();
		}
	}

	filter(match){
		var tree=$.ui.fancytree.getTree(this.container);
		var filterFunc = tree.filterNodes; //tree.filterBranches
		var option={
				autoApply: false,   // Re-apply last filter if lazy data is loaded
				autoExpand: true, // Expand all branches that contain matches while filtered
				counter: true,     // Show a badge with number of matching child nodes near parent icons
				fuzzy: false,      // Match single characters in order, e.g. 'fb' will match 'FooBar'
				hideExpandedCounter: true,  // Hide counter badge if parent is expanded
				hideExpanders: false,       // Hide expanders if all child nodes are hidden by filter
				highlight: true,   // Highlight matches by wrapping inside <mark> tags
				leavesOnly: false, // Match end nodes only
				nodata: true,      // Display a 'no data' status node if result is empty
				mode: "hide"       //dimm or hide Grayout unmatched nodes (pass "hide" to remove unmatched node instead)
		}
		var n = filterFunc.call(tree, match, option);
		console.log("filter out: "+n);
	}
}

class PopupModifyHarvest{
	constructor(jobId, harvestResultId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultId=harvestResultId;
		this.harvestResultNumber=harvestResultNumber;
		this.crawlerPathTreeView=new HierarchyTree("#hierachy-tree-harvest-struct", jobId, harvestResultNumber, treeOptionsHarvestStruct);
		this.folderTreeView=new HierarchyTree("#hierachy-tree-url-names", jobId, harvestResultNumber, treeOptionsCascadedPath);
		this.gridCandidate=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-candidate', gridOptionsCandidate, contextMenuItemsUrlGrid);
		this.gridImportPrepare=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-bulk-import-prepare', gridOptionsImportPrepare, null);
		this.gridToBeModified=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-tobe-modified', gridOptionsToBeModified, Object.assign({}, contextMenuItemsToBeModified));
		this.gridToBeModifiedVerified=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-tobe-modified-verified', gridOptionsToBeModifiedVerified, Object.assign({}, contextMenuItemsToBeModified));
		this.processorModify=new ModifyHarvestProcessor(jobId, harvestResultId, harvestResultNumber);
		this.uriSeedUrl="/networkmap/get/root/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		this.uriInvalidUrl="/networkmap/get/malformed/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
	}

	clear(){
		if (currentMainTab === 'tree-url-names') {
			this.folderTreeView.clearAll();
		}else if (currentMainTab === 'candidate-query') {
			this.gridCandidate.clearAll();
		}
	}

	reset(){
		if (currentMainTab === 'tree-harvest-struct') {
			this.initCrawlerPathTreeView();
		}else if (currentMainTab === 'tree-url-names') {
			this.initFolderTreeView(null);
		}else if (currentMainTab === 'candidate-query') {
			this.gridCandidate.clearAll();
		}
		$('#menu-tool-bar input[name="' + currentMainTab + '"]').val('');
	}

	showOutlinks(data){
		if ($.isEmptyObject(data) || data.id < 0) {
			toastr.warning("The url [" + data.url + "] does not exist.");
			return;
		}
		//this.crawlerPathTreeView.draw(dataset);
		var popupModifyHarvestInstance=this;
		var url="/networkmap/get/node?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber + "&id=" + data.id;
		g_TurnOnOverlayLoading();
		fetchHttp(url, null, function(response){
			if (!$.isEmptyObject(response.error)) {
				response.rspCode=9999;
				response.rspMsg=response.error;
			}

			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);				
				return;
	        }

			var data=JSON.parse(response.payload);
			var dataset=[data];

			$('#popup-window-modification').hide();

			currentMainTab = 'tree-harvest-struct';
			$('#btn-group-main-tabs label[name="tree-harvest-struct"]').trigger('click');
		
			popupModifyHarvestInstance.crawlerPathTreeView.draw(dataset);

			g_TurnOffOverlayLoading();
		});
	}

	filter(val){
		if (currentMainTab === 'tree-harvest-struct') {
			this.crawlerPathTreeView.filter(val);
		}if (currentMainTab === 'tree-url-names') {
			this.folderTreeView.filter(val);
		}else if (currentMainTab === 'candidate-query') {
			this.gridCandidate.filter(val);
		}
	}

	getTreeNodeStyle(option){
		if(!option){
			return '';
		}

		option=option.toLowerCase();
		var classOfTreeRow='';
		if (option === 'prune') {
			classOfTreeRow='tree-row-delete';
		}else if (option==='recrawl') {
			classOfTreeRow='tree-row-recrawl';
		}else{
			classOfTreeRow='tree-row-file';
		}

		return classOfTreeRow;		
	}

	setRowStyle(){
		var toBeModifiedDataMap={};
		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			if (node.data.id > 0) {
				toBeModifiedDataMap[node.data.id]=node.data;
			}
		});

		//Set class for tree view
		$('.hierachy-tree td').removeClass("tree-row-delete");
		$('.hierachy-tree td').removeClass("tree-row-recrawl");
		$('.hierachy-tree td').removeClass("tree-row-file");
		for(var key in toBeModifiedDataMap){
			var classOfTreeRow=this.getTreeNodeStyle(toBeModifiedDataMap[key].option);
			console.log('.hierachy-tree tr[idx="' + key + '"] td' + classOfTreeRow);
			$('.hierachy-tree tr[idx="' + key + '"] td').addClass(classOfTreeRow);
		}

		//Set class for grid candidate
		this.gridCandidate.gridOptions.api.forEachNode(function(node, index){
			if (toBeModifiedDataMap[node.data.id] && toBeModifiedDataMap[node.data.id].option) {
				node.data.flag=toBeModifiedDataMap[node.data.id].option;
				console.log(node.data.flag);
			}else{
				node.data.flag='normal';
			}
		});
		this.gridCandidate.gridOptions.api.redrawRows(true);
	}

	undo(data, source){
		this.gridToBeModified.clear(data);
		this.gridToBeModifiedVerified.clear(data);
		this.setRowStyle();
	}

	modify(dataset, option){
		var map={};
		for (var i = 0; i < dataset.length; i++) {
			dataset[i].option=option;
			dataset[i].flag='new';
			dataset[i].index=i;
			map[i]=dataset[i];
		}
		var popupModifyHarvestInstance=this;
		this._appendAndMoveHarvest2ToBeModifiedList(dataset, function(handledDateset){
			for (var i = 0; i < handledDateset.length; i++) {
				var oldNode=map[handledDateset[i].index];
				if (oldNode) {
					handledDateset[i].uploadFile=oldNode.uploadFile;
				}
			}
			popupModifyHarvestInstance._moveHarvest2ToBeModifiedList(handledDateset);
		});
	}

	_appendAndMoveHarvest2ToBeModifiedList(data, callback){
		var popupModifyHarvestInstance=this;
		var url="/check-and-append?targetInstanceOid=" + this.jobId + "&harvestNumber=" + this.harvestResultNumber;
		fetchHttp(url, data, function(rsp){
			if (rsp.rspCode!==0) {
				alert(rsp.rspMsg);
				return;
			}

			var dataset=JSON.parse(rsp.payload);
			callback(dataset);
		});
	}

	_moveHarvest2ToBeModifiedList(data){
		if(!data){return;}
		var map={};
		var isPruneOutlink=false;
		for(var i=0; i< data.length; i++){
			var node=data[i];
			map[node.url]=node;
		}

		//Checking duplicated rows
		var isReplaceDuplicated=false;
		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			if (!isReplaceDuplicated && map[node.data.url]) {
				isReplaceDuplicated=confirm('There are duplicated urls in patch harvest list, would you like to replace them?');
				if (!isReplaceDuplicated) {
					return;
				}
			}
		});

		// Add to 'patch harvest' grid, and marked as new
		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			if(map[node.data.url]){
				node.data=map[node.data.url];
				delete map[node.data.url];
			}else{
				node.data.flag='normal';
			}
		});
		this.gridToBeModified.gridOptions.api.redrawRows(true);

		var dataset=[];
		for(var key in map){
			var node=map[key];
			dataset.push(node);
		}
		this.gridToBeModified.gridOptions.api.updateRowData({add: dataset});

		this.setRowStyle();
	}

	showImportFromRowIndex(rowIndex){
		var data=this.gridImportPrepare.getNodeByDataIndex(rowIndex);
		this.showImport(data);
	}

	showRecrawl(){
		$('#specifyTargetUrlInputForRecrawl').val('');
		$('#popup-window-single-recrawl').show();
	}

	showImport(data){
		var d;
		if (data && data.index && data.index > 0) {
			$('#single-import-index').html(data.index);
			$('#specifyTargetUrlInput').attr('disabled', 'disabled');
			d=moment(data.lastModifiedDate);
		}else{
			$('#single-import-index').html(-1);
			$('#specifyTargetUrlInput').removeClass('disabled');
			d=moment();
		}

		if(data){
			$('#specifyTargetUrlInput').val(data.url);
		}else{
			$('#specifyTargetUrlInput').val('');
		}


		$('#sourceFile').val(null);
		$('#label-source-file').html('Choose file');

		var ds=d.format('YYYY-MM-DDTHH:mm');
		$("#datetime-local-customizard").val(ds);
		
	    $('#popup-window-single-import').show();
	}

	inspectHarvest(data){		
		var map={};
		for(var i=0; i< data.length; i++){
			var node=data[i];
			map[node.id]=node;
		}

		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			var id=map[node.data.id];
			if(map[node.data.id]){
				// delete map[node.data.id];
				map[node.data.id].flag=node.data.option;
			}
		});

		var dataset=[];
		for(var key in map){
			var node=map[key];
			dataset.push(node);
		}
		// this.gridCandidate.gridOptions.api.updateRowData({ add: dataset});
		this.gridCandidate.setRowData(dataset);

		this.setRowStyle();
	}

	queryHarvest(){
		$("#popup-window-query-input").hide();
		var searchCondition={
		    "domainNames": [],
		    "contentTypes": [],
		    "statusCodes": [],
		    "urlNames": []
		}

		var domainNames=$("#queryDomainName").val().trim();
		searchCondition.domainNames=splitString2Array(domainNames);

		var urlNames=$("#queryUrlName").val().trim();
	    searchCondition.urlNames=splitString2Array(urlNames);

		var statusCodes=$("#queryStatusCode").val().trim();
		searchCondition.statusCodes=splitString2Array(statusCodes);

	    var contentTypes=$("#queryContentType").val().trim();
		searchCondition.contentTypes=splitString2Array(contentTypes);

	    var reqUrl=''
	    if (currentMainTab === 'candidate-query') {
	    	this.checkUrls(searchCondition, 'inspect');
	    }else if (currentMainTab === 'tree-url-names') {
	    	this.initFolderTreeView(searchCondition);
	  	}else{
	  		alert('Bad request');
	  		return;
	  	}
	}

	checkUrls(searchCondition, flag){
		if(!searchCondition || !flag){
			console.log('Invalid input, searchCondition='+searchCondition+', flag='+flag);
			return;
		}

		var popupModifyHarvestInstance=this;
		var url="/networkmap/search/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		if (flag==='inspect') {
			currentMainTab = 'candidate-query';
			$('#btn-group-main-tabs label[name="candidate-query"]').trigger('click');
		}
		g_TurnOnOverlayLoading();
		fetchHttp(url, searchCondition, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			if(flag==='prune'){
				popupModifyHarvestInstance.modify(data, 'prune');
			}else if(flag==='inspect'){
				popupModifyHarvestInstance.inspectHarvest(data);
			}

			g_TurnOffOverlayLoading();
		});
	}

	loadUrls(url){
		var popupModifyHarvestInstance=this;
		g_TurnOnOverlayLoading();
		fetchHttp(url, null, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			if(data.length===0){
				g_TurnOffOverlayLoading();
				alert('No data found!');
				return;
			}else{
				popupModifyHarvestInstance.gridCandidate.setRowData(data);
			}
			g_TurnOffOverlayLoading();
		});
	}

	initCrawlerPathTreeView(){
		var popupModifyHarvestInstance=this;
		g_TurnOnOverlayLoading();
		fetchHttp(this.uriSeedUrl, null, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			popupModifyHarvestInstance.crawlerPathTreeView.draw(data);
			popupModifyHarvestInstance.setRowStyle();
			g_TurnOffOverlayLoading();
		});
	}

	initFolderTreeView(searchCommand){
		var reqUrl = "/networkmap/get/urls/cascaded-by-path?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber + '&title=';
		var popupModifyHarvestInstance=this;
		g_TurnOnOverlayLoading();
		fetchHttp(reqUrl, searchCommand, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			popupModifyHarvestInstance.folderTreeView.drawWithDomain(data);
			popupModifyHarvestInstance.setRowStyle();
			g_TurnOffOverlayLoading();
		});
	}

	exportData(req){
		g_TurnOnOverlayLoading();
		// var req=[];
		// for (var i = 0; i< data.length; i++) {
		// 	var node=data[i];
		// 	if (node.viewType && node.viewType===2 && ) {}
		// 	data[i]
		// }
		var url=webContextPath+"/curator/export/data?targetInstanceOid=" + this.jobId + "&harvestNumber=" + this.harvestResultNumber;
		fetch(url, { 
		    method: 'POST',
		    redirect: 'follow',
		    headers: {'Content-Type': 'application/json'},
		    body: JSON.stringify(req)
		}).then((res) => {
			if (res.ok) {
				return res.blob();
			}
			return null;
		}).then((blob) => {
			g_TurnOffOverlayLoading();
			console.log(blob);
			if(blob){
				saveAs(blob, "wct_export_data.xlsx");
			}
		});
	}

	//Save and reindexing
	preApply(){
		var dataset=this.gridToBeModified.getAllNodes();
		if(!dataset || dataset.length === 0){
			alert('Please input the URLs to be modified.');
			return;
		}

		var replaceModeByStatus=parseInt($("#radio-group-replace-status input[name='r-status']:checked").attr("flag"));
		var map={};
		var isValid=true;
		for(var i=0;i<dataset.length;i++){
			var node=dataset[i];
			node.respCode=0;
			node.respMsg='';
			var target=node.url;
			if(map[target]){
				node.respCode=-1;
				node.respMsg+="Duplicated target URL at line: " + (i+1);
				isValid=false;
			}
			map[target]=node;


			if(!target.toLowerCase().startsWith("http://") &&
				!target.toLowerCase().startsWith("https://")){
				node.respCode=-1;
				node.respMsg+="You must specify a valid target URL at line:" + (i+1);
				isValid=false;
			}

			var option=node.option.toUpperCase();
			if(option==="FILE"){
				if(!node.uploadFile){
					node.respCode=1;
					node.respMsg+='File is not uploaded at line' + (i+1);
					isValid=false;
				}

				var modifiedMode=node.modifiedMode.toUpperCase();
				var lastModifiedDate=node.lastModifiedDate;
				if(modifiedMode!=='TBC' && modifiedMode!=='FILE' && modifiedMode!=='CUSTOM'){
					node.respCode=-1;
					node.respMsg+='Invalid modifiedMode at line: ' + (i+1);
					isValid=false;
				}

				if((modifiedMode==='FILE' || modifiedMode==='CUSTOM') && lastModifiedDate<=0){
					node.respCode=-1;
					node.respMsg+="Invalid modification datetime at line: " + (i+1);
					isValid=false;
				}
			}

			var replaceAble=true;
			if (option === 'PRUNE' && node.outlinkNum > 0) {
				node.respCode=9;
				node.respMsg+="Existing URL with outlinks will be pruned";
				replaceAble=false;
			}
			if (replaceAble && replaceModeByStatus===1 && node.outlinkNum > 0) {
				node.respCode=9;
				node.respMsg+="Existing URL with outlinks will be pruned";
				replaceAble=false;
			}
			if (replaceAble && replaceModeByStatus===2 && !isSuccessNode(node.statusCode) && node.outlinkNum > 0) {
				node.respCode=9;
				node.respMsg+="Existing URL with outlinks will be pruned";
				replaceAble=false;
			}
		}

		if (!isValid) {
			alert('Please correct or cancel invalid rows!');
			return;
		}

		this.gridToBeModifiedVerified.setRowData(dataset);
		$('#popup-window-modification .flag-apply').hide();
		$('#popup-window-modification .flag-submit').show();
	}

	confirmApply(){
		$('#popup-window-modification').hide();
		$('#popup-window-modification .flag-apply').show();
		$('#popup-window-modification .flag-submit').hide();

		g_TurnOnOverlayLoading();

		var dataset=this.gridToBeModified.getAllNodes();

		//Uploading files 
		var toBeUploadedNodes=[];
		for(var i=0;i < dataset.length; i++){
			if(dataset[i].uploadFile){
				toBeUploadedNodes.push(dataset[i]);
			}
		}

		var popupModifyHarvestInstance=this;
		this.recurseUploadFiles(toBeUploadedNodes, 0, function(){
			var replaceModeByStatus=parseInt($("#radio-group-replace-status input[name='r-status']:checked").attr("flag"));
			var applyCommand={
				targetInstanceId: popupModifyHarvestInstance.jobId,
				harvestResultId: popupModifyHarvestInstance.harvestResultId,
				harvestResultNumber: popupModifyHarvestInstance.harvestResultNumber,
				newHarvestResultNumber: 0,
				replaceOptionStatus: replaceModeByStatus,
				replaceOptionOutlink: 1,
				dataset: dataset,
				provenanceNote: $('#provenance-note').val(),
			};
			
			var url="/modification/apply";
			fetchHttp(url, applyCommand, function(response){
				g_TurnOffOverlayLoading();
				if(response.respCode !== 0){
					alert(response.respMsg);
				}else{
					updateDerivedHarvestResults(response.derivedHarvestResult);
				}
			});
		});
	}

	// Upload file
	recurseUploadFiles(toBeUploadedNodes, idx, callback){
		if(!toBeUploadedNodes || idx >= toBeUploadedNodes.length){
			callback();
			return;
		}

		var popupModifyHarvestInstance=this;
		var reader = new FileReader();
		reader.addEventListener("loadend", function () {
			var req=toBeUploadedNodes[idx];
			req.uploadFileContent=reader.result;
			

			var url="/modification/upload-file-stream?job=" + popupModifyHarvestInstance.jobId + "&harvestResultNumber=" + popupModifyHarvestInstance.harvestResultNumber;
			fetchHttp(url, req, function(rsp){
				req.uploadFileContent='';
				if (rsp.respCode !== 0) {
					g_TurnOffOverlayLoading();
					alert(rsp.respMsg);
					return;
				}
				req.cachedFileName=rsp.cachedFileName;
				popupModifyHarvestInstance.recurseUploadFiles(toBeUploadedNodes, idx+1, callback);
			});
		});

		reader.readAsDataURL(toBeUploadedNodes[idx].uploadFile);
	}
}
