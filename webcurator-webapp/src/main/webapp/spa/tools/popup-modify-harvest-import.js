class ImportModifyHarvestProcessor{
	constructor(jobId, harvestResultId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultId=harvestResultId;
		this.harvestResultNumber=harvestResultNumber;
	}

	uploadFile(cmd, file, callback){
		var that=this;
		var reader = new FileReader();
		reader.addEventListener("loadend", function () {
			var req={
				content: reader.result,
				metadata: cmd
			};

			var url="/visualization/modification/upload-file-stream?job=" + that.jobId + "&harvestResultNumber=" + that.harvestResultNumber;
			fetch(url, { 
				method: 'POST',
				// headers: {'Content-Type': 'application/octet-stream'},
				headers: {'Content-Type': 'application/json'},
				body: JSON.stringify(req)
			}).then((response) => {
				return response.json();
			}).then((response) => {
				callback(cmd, response);
			});
		});

		reader.readAsDataURL(file);
		// reader.readAsArrayBuffer(file);
	}

	singleImport(resp, node){
		//Check result
		if(resp && resp.respCode!=1){
			alert(resp.respMsg);
			return;
		}

		node.uploadedFlag=1;
		
		$('#tab-btn-import').trigger('click');
		if(this.tobeReplaceNode){
			gPopupModifyHarvest.gridImport.gridOptions.api.updateRowData({remove: [this.tobeReplaceNode]});
		}
		gPopupModifyHarvest.gridImport.insert([node]);
		$('#popup-window-single-import').hide();

		if(node.pruneFlag){
			gPopupModifyHarvest.pruneHarvestByUrls([node]);
		}
	}

	insertRecrawlItem(){
		var that=this;
		var node={
			url: $("#specifyTargetUrlInput").val(),
		};

		// Check if targetURL exist in "to be imported" table
		this.tobeReplaceNode=null;
		gPopupModifyHarvest.gridImport.gridOptions.api.forEachNode(function(row, index){
			if(node.url===row.data.url){
				that.tobeReplaceNode=row.data;
			}
		});
		if (this.tobeReplaceNode) {
			var decision=confirm("The targetUrl has been exist in the ToBeImported table. \n Would you replace it?");
			if(!decision){
				return;
			}
		}

		node.pruneFlag=$("#checkbox-prune-of-single-import").is(":checked");

		// var option=$("#customRadio1").attr("");
		node.option=$("input[type='radio']:checked").attr("flag");
		if(node.option==='File'){
			if(!node.url.toLowerCase().startsWith("http://")){
				alert("You must specify a valid target URL. Starts with: http://");
				return;
			}
			var file=$('#sourceFile')[0].files[0];
			if(!file){
				alert("You must specify a source file name to import.");
				return;
			}
			node.name=file.name;
			node.length=file.size;
			node.contentType=file.type;
			node.lastModified=file.lastModified;
			// reqBody.file=file;

			var that=this;
			this.uploadFile(node, file, function(cmd, resp){
				that.singleImport(resp, cmd);
			});
		}else{
			if(!node.url.toLowerCase().startsWith("http://") &&
				!node.url.toLowerCase().startsWith("https://")){
				alert("You must specify a valid target URL.");
				return;
			}

			node.name=$('#importFromUrlInput').val();
			if(!node.name.toLowerCase().startsWith("http://") &&
				!node.name.toLowerCase().startsWith("https://")){
				alert("You must specify a valid source URL.");
				return;
			}

			// that.uploadFile(cmd);
			this.singleImport(null, node);
		}
	}


	bulkUploadFiles(){
		var dataset=gPopupModifyHarvest.gridImportPrepare.getAllNodes();
		var bulkFileNameMap={};
		for(var j=0; j<dataset.length; j++){
			var node=dataset[j];
			if(node.option.toLowerCase()!=='file' || node.respCode===1){
				continue;
			}
			var ary=bulkFileNameMap[node.name];
			if(!ary){
				ary=[node];
				bulkFileNameMap[node.name]=ary;
			}else{
				ary.push(node);
			}
		}

		var that=this;
		var files=$('#bulkImportContentFile')[0].files;
		for(var i=0; i<files.length; i++){
			var file=files[i];
			var node={name: file.name};

			var ary=bulkFileNameMap[node.name];
			if(!ary){
				console.log("Selected file not match any item to be bulk imported. Selected file name: " + cmd.srcName);
				continue;
			}

			for(var j=0; j<ary.length; j++){
				ary[j].length=file.size;
				ary[j].contentType=file.type;
				ary[j].lastModified=file.lastModified;
				ary[j].uploadedFlag=0;
			}

			this.uploadFile(node, file, function(cmd, response){
				var ary=bulkFileNameMap[cmd.name];
				if(!ary){
					console.log("System error. Selected file name: " + cmd.name);
					return;
				}

				for(var j=0; j<ary.length; j++){
					ary[j].respCode=response.respCode;
					ary[j].respMsg=response.respMsg;
					ary[j].uploadedFlag=1;
				}
				gPopupModifyHarvest.gridImportPrepare.gridOptions.api.redrawRows(true);

				delete bulkFileNameMap[cmd.name];
				var unUploadedNumber=0;
				for(var key in bulkFileNameMap){
					unUploadedNumber++;
				}
				if(unUploadedNumber>0){
					var html=$('#tip-bulk-import-prepare-invalid').html();
					$('#tip-bulk-import-prepare').html(html);
				}else{
					$('#tip-bulk-import-prepare').html('All rows are valid.');
				}
			});
		}

		gPopupModifyHarvest.gridImportPrepare.gridOptions.api.redrawRows(true);
		$('#bulkImportContentFile').val(null);
	}

	bulkImportStep0(){
		var file=$('#bulkImportMetadataFile')[0].files[0];
		if(!file){
			alert("You must specify a metadata file name to import.");
			return;
		}
		var that=this;
		var reader = new FileReader();
		reader.addEventListener("loadend", function () {
			var newBulkTargetUrlMap={}; //Using a map to check duplicated target urls;
			var dataset=[];
			var text=reader.result;
			var columnSeparator=$('#bulk-import-column-separator').val();
			if(columnSeparator==='Tab'){
				columnSeparator='\t';
			}

			var lines=text.split('\n');
			for(var i=0;i<lines.length;i++){
				var line=lines[i].trim();

				console.log(line);

				var columns=line.split(columnSeparator); //Type, Target, Source, Datetime
				if(columns.length!==4){
					alert("Invalid metadata format");
					return;
				}

				var type=columns[0].trim(), target=columns[1].trim(), source=columns[2].trim(), modifydatetime=columns[3].trim();
				var node={
					option: type,
					url: target,
					name: source,
					lastModified: modifydatetime,
					uploadedFlag: -1
				}

				if(newBulkTargetUrlMap[target]){
					alert("Duplicated target URL at line: " + (i+1));
					return;
				}else{
					newBulkTargetUrlMap[target]=i;
				}


				if(type.toLowerCase()==="file"){
					node.option="File";
					if(!target.toLowerCase().startsWith("http://")){
						alert("You must specify a valid target URL at line:" + (i+1) + ". URL starts with: http://");
						return;
					}
					dataset.push(node);
				}else if(type.toLowerCase()==='url'){
					node.option='URL';
					if(!target.toLowerCase().startsWith("http://") &&
						!target.toLowerCase().startsWith("https://")){
						alert("You must specify a valid target URL at line:" + (i+1));
						return;
					}

					if(!source.toLowerCase().startsWith("http://") &&
						!source.toLowerCase().startsWith("https://")){
						alert("You must specify a valid source URL at line:" + (i+1));
						return;
					}
					dataset.push(node);
				}else{
					//alert("Import type must be 'file' or 'url' at line: " + (i+1));
					//return;
					console.log('Skip invalid line: ' + line);
				}

				var d=new Date(modifydatetime);
				if(!d){
					alert("Invalid modification datetime at line: " + (i+1));
					return;
				}

				node.lastModified=d.getTime();
			}


			var gridImportNodes=gPopupModifyHarvest.gridImport.getAllNodes();
			for(var i=0; i<gridImportNodes.length; i++){
				var key=gridImportNodes[i].url;
				if(newBulkTargetUrlMap[key]>=0){
					alert("Duplicated target URL at line: " + (newBulkTargetUrlMap[key]+1));
					return;
				}
			}


			that.checkFilesExistAtServerSide(dataset, function(response){
				that.nextBulkImportTab(0);
				gPopupModifyHarvest.gridImportPrepare.setRowData(response.metadataDataset);
			});

		});

		// reader.readAsDataURL(file);
		reader.readAsText(file);

	}

	bulkImportStep1(){
		var that=this;
		var dataset=gPopupModifyHarvest.gridImportPrepare.getAllNodes();
		this.checkFilesExistAtServerSide(dataset, function(response){
			if(response.respCode!==1){
				gPopupModifyHarvest.gridImportPrepare.setRowData(response.metadataDataset);
				alert('Some files are missing. Can not proceed.');
				return;
			}else{
				$('#popup-window-bulk-import').hide();
				$('#tab-btn-import').trigger('click');
				that.nextBulkImportTab(1);
				gPopupModifyHarvest.insertImportData(response.metadataDataset);
				var pruneFlag=$("#checkbox-prune-of-bulk-import").is(":checked");
				if(pruneFlag){
					gPopupModifyHarvest.pruneHarvestByUrls(response.metadataDataset);					
				}
			}
		});
		
	}


	checkFilesExistAtServerSide(dataset, callback){
		var that=this;
		fetch("/visualization/modification/check-files?job=" + that.jobId + "&harvestResultNumber=" + that.harvestResultNumber, { 
			method: 'POST',
			headers: {'Content-Type': 'application/json'},
			body: JSON.stringify(dataset)
		}).then((response) => {
			return response.json();
		}).then((response) => {
			if(response.respCode!==1){
				var html=$('#tip-bulk-import-prepare-invalid').html();
				$('#tip-bulk-import-prepare').html(html);
			}else{
				$('#tip-bulk-import-prepare').html('All rows are valid.');
			}

			callback(response);
			
		});
	}

	nextBulkImportTab(step){
      step=(step+1) % 2;
      $('.tab-bulk-import').hide();
      $('#tab-bulk-import-'+step).show();
      $('#btn-bulk-import-submit').attr('step', step);
      if(step===0){
        $('#bulkImportMetadataFile').val(null);
        $('#label-bulk-import-metadata-file').html('Choose file');
        $('#bulkImportContentFile').val(null);
        $('#btn-bulk-import-submit').html('Next');        
      }else{
        $('#btn-bulk-import-submit').html('Re-crawl');
        $('#btn-bulk-import-submit').attr('status', 'recrawl');
      }
    }

}