class CustomizedAgGrid{
	constructor(jobId, harvestResultNumber, gridContainer, gridOptions, menuItems){
		this.jobId=jobId;
		this.harvestResultNumber=harvestResultNumber;
		this.gridContainer=gridContainer;
		this.gridOptions=gridOptions;
		this.menuItems=menuItems;
		this.grid=new agGrid.Grid(document.querySelector(this.gridContainer), this.gridOptions);
		this.dataMap={};

		if(menuItems){
			var that=this;
			$.contextMenu({
	            selector: that.gridContainer + ' .ag-row', 
	            callback: function(key, options) {
					var rowId=$(this).attr('row-id');
					// var rowNode = that.grid.gridOptions.api.getDisplayedRowAtIndex(rowId);
					var rowNode = that.grid.gridOptions.api.getRowNode(rowId);
					contextMenuCallback(key, rowNode.data, that, gPopupModifyHarvest);
	            },
	            items: that.menuItems
	        });
		}
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
		// var urlLength=e.url.length;
		// if (urlLength > 150) {
		// 	e.title=e.url.substring(0,150)+'...';
		// }else{
		// 	e.title=e.url;
		// }
	    
	    if (!isDomain && e.outlinks && e.outlinks.length > 0) {
	    	e.lazy = true;
	    	e.folder = true;
	    }

	    delete e['children'];
	}

	return dataset;
}

function treeRenderColumns(event, treeNode) {
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
	}

	// if (nodeData.viewType && nodeData.viewType===2 && nodeData.id===-1){
	// 	$(treeNode.node.tr).attr("menu", "folder");
	// }else{
	// 	$(treeNode.node.tr).attr("menu", "url");
	// }
}

var treeOptionsBasic={
	extensions: ["table", "wide", "filter"],
	quicksearch: true,
	checkbox: true,
	table: {checkboxColumnIdx: 0, nodeColumnIdx: 1},
	source: [],
};

var treeOptionsHarvestStruct=JSON.parse(JSON.stringify(treeOptionsBasic));
treeOptionsHarvestStruct.renderColumns=function(event, data){
	treeRenderColumns(event, data);
};
treeOptionsHarvestStruct.icon=function(event, data){
	if (data.node.folder) {
		return "fas fa-share-alt text-dark";
	}else{
		return "fas fa-link text-link";
	}
};
treeOptionsHarvestStruct.lazyLoad=function(event, data) {
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
};


var treeOptionsCascadedPath=JSON.parse(JSON.stringify(treeOptionsBasic));
treeOptionsCascadedPath.renderColumns=function(event, data){
	treeRenderColumns(event, data);
};
treeOptionsCascadedPath.icon = function(event, data){
	if (data.node.folder) {
		return "fa fa-folder-open text-dark";
	}else{
		return "fas fa-link text-link";
	}
};

