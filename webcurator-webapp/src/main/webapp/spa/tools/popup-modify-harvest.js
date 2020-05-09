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
		this.sourceUrlRootUrls="/curator/networkmap/get/hierarchy/urls?job=" + jobId + "&harvestResultNumber=" + harvestResultNumber;
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
		fetch(this.sourceUrlRootUrls, {
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
			body: JSON.stringify(searchCondition)
		}).then((response) => {
		    if(response.status===200){
		      if (response.redirected) {
		        alert("Please login!")
		        return null;
		      }else{
		      	// console.log(response.json());
		        return response.json();
		      }
		    }
		}).then((data)=>{
		  if(data!=null){
		  	if($.ui.fancytree.getTree(that.container)){
				$.ui.fancytree.getTree(that.container).destroy();
			}
  			that.formatDataForTreeGrid(data);
  			console.log(data);
  			that.options.source=data;
  			$(that.container).fancytree(that.options);
		  }
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
	constructor(jobId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultNumber=harvestResultNumber;
		this.hierarchyTree=new HierarchyTree("#hierachy-tree", jobId, harvestResultNumber);
		this.gridCandidate=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-candidate', gridOptionsCandidate, contextMenuItemsUrlBasic);
		this.gridPrune=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-prune', gridOptionsPrune, contextMenuItemsPrune);
		this.gridImport=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-modify-import', gridOptionsImport, contextMenuItemsImport);
		this.gridImportPrepare=new CustomizedAgGrid(jobId, harvestResultNumber, '#grid-bulk-import-prepare', gridOptionsImportPrepare, contextMenuItemsImport);
		this.processorImport=new ImportModifyHarvestProcessor(jobId, harvestResultNumber);
		this.uriSeedUrl="/curator/networkmap/get/root/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		this.uriInvalidUrl="/curator/networkmap/get/malformed/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
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

	showImport(data){
		if(data){
			$('#specifyTargetUrlInput').val(data.url);
		}else{
			$('#specifyTargetUrlInput').val('');
		}


		$('#sourceFile').val(null);
		$('#label-source-file').html('Choose file');
		$('#importFromUrlInput').val('');
    	$('#popup-window-single-import').show();
    	// this.processorImport.setNode(data);
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

		$('#popup-window-loading').show();
		var that=this;
		var sourceUrl="/curator/networkmap/search/urls?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		fetch(sourceUrl, {
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
			body: JSON.stringify(searchCondition)
		}).then((response) => {
			return response.json();
		}).then((rawData) => {
			var data=formatStringArrayToJsonArray(rawData);
			if(flag==='prune'){
				that.pruneHarvest(data);
			}else if(flag==='inspect'){
				that.inspectHarvest(data);
			}else if(flag==='import-prune'){
				that.pruneHarvest(data);
				$('#tab-btn-import').trigger('click');
			}
			$('#popup-window-loading').hide();
			$('#popup-window-modify-harvest').show();
		});
	}

	loadUrls(uri){
		// var decision=confirm("The current candidates will be removed before loading URL. \n Please confirm you want to load data?");
		// if(!decision){
		// 	return;
		// }

		$('#popup-window-loading').show();
		var that=this;
		fetch(uri, {
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
		}).then((response) => {
			return response.json();
		}).then((rawData) => {
			var data=formatStringArrayToJsonArray(rawData);
			if(data.length===0){
				$('#popup-window-loading').hide();	
				alert('No data found!');
			}else{
				that.gridCandidate.setRowData(data);
				$('#popup-window-loading').hide();
			}
		});
	}

	loadSeedUrls(){
		this.loadUrls(this.uriSeedUrl);
	}

	loadInvalidUrls(){
		this.loadUrls(this.uriInvalidUrl);
	}


	applyRecrawl(){
	  for(var i=0; i<aryFiles.length; i++){
	    var file=aryFiles[i];
	    var reader = new FileReader();

	    reader.addEventListener("loadend", function () {
	      fetch("../../curator/tools/modify-import", { 
	        method: 'POST',
	        headers: {'Content-Type': 'application/json'},
	        body: reader.result });
	      // reader.removeEventListener("loadend");
	    });

	    reader.readAsDataURL(file);
	  }
	  aryFiles=[];
	}

	exportData(data){
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

		saveData(content, 'harvest_links.csv');
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
				targetUrl: pruned[i].url
			}
			dataset.push(node);
		}

		$('#popup-window-loading').show();
		var that=this;
		var sourceUrl="/curator/tools/apply?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		fetch(sourceUrl, {
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
			body: JSON.stringify(dataset)
		}).then((response) => {
			return response.json();
		}).then((rawData) => {
			
		});
	}
}
