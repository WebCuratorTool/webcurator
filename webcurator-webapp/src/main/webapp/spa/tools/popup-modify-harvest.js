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

var prunedDataMap={}, importDataMap={};
class HierarchyTree{
	constructor(container, jobId, harvestResultNumber){
		this.container=container;
		this.jobId=jobId;
		this.harvestResultNumber=harvestResultNumber;
		this.sourceUrlRootUrls="/networkmap/get/hierarchy/urls?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber;
		this.options={
			extensions: ["table", "wide"],
			checkbox: true,
			// autoScroll: true,
			// selectMode: 3,
			table: {checkboxColumnIdx: 0, nodeColumnIdx: 1},
			// viewport: {enabled: true, count: 3200},
			source: [],
			renderColumns: function(event, data) {
				var node = data.node;
				var $tdList = $(node.tr).find(">td");
				$tdList.eq(2).text(node.data.contentType);
				$tdList.eq(3).text(node.data.statusCode);
				$tdList.eq(4).text(formatContentLength(node.data.contentLength));
				$tdList.eq(5).text(node.data.totUrls);
				$tdList.eq(6).text(node.data.totSuccess);
				$tdList.eq(7).text(node.data.totFailed);
				$tdList.eq(8).text(formatContentLength(node.data.totSize));

				$(node.tr).attr("data", JSON.stringify(node.data));
				$(node.tr).attr("id", "tree-row-"+node.data.id);
			},
			
	    };

	    var that=this;
        $.contextMenu({
		        selector: this.container + ' tr', 
		        trigger: 'right',
		        reposition: true,
		        callback: function(key, options) {
		            var node=JSON.parse($(this).attr('data'));
		            contextMenuCallback(key, node, that, gPopupModifyHarvest);
		        },
		        items: contextMenuItemsUrlTree
    	});
	} 

	draw(dataset){
		var searchCondition=[];
		for(var i=0;i<dataset.length;i++){
			var node=dataset[i];
			searchCondition.push(node.id);
		}

		var that=this;
		fetchHttp(this.sourceUrlRootUrls, searchCondition, function(response){
			if($.ui.fancytree.getTree(that.container)){
				$.ui.fancytree.getTree(that.container).destroy();
			}
  			that.formatDataForTreeGrid(response);
  			console.log(response);
  			that.options.source=response;
  			$(that.container).fancytree(that.options);
		});
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
		var data=[];
		var rows=$(this.container+' tr[role="row"]');
		$.each(rows, function(index, value){
			var node=JSON.parse($(value).attr('data'));
			data.push(node);
		});
		return data;
	}

	// setOperationData(prunedDataMap, importDataMap){
	// 	this.prunedDataMap=prunedDataMap;
	// 	this.importDataMap=importDataMap;
	// }
}

class PopupModifyHarvest{
	constructor(jobId, harvestResultId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultId=harvestResultId;
		this.harvestResultNumber=harvestResultNumber;
		this.hierarchyTree=new HierarchyTree("#hierachy-tree", jobId, harvestResultNumber);
		this.gridCandidate=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-candidate', gridOptionsCandidate, contextMenuItemsUrlBasic);
		this.gridPrune=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-prune', gridOptionsPrune, contextMenuItemsPrune);
		this.gridImport=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-import', gridOptionsImport, contextMenuItemsImport);
		this.gridImportPrepare=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-bulk-import-prepare', gridOptionsImportPrepare, null);
		this.processorModify=new ModifyHarvestProcessor(jobId, harvestResultId, harvestResultNumber);
		this.uriSeedUrl="/networkmap/get/root/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		this.uriInvalidUrl="/networkmap/get/malformed/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
	}

