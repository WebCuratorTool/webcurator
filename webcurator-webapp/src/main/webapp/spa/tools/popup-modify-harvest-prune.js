class PruneModifyHarvestProcessor{
	constructor(jobId, harvestResultId, harvestResultNumber){
		this.jobId=jobId;
		this.harvestResultId=harvestResultId;
		this.harvestResultNumber=harvestResultNumber;
	}

	bulkPrune(){
		var file=$('#bulkPruneMetadataFile')[0].files[0];
		if(!file){
			alert("You must specify a metadata file name to prune.");
			return;
		}
		var ignoreInvalidURLsFlag=$("#checkbox-ignore-invalid-prune-urls").is(":checked");
		var ignoreDuplicatedURLsFlag=$("#checkbox-ignore-duplicated-prune-urls").is(":checked");

		var that=this;
		var reader = new FileReader();
		reader.addEventListener("loadend", function () {
			var searchCondition={
	          "domainNames": [],
	          "contentTypes": [],
	          "statusCodes": [],
	          "urlNames": []
	        }

	        var map={};
			var gridImportNodes=gPopupModifyHarvest.gridImport.getAllNodes();
			for(var i=0; i<gridImportNodes.length; i++){
				var key=gridImportNodes[i].url;
				map[key]=2;
			}

			var text=reader.result;
			var lines=text.split('\n');
			for(var i=0;i<lines.length;i++){
				var url=lines[i].trim();

				console.log(url);

				if(!url.toLowerCase().startsWith("http://") &&
					!url.toLowerCase().startsWith("https://")){
					if(!ignoreInvalidURLsFlag){
						alert("You must specify a valid URL at line:" + (i+1));
						return;
					}
				}

				if(map[url]===2 || map[url]===1){
					if(!ignoreDuplicatedURLsFlag){
						alert('Duplicated URL: ' + url);
						return;
					}
				}else{
					map[url]=1;
					searchCondition.urlNames.push(url);
				}
			}

			gPopupModifyHarvest.checkUrls(searchCondition, 'prune');

		});

		// reader.readAsDataURL(file);
		reader.readAsText(file);

	}

}