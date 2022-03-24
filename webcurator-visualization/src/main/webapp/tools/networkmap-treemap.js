partition = data => {
  const root = d3.hierarchy(data)
      .sum(d => d.value)
      .sort((a, b) => b.height - a.height || b.value - a.value);  
  return d3.partition()
      .size([height, (root.height + 1) * width / 3])
    (root);
}


// format = d3.format(",d");

width = 900;
height = 1110;

function format(d){
  // return d3.format(",d");
  return '';
}

class NetworkMapTreeMap{
  constructor(containerContentType, statKey){
  	this.containerContentType=containerContentType;
  	this.statKey=statKey;
    this.maxContentType;
    this.node;

    // append the svg object to the body of the page
    this.svg = d3.select("#"+this.containerContentType)
      .append("svg")
      .attr("viewBox", [0, 0, width, height])
      .style("font", "24px sans-serif");
  }

  setData(node){
    this.node=node;
  }

  draw(node){
    if(!node){
      node=this.node;
    }

    var statMap={};
    for(var i=0; i<node.statData.length; i++){
      var statNode=node.statData[i];
      var contentType=statNode.contentType;
      var statusCode=statNode.statusCode;
      var value=statNode[this.statKey];

      var contentTypeNode=statMap[contentType];
      if(!contentTypeNode){
        contentTypeNode={
          name: contentType,
          value: 0,
          children: {}
        };
        statMap[contentType]=contentTypeNode;
      }

      
      contentTypeNode.value=contentTypeNode.value + value;


      var statusCodeNode=contentTypeNode.children[statusCode];
      if(!statusCodeNode){
        statusCodeNode={
          name: statusCode,
          value: 0
        }
        contentTypeNode.children[statusCode]=statusCodeNode;
      }
      statusCodeNode.value = statusCodeNode.value + value;
    }


    var treeData={children:[]};
    var totalValue=0;
    for(var contentType in statMap){
        var contentTypeNode=statMap[contentType];
        var contentTypeTreeNode={
          'name': contentTypeNode.name + '(' + contentTypeNode.value + ')',
          'children': [],
        }
        treeData.children.push(contentTypeTreeNode);
        totalValue=totalValue+contentTypeNode.value;


        for(var statusCode in contentTypeNode.children){
          var statusCodeNode=contentTypeNode.children[statusCode];
          var statusCodeTreeNode={
            'name': 'StatusCode: ' + statusCodeNode.name + '(' + statusCodeNode.value + ')',
            'value': Math.ceil(1 + Math.log(statusCodeNode.value)),
          }

          contentTypeTreeNode.children.push(statusCodeTreeNode);
        }
    }

    treeData['name']=this.statKey + '(' + totalValue + ')';
    this.drawTreeMap(treeData);
  }


drawTreeMap(data){
  // console.log(data);
  // d3.select("#"+this.containerContentType).selectAll("g").remove();

    const root = partition(data);
    var focus = root;

  root.each(d => d.current = d);

  var color = d3.scaleOrdinal(d3.quantize(d3.interpolateRainbow, data.children.length + 1));

  this.svg.selectAll("g").remove();

const cell = this.svg
    .selectAll("g")
    .data(root.descendants())
    .join("g")
      .attr("transform", d => `translate(${d.y0},${d.x0})`);

  const rect = cell.append("rect")
      .attr("width", d => d.y1 - d.y0 - 1)
      .attr("height", d => rectHeight(d))
      .attr("fill-opacity", 0.6)
      .attr("fill", d => {
        if (!d.depth) return "#ccc";
        while (d.depth > 1) d = d.parent;
        return color(d.data.name);
      })
      .style("cursor", "pointer")
      .on("click", clicked);

  const text = cell.append("text")
      .style("user-select", "none")
      .attr("pointer-events", "none")
      .attr("x", 4)
      .attr("y", 30)
      .attr("fill-opacity", d => +labelVisible(d));

  text.append("tspan")
      .text(d => d.data.name);

  const tspan = text.append("tspan")
      .attr("fill-opacity", d => labelVisible(d) * 0.7)
      .text(d => ` ${format(d.value)}`);

  cell.append("title")
      .text(d => `${d.ancestors().map(d => d.data.name).reverse().join("/")}\n${format(d.value)}`);

  function clicked(p) {
    focus = focus === p ? p = p.parent : p;

    root.each(d => d.target = {
      x0: (d.x0 - p.x0) / (p.x1 - p.x0) * height,
      x1: (d.x1 - p.x0) / (p.x1 - p.x0) * height,
      y0: d.y0 - p.y0,
      y1: d.y1 - p.y0
    });

    const t = cell.transition().duration(750)
        .attr("transform", d => `translate(${d.target.y0},${d.target.x0})`);

    rect.transition(t).attr("height", d => rectHeight(d.target));
    text.transition(t).attr("fill-opacity", d => +labelVisible(d.target));
    tspan.transition(t).attr("fill-opacity", d => labelVisible(d.target) * 0.7);
  }
  
  function rectHeight(d) {
    return d.x1 - d.x0 - Math.min(1, (d.x1 - d.x0) / 2);
  }

  function labelVisible(d) {
    return d.y1 <= width && d.y0 >= 0 && d.x1 - d.x0 > 16;
  }
  
  return this.svg.node();
}

}