	setRowStyle(){
		prunedDataMap={};
		this.gridPrune.gridOptions.api.forEachNode(function(node, index){
			prunedDataMap[node.data.id]=true;
		});
		importDataMap={};
		this.gridImport.gridOptions.api.forEachNode(function(node, index){
			importDataMap[node.data.id]=true;
		});

		//Set class for tree view
		$('#hierachy-tree td').removeClass("tree-row-delete");
		$('#hierachy-tree td').removeClass("tree-row-import");
		$('#hierachy-tree td').removeClass("tree-row-delete-import");
		for(var key in prunedDataMap){
			if(!importDataMap[key]){
				$("#tree-row-"+key+" td").addClass("tree-row-delete");
			}else{
				$("#tree-row-"+key+" td").addClass("tree-row-delete-import");
			}
		}
		for(var key in importDataMap){
			if(!prunedDataMap[key]){
				$("#tree-row-"+key+" td").addClass("tree-row-import");
			}
		}

		//Set class for grid candidate
		this.gridCandidate.gridOptions.api.forEachNode(function(node, index){
			var key=node.data.id;
			if(prunedDataMap[key]){
				if(!importDataMap[key]){
					node.data.flag='prune';
				}else{
					node.data.flag='prune-import';
				}
			}else if(importDataMap[key]){
				node.data.flag='import';
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

	pruneHarvestByUrls(dataset){
		if(!dataset || dataset.length===0){
			return;
		}

		var searchCondition={
          "domainNames": [],
          "contentTypes": [],
          "statusCodes": [],
          "urlNames": []
        }

        for(var i=0; i<dataset.length; i++){
        	searchCondition.urlNames.push(dataset[i].url);
        }

        this.checkUrls(searchCondition, 'import-prune');
	}

	pruneHarvest(data){
		$('#tab-btn-prune').trigger('click');
		if(!data){
			return;
		}

		var map={};
		for(var i=0; i< data.length; i++){
			var node=data[i];
			map[node.id]=node;
		}

		// Add to 'to be pruned' grid, and marked as new
		this.gridPrune.gridOptions.api.forEachNode(function(node, index){
			if(map[node.data.id]){
				delete map[node.data.id];
				node.data.flag='new';
			}else{
				node.data.flag='normal';
			}
		});
		this.gridPrune.gridOptions.api.redrawRows(true);

		map=JSON.parse(JSON.stringify(map));
		var dataset=[];
		for(var key in map){
			var node=map[key];
			node.flag='new';
			dataset.push(node);
		}
		this.gridPrune.gridOptions.api.updateRowData({ add: dataset});

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

	showOutlinks(data){
		if(!data){
			return;
		}
		$('#grid-modify-candidate').hide();
		$('#popup-window-hierarchy').show();

		this.hierarchyTree.draw(data);

		this.setRowStyle();
	}

	inspectHarvest(data){
		$('#popup-window-hierarchy').hide();
		$('#grid-modify-candidate').show();
		
		if(!data){
			return;
		}
		
		var map={};
		for(var i=0; i< data.length; i++){
			var node=data[i];
			map[node.id]=node;
		}

		this.gridPrune.gridOptions.api.forEachNode(function(node, index){
			if(map[node.data.id]){
				// delete map[node.data.id];
				map[node.data.id].flag='prune';
			}
		});
		this.gridImport.gridOptions.api.forEachNode(function(node, index){
			if(map[node.data.id]){
				// delete map[node.data.id];
				map[node.data.id].flag='import';
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

        this.checkUrls(searchCondition, 'inspect');
	}

	checkUrls(searchCondition, flag){
		if(!searchCondition || !flag){
			console.log('Invalid input, searchCondition='+searchCondition+', flag='+flag);
			return;
		}

		var that=this;
		var url="/networkmap/search/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		fetchHttp(url, searchCondition, function(response){
			var data=formatStringArrayToJsonArray(response);
			if(flag==='prune'){
				that.pruneHarvest(data);
			}else if(flag==='inspect'){
				that.inspectHarvest(data);
			}else if(flag==='import-prune'){
				that.pruneHarvest(data);
				$('#tab-btn-import').trigger('click');
			}
			$('#popup-window-modify-harvest').show();
		});
	}

	loadUrls(url){
		var that=this;
		fetchHttp(url, null, function(response){
			var data=formatStringArrayToJsonArray(response);
			if(data.length===0){
				$('#popup-window-loading').hide();	
				alert('No data found!');
			}else{
				that.gridCandidate.setRowData(data);
			}
		});
	}

	loadSeedUrls(){
		this.loadUrls(this.uriSeedUrl);
	}

	loadInvalidUrls(){
		this.loadUrls(this.uriInvalidUrl);
	}


	exportInspectData(data){
		var lines=[], row=[];
		row.push('URL');
		row.push('Type');
		row.push('Status');
		row.push('Size');
		row.push('Outlink-TotUrls');
		row.push('Outlink-Failed');
		row.push('Outlink-Success');
		row.push('Outlink-TotSize');
		var line=row.join('\t');
		lines.push(line);

		for(var i=0; i<data.length; i++){
			var d=data[i];
			row=[];
			row.push(d.url);
			row.push(d.contentType);
			row.push(d.statusCode);
			row.push(d.contentLength);
			row.push(d.totUrls);
			row.push(d.totFailed);
			row.push(d.totSuccess);
			row.push(d.totSize);
			lines.push(row.join('\t'));
		}
		
		var content=lines.join('\r\n');

		saveData(content, 'harvest_links_inspect.csv');
	}

	exportPruneData(data){
		var lines=[], row=[];
		row.push('URL');
		var line=row.join('\t');
		lines.push(line);

		for(var i=0; i<data.length; i++){
			var d=data[i];
			row=[];
			row.push(d.url);
			lines.push(row.join('\t'));
		}
		
		var content=lines.join('\r\n');

		saveData(content, 'harvest_links_prune.csv');
	}

	exportImportData(data){
		var lines=[], row=[];
		row.push('Option');
		row.push('Target');
		row.push('Source');
		row.push('ModifiedDate');
		var line=row.join('\t');
		lines.push(line);

		for(var i=0; i<data.length; i++){
			var d=data[i];
			row=[];
			row.push(d.option);
			row.push(d.url);
			row.push(d.name);
			row.push(d.lastModified);
			lines.push(row.join('\t'));
		}
		
		var content=lines.join('\r\n');

		saveData(content, 'harvest_links_import.csv');
	}

	insertImportData(dataset){
		this.gridImport.insert(dataset);
		// this.setRowStyle();
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

		$('#popup-window-loading').show();
		var that=this;
		var url="/modification/apply";
		fetchHttp(url, applyCommand, function(response){
			if(response.respCode !== 0){
				alert(response.respMsg);
			}else{
				$("#popup-window-modify-harvest").hide();
			}
		});
	}
}
