
class NetworkMapGrid{
  	constructor(container, key){
	  	this.container=container;
	  	this.key=key;

	  	var headerNameValue;
	  	if(key === 'statusCode'){
	  		headerNameValue='StatusCode';
	  	}else{
	  		headerNameValue='ContentType';
	  	}

	  	this.columnDefs=[
	  		{headerName: "", width:45, pinned: "left", headerCheckboxSelection: true, headerCheckboxSelectionFilteredOnly: true, checkboxSelection: true},
			{headerName: headerNameValue, field: "name", width: 160, filter: 'agNumberColumnFilter'},
			{headerName: "TotSize", field: "totSize", width: 120, type: "numericColumn", filter: 'agNumberColumnFilter', valueFormatter: formatContentLengthAg},
			{headerName: "TotURLs", field: "totUrls", width: 100, type: "numericColumn", filter: 'agNumberColumnFilter'}
	    ];

	    this.gridOptions = {
		  suppressRowClickSelection: true,
		  rowSelection: 'multiple',
		  defaultColDef: {
		    resizable: true,
		    filter: true,
		    sortable: true
		  },
		  columnDefs: this.columnDefs,
		  rowData: []
		};

		var that=this;
		$.contextMenu({
            selector: that.container + ' .ag-row', 
            callback: function(key, options) {
            	var searchCondition={
                  "domainNames": [],
                  "contentTypes": [],
                  "statusCodes": []
                }

            	var rowId=$(this).attr('row-id');
            	var node = that.grid.gridOptions.api.getRowNode(rowId); //getDisplayedRowAtIndex
				if(node){
					if(that.key === 'statusCode'){
						searchCondition.statusCodes.push(node.data.name);
					}else{
						searchCondition.contentTypes.push(node.data.name);
					}
				}
				if(that.domain.title){
					searchCondition.domainNames.push(that.domain.title);
					if(that.domain.children.length>0){
						searchCondition.domainLevel='high';
					}
				}

                networkmap.contextMenuCallback(key, searchCondition, that);

            },
            items: NetworkMap.contextMenuItemsGrid
        });


        // lookup the container we want the Grid to use
		var eGridDiv = document.querySelector(this.container);

		// create the grid passing in the div to use together with the columns & data we want to use
		this.grid = new agGrid.Grid(eGridDiv, this.gridOptions);
  	}

	draw(domain){
		if(!domain || !this.grid){
			return;
		}
		this.domain=domain;

		var dataset=this.summary(domain);
		this.grid.gridOptions.api.setRowData(dataset);
	}

	getSelectedNodes(){
		var searchCondition={
          "domainNames": [],
          "contentTypes": [],
          "statusCodes": []
        }

		var rows=this.grid.gridOptions.api.getSelectedRows();
		if(!rows || rows.length === 0){
			alert("Please select some rows!")
			return;
		}

		if(this.domain.title){
			searchCondition.domainNames.push(this.domain.title);
			if(this.domain.children.length>0){
				searchCondition.domainLevel='high';
			}
		}

		for(var i=0; i<rows.length; i++){
			var node=rows[i];
			if(this.key === 'statusCode'){
				searchCondition.statusCodes.push(node.name);
			}else{
				searchCondition.contentTypes.push(node.name);
			}
		}

		return searchCondition;
	}

	summary(node){
	    var statMap={};
	    for(var i=0; i<node.statData.length; i++){
	      var statNode=node.statData[i];
	      var key=statNode[this.key];
	      var totUrls=statNode['totUrls'];
	      var totSize=statNode['totSize'];

	      var statNode=statMap[key];
	      if(!statNode){
	        statNode={
	          name: key,
	          totUrls: 0,
	          totSize: 0,
	        };
	        statMap[key]=statNode;
	      }
	      statNode.totUrls=statNode.totUrls + totUrls;
	      statNode.totSize=statNode.totSize + totSize;
	    }

	    var statList=[];
	    for(var key in statMap){
	    	statList.push(statMap[key]);
	    }

	    return statList;
	}
}

