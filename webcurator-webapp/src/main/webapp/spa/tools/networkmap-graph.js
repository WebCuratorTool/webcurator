class NetworkMapGraph{
  constructor(containerName){
    this.containerName='#'+containerName;
    this.container=document.getElementById(containerName);
    this.options={
      nodes: {shape: 'dot', size: 10, borderWidth: 1, color: '#98AFC7'},
      edges: {width: 1, arrows: 'to', color: '#98AFC7'},
      physics: {
        enabled: true,
        forceAtlas2Based: {
            gravitationalConstant: -26,
            centralGravity: 0.005,
            springLength: 70,
            springConstant: 0.18
        },
        minVelocity: 0.75,
        maxVelocity: 146,
        solver: 'forceAtlas2Based',
        timestep: 0.35,
        stabilization: {
            enabled:true,
            iterations:300,
            updateInterval:25,
            onlyDynamicEdges: false,
            fit: true
        }
      },
      groups:{
        'seed-high': {color: '#566573', shape: 'star'},
        'seed-lower': {color: '#ffbf00', shape: 'star'},
        'seed-expanded-lower': {color: '#C24125', shape: 'star'},
        'high': {color: '#566573', shape: 'dot'},
        'lower': {color: '#ffbf00', shape: 'dot'},
        'expanded-lower': {color: '#C24125', shape: 'dot'},
        'expanded': {shape: 'circularImage', image: '../images/expand.png'}
      }
    };

    this.stabilized=false;

    this.viewOptions={
      scale: -1,
      position: {x:0, y:0}
    };

    this.originalData={};
    this.originalDataMap=null;


    var that=this;
    $.contextMenu({
            selector: that.containerName, 
            trigger: 'none',
            reposition: false,
            callback: function(key, options) {
                var node=that.getSelectedNode();
                var searchCondition={
                  "domainNames": [],
                  "contentTypes": [],
                  "statusCodes": []
                }

                if(node){
                  searchCondition.domainNames.push(node.title);
                  if(node.children.length > 0){
                    searchCondition.domainLevel='high';
                  }
                }

                networkmap.contextMenuCallback(key, searchCondition, that);
            },
            position: function(opt, x, y){
                // console.log('context menu position: (' + that.x + ', ' + that.y + ')');
                var offset = opt.$menu.position();
                offset.top=that.y;
                offset.left=that.x;
                opt.$menu.css(offset);
            },
            items: NetworkMap.contextMenuItemsGraph
        });
  }

  draw(data){
    $('#popup-window-loading').show();

    this.originalData=data;
    this.dataMap=this.initialDataSet(data);
  
    if(this.network){
      this.network.destroy();
    }
    this.network = new vis.Network(this.container, this.formatDataSet(), this.options);

    var that=this;
    //Event: doubleClic
    this.network.on("click", function (params) {
        console.log(params);

        if(params.nodes.length<=0){
          networkmap.reset();
        }else{
          var nodeId=params.nodes[0];
          networkmap.switchNode(nodeId);
        }
    });

    //Event: doubleClic
    this.network.on("doubleClick", function (params) {
        if(params.nodes.length<=0){
          return;
        }
        var nodeId=params.nodes[0];
        that.toggleParentNode(nodeId);
    });

    //Event: statbilized
    this.network.on("stabilized", function(){
      $('#popup-window-loading').hide();

      if(!that.stabilized){
        console.log("stabilized");
        // that.network.setOptions({physics: false});
        that.options.physics.stabilization.iterations=6;
        that.network.setOptions(that.options);
        that.attachPositions();
        if(!that.originalDataMap){
          that.originalDataMap=JSON.parse(JSON.stringify(that.dataMap));
        }
        that.stabilized=true;
      }
    });

    //========Revover the scale and position after pyhsics simulation========
    this.network.on("release", function(params){
      that.viewOptions.scale=that.network.getScale();
      that.viewOptions.position=that.network.getViewPosition();
    });
    this.network.on("initRedraw", function(){
      if(that.viewOptions.scale > 0){
        that.network.moveTo(that.viewOptions);
        that.viewOptions.scale = -1;
      }    
    });
    //========================================================================
    this.network.on("oncontext", function(params){
      params.event.preventDefault();

      if(!params || !params.pointer || !params.pointer.DOM){
        return;
      }

      // var node = that.network.getNodeAt({x: params.pointer.DOM.x, y: params.pointer.DOM.y});
      var node = that.network.getNodeAt(params.pointer.DOM);

      console.log(node);

      if(!node){
        return;
      }

      networkmap.switchNode(node);
      that.network.selectNodes([node]);

      that.x=params.event.x;
      that.y=params.event.y;
      console.log('vis network position: (' + that.x + ', ' + that.y + ')');
      $(that.containerName).contextMenu();
    }); 
  }

  /**Initial data*/
  initialDataSet(data){
    var dataMap={};
    for(var i=0; i<data.length;i++){
      var node=data[i];
      var group="high";
      if(node.children.length===1){
        node=node.children[0];
        group="lower";
      }

      node.group=group;
      dataMap[node.id]=node;
    }
    return dataMap;
  }

  /**Format data to dataset acceptable to vis network*/
  formatDataSet(dataMap=this.dataMap){
    var dataSet={
          nodes:[],
          edges:[]
        };

    for(var key in dataMap){
      var dataNode=dataMap[key];
      var node={
        id: dataNode.id,
        size: 5 + Math.log(dataNode.totSize+1),
        group: dataNode.group
      }

      if(dataNode.children.length>0){
        node['label']= dataNode.title +"(*"+dataNode.children.length+")";
      }else{
        node['label']= dataNode.title;
      }


      if (dataNode.x && dataNode.y) {
        node.x=dataNode.x;
        node.y=dataNode.y;
      }

      if(dataNode.seed && (node.group!=="expanded")){
        node.group="seed-"+node.group;
      }

      dataSet.nodes.push(node);

      this.dataMap[dataNode.id]=dataNode;


      if(dataNode.group==="expanded"){ //Only add links to it's direct children
        for(var j=0;j<dataNode.children.length;j++){
          var outlinkNode=dataNode.children[j];
          if(!outlinkNode || outlinkNode.parentId!==dataNode.id){
            continue;
          }

          var edge={
            from: dataNode.id,
            to: outlinkNode.id,
            dashes: true,
            width: 1,
            color:{color: 'rgba(30,30,30,0.5)'}
          }

          dataSet.edges.push(edge);
        }
      }else{
        for(var j=0;j<dataNode.outlinks.length;j++){
          var outlinkNode=this.dataMap[dataNode.outlinks[j]];
          if(!outlinkNode || outlinkNode.group==="expanded"){
            continue;
          }

          var edge={from: dataNode.id, to: outlinkNode.id}
          dataSet.edges.push(edge);
        }
      }
    }
    return dataSet;
  }

  /**Set position to data set*/
  attachPositions(){
    var dataPositionArray=this.network.getPositions();
    for(var key in dataPositionArray){
      var pos=dataPositionArray[key];
      var node=this.dataMap[key];
      node.x=pos.x;
      node.y=pos.y;
    }
    // console.log(this.dataMap);
  }

  toggleParentNode(parentId){
    var parentNode=this.dataMap[parentId];
    if (!parentNode) {
      console.log("Node does not exist, parentId="+parentId);
      return;
    }

    if (parentNode.group==="expanded") {
      parentNode.group="high";
      this.collapseParentNode(parentNode);
    }else if(parentNode.group==="high"){
      parentNode.group="expanded";
      this.expandParentNode(parentNode);
    }
  }

  collapseParentNode(parentNode){
     var children=parentNode.children;
     for(var i=0;i<children.length;i++){
        var child=children[i];
        delete this.dataMap[child.id];
     }

    var dataset=this.formatDataSet();
    this.network.setData(dataset);
  }

  /**Expand a high level node*/
  expandParentNode(parentNode){
    if (parentNode.children.length<=1) { //Nothing to expand
      return;
    }

    var minX=parentNode.x, minY=parentNode.y, maxX=parentNode.x, maxY=parentNode.y;
    for(var key in this.dataMap){
      var node=this.dataMap[key];
      if(node.x<minX){
        minX=node.x;
      }
      if(node.y<minY){
        minY=node.y;
      }
      if(node.x>maxX){
        maxX=node.x;
      }
      if(node.y>maxY){
        maxY=node.y;
      }
    }


    //To empty 10% for children
    var width=maxX - minX, height=maxY - minY;
    
    if (parentNode.x===0 || parentNode.y===0) {
      this.windFromCenter(width, height, parentNode);
    }else{
      this.windToSigleDirection(width, height, parentNode);
    }

    // this.scale=this.network.getScale();

    var dataset=this.formatDataSet();
    this.network.setData(dataset);
  }


  //Move the existing from center
  windFromCenter(width, height, parentNode){
    var subWidth=width/5, subHeight=height/5;
    var halfSubWidth=subWidth/2, halfSubHeight=subHeight/2;

    //Moving other nodes
    // for(var key in this.dataMap){
    //   var node=this.dataMap[key];
    //   if(node.x<parentNode.x){
    //     node.x=node.x - halfSubWidth;
    //   }else{
    //     node.x=node.x + halfSubWidth; 
    //   }

    //   if(node.y<parentNode.y){
    //     node.y=node.y - halfSubHeight;
    //   }else{
    //     node.y=node.y + halfSubHeight;
    //   }
    // }

    // var scale=this.network.getScale();
    var children=parentNode.children;
    
    parentNode.x=parentNode.x - subWidth;
    parentNode.y=parentNode.y - subHeight;
    for(var i=0;i<children.length;i++){
      var child=children[i];
      var pos=generateRandomPosition(subWidth, subHeight);
      child.x=parentNode.x+pos.w;
      child.y=parentNode.y+pos.h;
      child.group="expanded-lower";
      this.dataMap[child.id]=child;
    }
  }


  //Moving the existing to one direct
  windToSigleDirection(width, height, parentNode){
    var signX=parentNode.x/Math.abs(parentNode.x), signY=parentNode.y/Math.abs(parentNode.y);
    var subWidth=signX*width/5, subHeight=signY*height/5;

    //Moving other nodes
    // for(var key in this.dataMap){
    //   var node=this.dataMap[key];
    //   var signXX=node.x/Math.abs(node.x), signYY=node.y/Math.abs(node.y);
    //   if(signX===signXX && signY===signYY && Math.abs(node.x)>Math
    //     .abs(parentNode.x) && Math.abs(node.y)>Math.abs(parentNode.y)){
    //     node.x=node.x + subWidth; 
    //     node.y=node.y + subHeight;
    //   }
    // }

    var children=parentNode.children;
    for(var i=0;i<children.length;i++){
      var child=children[i];
      var pos=generateRandomPosition(subWidth, subHeight);
      child.x=parentNode.x+pos.w+5*signX;
      child.y=parentNode.y+pos.h+5*signY;
      child.group="expanded-lower";
      this.dataMap[child.id]=child;
    }
  }

  reload(){
    // this.viewOptions.scale=this.network.getScale();
    this.stabilized=false;
    this.options.physics.stabilization.iterations=300;
    this.network.setOptions(this.options);
    this.draw(this.originalData);
  }

  reset(){
    // this.viewOptions.scale=this.network.getScale();
    this.dataMap=JSON.parse(JSON.stringify(this.originalDataMap));
    this.network.setData(this.formatDataSet());
  }

  collapseAll(){
    this.viewOptions.scale=this.network.getScale();
    var expandedNode=[];
    for(var key in this.dataMap){
      var parentNode=this.dataMap[key];
      if(parentNode.group!=='expanded'){
        continue;
      }
      parentNode.group='high';

      var children=parentNode.children;
      for(var i=0;i<children.length;i++){
        var child=children[i];
        delete this.dataMap[child.id];
      }
    }

    var dataset=this.formatDataSet();
    this.network.setData(dataset);
  }

  getSelectedNodes(){
    var selectedNodeIds=networkmap.graph.network.getSelectedNodes();
    var selectedNodes=[];
    for(var i=0;i<selectedNodeIds.length;i++){
      var node=this.dataMap[selectedNodeIds[i]];
      if(node){
        selectedNodes.push(node);
      }
    }
    return selectedNodes;
  }

  getSelectedNode(){
    var selectedNodes=this.getSelectedNodes();
    if (selectedNodes.length==1) {
      return selectedNodes[0];
    }
    return null;
  }

  save2image(){
    var canvas=$(this.containerName + ' canvas')[0];
    var image=canvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
    // var imageData=image.replace("data:image/octet-stream;base64,", "");
    // var rawData=window.atob(imageData);
    // var image=canvas.data;
    // saveData(rawData, "networkmap.png");
    var link = document.createElement("a");

    link.setAttribute("href", image);
    link.setAttribute("download", "networkmap.png");
    link.click();
  }
}

//Genereate the position for children node
function generateRandomPosition(width, height){
  var node={};
  node.w=Math.floor(Math.random() * width);
  node.h=Math.floor(Math.random() * height);  
  return node;
}