class HierarchyTree{
	constructor(container, jobId, harvestResultNumber, options){
		var that=this;
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
		            var treeNode=$.ui.fancytree.getTree(that.container).getNodeByKey(treeNodeKey);
		            var nodeData=that._getDataFromNode(treeNode);

		            contextMenuCallback(key, nodeData, that, gPopupModifyHarvest);
		        },
		        items: contextMenuItemsUrlTree
    	});
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
			selData.push(selNodes[i].data);
			// $.ui.fancytree.getTree(this.container).applyCommand('indent', selNodes[i]);
		}

		console.log(selData);
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
		dataset.push(nodeData);

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
				leavesOnly: true, // Match end nodes only
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
		this.hierarchyTreeHarvestStruct=new HierarchyTree("#hierachy-tree-harvest-struct", jobId, harvestResultNumber, treeOptionsHarvestStruct);
		this.hierarchyTreeUrlNames=new HierarchyTree("#hierachy-tree-url-names", jobId, harvestResultNumber, treeOptionsCascadedPath);
		this.gridCandidate=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-candidate', gridOptionsCandidate, contextMenuItemsUrlGrid);
		this.gridToBeModified=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-tobe-modified', gridOptionsToBeModified, contextMenuItemsToBeModified);
		this.gridImportPrepare=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-bulk-import-prepare', gridOptionsImportPrepare, null);
		this.processorModify=new ModifyHarvestProcessor(jobId, harvestResultId, harvestResultNumber);
		this.uriSeedUrl="/networkmap/get/root/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		this.uriInvalidUrl="/networkmap/get/malformed/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
	}

	clear(){
		if (currentMainTab === 'tree-url-names') {
			this.hierarchyTreeUrlNames.clearAll();
		}else if (currentMainTab === 'candidate-query') {
			this.gridCandidate.clearAll();
		}
	}

	filter(val){
		if (currentMainTab === 'tree-harvest-struct') {
			this.hierarchyTreeHarvestStruct.filter(val);
		}if (currentMainTab === 'tree-url-names') {
			this.hierarchyTreeUrlNames.filter(val);
		}else if (currentMainTab === 'candidate-query') {
			this.gridCandidate.filter(val);
		}
	}

	setRowStyle(){
		var toBeModifiedDataMap={};
		this.gridCandidate.gridOptions.api.forEachNode(function(node, index){
			if (node.data.id > 0) {
				toBeModifiedDataMap[node.data.id]=node.data;
			}
		});

		//Set class for tree view
		$('.hierachy-tree td').removeClass("tree-row-delete");
		$('.hierachy-tree td').removeClass("tree-row-recrawl");
		$('.hierachy-tree td').removeClass("tree-row-import");
		for(var key in toBeModifiedDataMap){
			var option=toBeModifiedDataMap[key].option;
			var classOfTreeRow='';
			if (option === 'prune') {
				classOfTreeRow='tree-row-delete';
			}else if (option==='recrawl') {
				classOfTreeRow='tree-row-recrawl';
			}else{
				classOfTreeRow='tree-row-import';
			}
			$('.hierachy-tree td[idx="' + key + ' "]').addClass(classOfTreeRow);
		}

		//Set class for grid candidate
		this.gridCandidate.gridOptions.api.forEachNode(function(node, index){
			if (toBeModifiedDataMap[node.data.id] && toBeModifiedDataMap[node.data.id].option) {
				node.data.flag=toBeModifiedDataMap[node.data.id].option;
			}else{
				node.data.flag='normal';
			}
		});
		this.gridCandidate.gridOptions.api.redrawRows(true);
	}

	undo(data, source){
		source.clear(data);
		this.setRowStyle();
	}

	pruneHarvest(data){
		this._moveHarvest2ToBeModifiedList(data, 'prune');
	}

	recrawl(data){
		this._moveHarvest2ToBeModifiedList(data, 'recrawl');
	}

	import(data){
		this._moveHarvest2ToBeModifiedList(data, 'import');
	}

	_moveHarvest2ToBeModifiedList(data, action){
		if(!data){return;}

		var map={};
		var isPruneOutlink=false;
		for(var i=0; i< data.length; i++){
			var node=data[i];
			map[node.url]=node;
			if (isPruneOutlink || node.outlinkNum <= 0){
				continue;
			}
			isPruneOutlink=confirm('There are urls contain outlinks, would you like to ' + action + ' them?');
			if (!isPruneOutlink) {
				return;
			}
		}

		//Checking duplicated rows
		var isReplaceDuplicated=false;
		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			if (!isReplaceDuplicated && map[node.data.url]) {
				isReplaceDuplicated=confirm('There are duplicated urls in to be modified list, would you like to replace them?');
				if (!isReplaceDuplicated) {
					return;
				}
			}
		});

		// Add to 'to be modified' grid, and marked as new
		this.gridToBeModified.gridOptions.api.forEachNode(function(node, index){
			if(map[node.data.url]){
				delete map[node.data.url];
				node.data.option=action;
				node.data.flag='new';
			}else{
				node.data.flag='normal';
			}
		});
		this.gridToBeModified.gridOptions.api.redrawRows(true);

		map=JSON.parse(JSON.stringify(map));
		var dataset=[];
		for(var key in map){
			var node=map[key];
			node.flag='new';
			node.option=action;
			dataset.push(node);
		}
		this.gridToBeModified.gridOptions.api.updateRowData({add: dataset});

		this.setRowStyle();
	}

	showBulkPrune(){
		$('#bulkPruneMetadataFile').val(null);
		$('#label-bulk-prune-metadata-file').html('Choose file');
    $('#popup-window-bulk-prune').show();
	}

	showImport(data){
		$('#single-impot-mode').html('New');
		this.enableEditImport(true);

		if(data){
			$('#specifyTargetUrlInput').val(data.url);
		}else{
			$('#specifyTargetUrlInput').val('');
		}


		$('#sourceFile').val(null);
		$('#label-source-file').html('Choose file');

		var d=moment();
		var ds=d.format('YYYY-MM-DDTHH:mm');
		$("#datetime-local-customizard").val(ds);
		
    $('#popup-window-single-import').show();
    // this.processorModify.setNode(data);
	}

	editImport(node){
		if(!node){
			alert("Input parameter is missed");
			return;
		}

		this.processorModify.currentEdittingNode=node;

		$('#single-impot-mode').html('Edit');
		this.enableEditImport(false);

		$('#specifyTargetUrlInput').val(node.url);
		$('#popup-window-single-import input[name="customRadio"]').prop('checked', false);
		if(node.option.toLowerCase()==='file'){
			$('#customRadio1').prop('checked', true);
			$('#sourceFile').val(null);
			$('#label-source-file').html('Choose file');
		}else{
			$('#customRadio2').prop('checked', true);
			$('#importFromUrlInput').val(node.name);
		}

		//Modified Datetime
		var lastModified=node.lastModified;
		$('#popup-window-single-import input[name="r1"]').prop('checked', false);
		$('#popup-window-single-import input[flag=" + node.modifiedMode + "]').prop('checked', true);
		if(lastModified <= 0){ //TBC
			lastModified=moment();
		}else{
			lastModified=moment(lastModified);
		}

		var formatLastModified=lastModified.format('YYYY-MM-DDTHH:mm');
		$("#datetime-local-customizard").val(formatLastModified);

    	$('#popup-window-single-import').show();
	}

	//To make import input disabled or available 
	enableEditImport(flag){
		flag=!flag;
		$('#specifyTargetUrlInput').prop('disabled', flag);
		$('#popup-window-single-import input[name="customRadio"]').prop('disabled', flag);
		$('#sourceFile').prop('disabled', flag);
		$('#label-source-file').prop('disabled', flag);
		$('#importFromUrlInput').prop('disabled', flag);

	}

	showBulkImport(){
		this.processorModify.nextBulkImportTab(1);
		$('#popup-window-bulk-import').show();
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
		if(domainNames.length > 0){
			searchCondition.domainNames=domainNames.split();
		}

		var contentTypes=$("#queryContentType").val().trim();
		if(contentTypes.length > 0){
			searchCondition.contentTypes=contentTypes.split();
		}

		var statusCodes=$("#queryStatusCode").val().trim();
	    if(statusCodes.length > 0){
	    	searchCondition.statusCodes=statusCodes.split();
	    }

	    var urlNames=$("#queryUrlName").val().trim();
	    if(urlNames.length > 0){
	    	searchCondition.urlNames=urlNames.split();
	    }

	    var reqUrl=''
	    if (currentMainTab === 'candidate-query') {
	    	this.checkUrls(searchCondition, 'inspect');
	    }else if (currentMainTab === 'tree-url-names') {
	    	this.initTreeWithSearchCommand(searchCondition);
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

		var that=this;
		var url="/networkmap/search/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		if (flag==='inspect') {
			currentMainTab = 'candidate-query';
			$('#btn-group-main-tabs label[name="candidate-query"]').trigger('click');
		}
		g_TurnOnOverlayLoading();
		fetchHttp(url, searchCondition, function(response){
			if (response.rspCode != 0) {
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			if(flag==='prune'){
				that.pruneHarvest(data);
			}else if(flag==='inspect'){
				that.inspectHarvest(data);
			}

			g_TurnOffOverlayLoading();
		});
	}

	loadUrls(url){
		var that=this;
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
			}else{
				that.gridCandidate.setRowData(data);
			}
			g_TurnOffOverlayLoading();
		});
	}

	initTreeWithSeedUrls(){
		var that=this;
		g_TurnOnOverlayLoading();
		fetchHttp(this.uriSeedUrl, null, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			that.hierarchyTreeHarvestStruct.draw(data);
			that.setRowStyle();
			g_TurnOffOverlayLoading();
		});
	}

	initTreeWithSearchCommand(searchCommand){
		var reqUrl = "/networkmap/get/urls/cascaded-by-path?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber + '&title=';
		var that=this;
		g_TurnOnOverlayLoading();
		fetchHttp(reqUrl, searchCommand, function(response){
			if (response.rspCode != 0) {
				g_TurnOffOverlayLoading();
				alert(response.rspMsg);
				return;	 
	        }

			var data=JSON.parse(response.payload);
			that.hierarchyTreeUrlNames.drawWithDomain(data);
			that.setRowStyle();
			g_TurnOffOverlayLoading();
		});
	}

	exportData(data){
		g_TurnOnOverlayLoading();
		var req=[];
		// for (var i = 0; i< data.length; i++) {
		// 	var node=data[i];
		// 	if (node.viewType && node.viewType===2 && ) {}
		// 	data[i]
		// }
		var url="/curator/export/data?targetInstanceOid=" + this.jobId + "&harvestNumber=" + this.harvestResultNumber;
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

	insertImportData(dataset){
		this.gridToBeModified.insert(dataset);
		this.setRowStyle();
	}

	//Save and reindexing
	apply(){
		var dataset=this.gridImport.getAllNodes();
		var pruned=this.gridPrune.getAllNodes();
		for(var i=0; i<pruned.length; i++){
			var node={
				option: 'prune',
				url: pruned[i].url
			}
			dataset.push(node);
		}

		var applyCommand={
			targetInstanceId: this.jobId,
			harvestResultId: this.harvestResultId,
			harvestResultNumber: this.harvestResultNumber,
			newHarvestResultNumber: 0,
			dataset: dataset,
			provenanceNote: $('#provenance-note').val(),
		};

		g_TurnOnOverlayLoading();
		var that=this;
		var url="/modification/apply";
		fetchHttp(url, applyCommand, function(response){
			g_TurnOffOverlayLoading();
			if(response.respCode !== 0){
				alert(response.respMsg);
			}else{
				$("#popup-window-modify-harvest").hide();
				updateDerivedHarvestResults(response.derivedHarvestResult);
				// popupDerivedSummaryWindow(response.derivedHarvestResult.oid, response.derivedHarvestResult.harvestNumber);
			}
		});
	}
}
