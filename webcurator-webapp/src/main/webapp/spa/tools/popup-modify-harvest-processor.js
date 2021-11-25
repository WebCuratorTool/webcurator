class ModifyHarvestProcessor{
	constructor(jobId, harvestResultId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultId=harvestResultId;
		this.harvestResultNumber=harvestResultNumber;
		this.currentEdittingNode=null;
		this.chunk_size = 1*1024*1024; // 1Mbyte Chunk
		this.offset = 0;
	}

	bulkOpenMetadataFile(){
        setTimeout(function(){
    		$('#bulkImportMetadataFile').trigger('click');
		}, 200);
    }

    _validateToBeImportedData(dataset){
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

			var option=node.option;
			var modifiedMode=node.modifiedMode;
			var lastModifiedDate=node.lastModifiedDate;

			if(!target.toLowerCase().startsWith("http://") &&
				!target.toLowerCase().startsWith("https://")){
				node.respCode=-1;
				node.respMsg+="You must specify a valid target URL at line:" + (i+1);
				isValid=false;
			}

			if(option==="FILE"){
				if(modifiedMode==='TBC' || modifiedMode==='FILE'){
					node.lastModified=0;
				}else if (modifiedMode==='CUSTOM') {
					var dt=moment(lastModifiedDate);
					if(!dt){
						node.respCode=-1;
						node.respMsg+="Invalid modification datetime at line: " + (i+1);
						isValid=false;
					}

					node.lastModified=dt.valueOf();
				}else{
					node.respCode=1;
					node.respMsg+="Invalid modification mode or datetime at line: " + (i+1);
					isValid=false;
				}
			}else if(option==='PRUNE' || option==='RECRAWL'){
				node.modifiedMode='TBC';
				node.lastModifiedDate=0;
			}else{
				delete dataset[i];
				console.log('Skip invalid line: ' + (i+1));
			}
		}

		var gridImportNodes=gPopupModifyHarvest.gridToBeModified.getAllNodes();
		for(var i=0; i<gridImportNodes.length; i++){
			var key=gridImportNodes[i].url;
			if(map[key]){
				node.respCode=-1;
				map[key].respMsg+="Duplicated target URL at line: " + (map[key]+1);
				isValid=false;
			}
		}

		return isValid;
	}

	bulkUploadMetadataFile(file){
		var modifyHarvestProcessorInstance=this;
		var reader = new FileReader();
		reader.addEventListener("loadend", function () {
			var url="/bulk-import/parse?targetInstanceOid=" + modifyHarvestProcessorInstance.jobId + "&harvestNumber=" + modifyHarvestProcessorInstance.harvestResultNumber;
			var req={
				uploadFileContent: reader.result,
			};
			fetchHttp(url, req, function(rsp){
				$('#popup-window-bulk-import .overlay').hide();
				if (!rsp || rsp.rspCode !== 0) {
					console.log('Invalid response from WCT server');
					return;
				}

				var dataset=JSON.parse(rsp.payload);
				modifyHarvestProcessorInstance._validateToBeImportedData(dataset);
				gPopupModifyHarvest._appendAndMoveHarvest2ToBeModifiedList(dataset, function(data){
					modifyHarvestProcessorInstance._validateToBeImportedData(data);
					for (var i = data.length - 1; i >= 0; i--) {
						data[i].index=i;
					}
					gPopupModifyHarvest.gridImportPrepare.setRowData(data);
				});
			});
		});

		$('#popup-window-bulk-import .overlay').show();
		$('#popup-window-bulk-import').show();

		reader.readAsDataURL(file);
	}

	bulkAddData2ToBeImportedGrid(){
		var dataset=gPopupModifyHarvest.gridImportPrepare.getAllNodes();
		var valid=this._validateToBeImportedData(dataset);
		if(!valid){
			alert('Please correct or cancel the invalid rows');
			return;
		}
		gPopupModifyHarvest._moveHarvest2ToBeModifiedList(dataset);
		$('#popup-window-bulk-import').hide();
	}

	bulkCancelRowByRowIndex(rowIndex){
		gPopupModifyHarvest.gridImportPrepare.removeByDataIndex(rowIndex);

		var dataset=gPopupModifyHarvest.gridImportPrepare.getAllNodes();
		this._validateToBeImportedData(dataset);
		gPopupModifyHarvest.gridImportPrepare.setRowData(dataset);
	}

	singleImport(){
		var node;
		var currentRowIndex=parseInt($('#single-import-index').html());
		if (currentRowIndex > 0) {
			node=gPopupModifyHarvest.gridImportPrepare.getNodeByDataIndex(currentRowIndex);
		}else{
			node={url: $("#specifyTargetUrlInput").val(),};
		}

		// Check if targetURL exist in "to be imported" table
		this.tobeReplaceNode=gPopupModifyHarvest.gridToBeModified.getNodeByUrl(node.url);
		if (this.tobeReplaceNode) {
			var decision=confirm("The targetUrl has been exist in the ToBeImported table. \n Would you replace it?");
			if(!decision){
				return;
			}
		}

		if(!node.url.toLowerCase().startsWith("http://")
			&& !node.url.toLowerCase().startsWith("https://")){
			alert("You must specify a valid target URL. Starts with: http:// or https://");
			return;
		}
		var file=$('#sourceFile')[0].files[0];
		if(!file){
			alert("You must specify a source file name to import.");
			return;
		}
		node.uploadFileName=file.name;
		node.uploadFileLength=file.size;
		node.contentType=file.type;

		var modifiedMode=$("#radio-group-modified-date input[name='r1']:checked").attr("flag");
		node.modifiedMode=modifiedMode;
		if (modifiedMode.toUpperCase() === 'TBC') {
			node.lastModifiedDate=0;
		}else if (modifiedMode.toUpperCase() === 'FILE') {
			node.lastModifiedDate=file.lastModified;
		}else if (modifiedMode.toUpperCase() === 'CUSTOM') {
			var customModifiedDate=moment($("#datetime-local-customizard").val()).valueOf();
			node.lastModifiedDate=customModifiedDate;
		}else{
			alert('Invalid modified mode: ' + modifiedMode);
			return;
		}

		node.uploadFile=file;
		if (node.respCode === 1) {
			node.respCode = 0;
		}

		var dataset=[];
		dataset.push(node);

		if (currentRowIndex > 0) {
			gPopupModifyHarvest.gridImportPrepare.gridOptions.api.forEachNode(function(oldNode, index){
				if(oldNode.data.index===node.index){
					oldNode.data=node;
				}
			});
			gPopupModifyHarvest.gridImportPrepare.gridOptions.api.redrawRows(true);
		}else{
			gPopupModifyHarvest.modify(dataset, 'file');
		}

		$('#popup-window-single-import').hide();
	}

	singleRecrawl(){
		var node={url: $("#specifyTargetUrlInputForRecrawl").val(),};

		// Check if targetURL exist in "to be imported" table
		this.tobeReplaceNode=gPopupModifyHarvest.gridToBeModified.getNodeByUrl(node.url);
		if (this.tobeReplaceNode) {
			var decision=confirm("The targetUrl has been exist in the ToBeImported table. \n Would you replace it?");
			if(!decision){
				return;
			}
		}

		if(!node.url.toLowerCase().startsWith("http://")
			&& !node.url.toLowerCase().startsWith("https://")){
			alert("You must specify a valid target URL. Starts with: http:// or https://");
			return;
		}

		var dataset=[];
		dataset.push(node);

		gPopupModifyHarvest.modify(dataset, 'recrawl');

		$('#popup-window-single-recrawl').hide();
	}

	checkFilesExistAtServerSide(dataset, callback){	
		var url = "/modification/check-files?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber;
		fetchHttp(url, dataset, function(response){
			if(response.respCode!==1){
				var html=$('#tip-bulk-import-prepare-invalid').html();
				$('#tip-bulk-import-prepare').html(html);
			}else{
				$('#tip-bulk-import-prepare').html('All rows are valid.');
			}

			callback(response);
		});
	}
}