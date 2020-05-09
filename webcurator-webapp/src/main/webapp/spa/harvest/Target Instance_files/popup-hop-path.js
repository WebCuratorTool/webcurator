class HopPath{
  constructor(container, jobId, harvestResultNumber){
    this.container=container;
    this.jobId=jobId;
    this.harvestResultNumber=harvestResultNumber;
    this.options={
                  nodes: {
                      shape: 'dot',
                      // size: 10,
                      borderWidth: 2,
                      color: '#98AFC7'
                   },
                  edges: {
                      width: 1,
                      arrows: 'to',
                      color: '#98AFC7'
                  },
                  layout: {
                      hierarchical: {
                          direction: "UD"
                      }
                  }
              };
  }

  draw(nodeId){
    var sourceUrl="/curator/networkmap/get/hop/path?job=" + this.jobId + "&harvestResultNumber=" + this.harvestResultNumber + "&id=" + nodeId;
    fetch(sourceUrl)
        .then((response) => {
            return response.json();
        })
        .then((data) => {
        this.drawHopPath(data);
        $('#popup-window-hop-path').show();
    });
  }

  drawHopPath(data){
    var dataSet={
      nodes:[],
      edges:[]
    };

    for(var i=0; i<data.length;i++){
      var dataNode=data[i];
      var node={
        id: dataNode.id,
        label: dataNode.url+"\n (Outlinks:" + dataNode.outlinks.length + " )",
        size: 5 + Math.log(dataNode.totSize+1)
      }

      if(dataNode.seed){
        // node.color='#A18648';
        node.shape='star';
        // node.color='#2A4B7C';
        // node.shape='hexagon';
      }else if(i===0){
        node.color='#00bfee';
        node.shape="box";
      }

      dataSet.nodes.push(node);
      // mapHopPath[dataNode.id]=dataNode;

      if(dataNode.parentId>0){
        var edge={
            from: dataNode.parentId,
            to: dataNode.id
        }
        dataSet.edges.push(edge);
      }
    }
    
    if(this.visHopPath){
      this.visHopPath.destroy();
    }

    var c = document.getElementById(this.container);
    this.visHopPath = new vis.Network(c, dataSet, this.options);
  }
}