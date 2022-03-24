


// format = d3.format(",d");

function format(d){
  // return d3.format(",d");
  return '';
}

class NetworkMapMenuMap{
  constructor(container, key1, key2){
  	this.container=container;
  	this.key1=key1;
    this.key2=key2;

    this.defaultHeight=2100;
    this.width=800;
    this.height=2100;
    this.minCellHeight=100;
    this.maxLines=0;
    this.parentNodeWidth=100;

    this.partition = data => {
        const root = d3.hierarchy(data)
            .sum(d => d.value)
            .sort((a, b) => b.height - a.height || b.value - a.value);  
        return d3.partition()
            .size([this.height, (root.height + 1) * this.width / 2])
          (root);
      };
    this.format = d3.format(",d");

    this.svg = d3.select(this.container).append("svg");
  }
  
  draw(node){
    this.height=this.defaultHeight;
    this.maxLines=0;
    var statMap=this.summary(node);
    
    this.rootNode=this.map2list(statMap);
    this.rootNode.name=node.title?node.title:'Root';

    var root = d3.hierarchy(this.rootNode);
    this.drawMenuMap(root);
  }

  
  drawMenuMap(parentNode){
    if(!parentNode || !parentNode.children || parentNode.children.length===0){
      return;
    }

    if(!parentNode.data.name){
      parentNode.data.name='Root';
    }

    var data=[];
    data.push(parentNode);
    data=data.concat(parentNode.children);

    var color = d3.scaleOrdinal(d3.quantize(d3.interpolateRainbow, data.length));


    this.height = Math.max(this.defaultHeight, this.minCellHeight*data.length);

    this.appendPosition(parentNode);

    this.svg.selectAll("g").remove();
    this.svg.attr("viewBox", [0, 0, this.width, this.height])
      .attr("preserveAspectRatio", "xMinYMin meet")
      .style("font", "24px sans-serif");
    
    const cell_left=this.svg.append("g")
    .attr("transform", d => `translate(0,0)`);

    const rect_left=cell_left.append("rect")
    .attr("width", this.parentNodeWidth - 1)
    .attr("height", this.height -1)
    .attr("fill-opacity", 0.6)
    .attr("fill", color(parentNode.data.name))
    .attr("id", 0)
    .style("cursor", "pointer")
    .on("click", clicked);

    const text_left=cell_left.append("text")
    .style("user-select", "none")
    .attr('transform',"translate(60, 60) rotate(90)")
    .attr("pointer-events", "none")
    .attr("x", 4)
    .attr("y", 30)
    .text(parentNode.data.name + ' URLs:' + parentNode.data.totUrls + ' Size:' + parentNode.data.totSize)
    .style("font", "36px sans-serif");


    for(var i=1; i<data.length; i++){
      var d=data[i];
      const cell = this.svg
      .append("g")
      .attr("transform", `translate(${d.x0},${d.y0})`);

      const rect = cell.append("rect")
      .attr("width", d.x1 - d.x0 - 1)
      .attr("height", d.y1 - d.y0 -1)
      .attr("fill-opacity", 0.6)
      .attr("fill", color(d.data.name))
      .attr("id", i)
      .style("cursor", "pointer")
      .on("click", clicked);

      const text = cell.append("text")
      .style("user-select", "none")
      .attr("pointer-events", "none")
      .attr("x", 4)
      .attr("y", 30);


      text.append("tspan")
      .text(d.data.name)
      .append("tspan").attr('dy', '1.2em').attr('x', '0')
      .text('URLs: ' + d.data.totUrls)
      .append("tspan").attr('dy', '1.2em').attr('x', '0')
      .text('Size: ' + d.data.totSize);
    }
    
    // this.svg.selectAll('g').data(parentNode);

    const that=this;
    function clicked(p,index){
      var id=$(this).attr('id');
      if(id == 0){
        that.drawMenuMap(data[id].parent);
      }else{
        that.drawMenuMap(data[id]);
      }
    }
  }

  summary(node){
    var statMap={};
    for(var i=0; i<node.statData.length; i++){
      var statNode=node.statData[i];
      var label1=statNode[this.key1];
      var label2=statNode[this.key2];
      var totUrls=statNode['totUrls'];
      var totSize=statNode['totSize'];

      var nodeLevel1=statMap[label1];
      if(!nodeLevel1){
        nodeLevel1={
          name: label1,
          totUrls: 0,
          totSize: 0,
          children: {},
          length: 0
        };
        statMap[label1]=nodeLevel1;
      }
      nodeLevel1.totUrls=nodeLevel1.totUrls + totUrls;
      nodeLevel1.totSize=nodeLevel1.totSize + totSize;


      var nodeLevel2=nodeLevel1.children[label2];
      if(!nodeLevel2){
        nodeLevel2={
          name: label2,
          totUrls: 0,
          totSize: 0,
        };
        nodeLevel1.children[label2]=nodeLevel2;
        nodeLevel1.length=nodeLevel1.length+1;
      }
      nodeLevel2.totUrls=nodeLevel2.totUrls + totUrls;
      nodeLevel2.totSize=nodeLevel2.totSize + totSize;
    }

    return statMap;
  }

  map2list(statMap){
    var root={
      totUrls: 0,
      totSize: 0,
      children: []
    };
    
    var totUrls=0, totSize=0;
    for(var label1 in statMap){
      var nodeLevel1={
        name: label1,
        totUrls: statMap[label1].totUrls,
        totSize: statMap[label1].totSize,
        children: [],
        colorName: label1
      };

      root.totUrls+=nodeLevel1.totUrls;
      root.totSize+=nodeLevel1.totSize;

      root.children.push(nodeLevel1);

      for (var label2 in statMap[label1].children) {
        var nodeLevel2=statMap[label1].children[label2];
        nodeLevel1.children.push({
          name: label2,
          totUrls: nodeLevel2.totUrls,
          totSize: nodeLevel2.totSize,
          children: [],
          colorName: label1
        });
      }
    }

    return root;
  }

  appendPosition(parentNode){
    var dataset=parentNode.children;
    var sliceHeight=this.height/dataset.length;
    for(var i=0;i<dataset.length;i++){
      var node=dataset[i];
      node['x0']=this.parentNodeWidth;
      node['x1']=this.width;
      node['y0']=i*sliceHeight;
      node['y1']=(i+1)*sliceHeight;
    }
  }
}

