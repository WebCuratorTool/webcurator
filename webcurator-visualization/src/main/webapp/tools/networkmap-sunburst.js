// d3 = require("d3@5");
partition = data => {
  const root = d3.hierarchy(data)
      .sum(d => d.value)
      .sort((a, b) => b.value - a.value);
  return d3.partition()
      .size([2 * Math.PI, root.height + 1])
    (root);
};


format = d3.format(",d");

width = 900;

radius = width / 6;


arc = d3.arc()
    .startAngle(d => d.x0)
    .endAngle(d => d.x1)
    .padAngle(d => Math.min((d.x1 - d.x0) / 2, 0.005))
    .padRadius(radius * 1.5)
    .innerRadius(d => d.y0 * radius)
    .outerRadius(d => Math.max(d.y0 * radius, d.y1 * radius - 1));



class NetworkMapSunBurst{
  constructor(containerContentType, statKey){
  	this.containerContentType=containerContentType;
  	this.statKey=statKey;
    this.maxContentType;
    this.node;

     // append the svg object to the body of the page
    this.svg = d3.select("#"+this.containerContentType)
      .append("svg")
      .attr("viewBox", [0, 0, width, width])
      .style("font", "24px sans-serif");

  }

  setData=function(node){
    this.node=node;
  }

draw=function(node){
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
            'name': statusCodeNode.name + '(' + statusCodeNode.value + ')',
            'value': Math.ceil(1 + Math.log(statusCodeNode.value)),
          }

          contentTypeTreeNode.children.push(statusCodeTreeNode);
        }
    }

    treeData['name']=this.statKey + '(' + totalValue + ')';
    this.drawTreeMap3(treeData);
  }


drawTreeMap3(data){
  console.log(data);
  // d3.select("#"+this.containerContentType).selectAll("g").remove();

    const root = partition(data);

  root.each(d => d.current = d);

  var color = d3.scaleOrdinal(d3.quantize(d3.interpolateRainbow, data.children.length + 1));
  this.svg.selectAll("g").remove();

  const g = this.svg.append("g").attr("transform", `translate(${width / 2},${width / 2})`);


  const path = g.append("g")
    .selectAll("path")
    .data(root.descendants().slice(1))
    .join("path")
      .attr("fill", d => { while (d.depth > 1) d = d.parent; return color(d.data.name); })
      .attr("fill-opacity", d => arcVisible(d.current) ? (d.children ? 0.6 : 0.4) : 0)
      .attr("d", d => arc(d.current));

  path.filter(d => d.children)
      .style("cursor", "pointer")
      .on("click", clicked);

  path.append("title")
      .text(d => `${d.ancestors().map(d => d.data.name).reverse().join("/")}\n${format(d.value)}`);

  const label = g.append("g")
      .attr("pointer-events", "none")
      .attr("text-anchor", "middle")
      .style("user-select", "none")
    .selectAll("text")
    .data(root.descendants().slice(1))
    .join("text")
      .attr("dy", "0.35em")
      .attr("fill-opacity", d => +labelVisible(d.current))
      .attr("transform", d => labelTransform(d.current))
      .text(d => d.data.name);
      

  const parent = g.append("circle")
      .datum(root)
      .attr("r", radius)
      .attr("fill", "none")
      .attr("pointer-events", "all")
      .on("click", clicked);

  function clicked(p) {
    parent.datum(p.parent || root);

    root.each(d => d.target = {
      x0: Math.max(0, Math.min(1, (d.x0 - p.x0) / (p.x1 - p.x0))) * 2 * Math.PI,
      x1: Math.max(0, Math.min(1, (d.x1 - p.x0) / (p.x1 - p.x0))) * 2 * Math.PI,
      y0: Math.max(0, d.y0 - p.depth),
      y1: Math.max(0, d.y1 - p.depth)
    });

    const t = g.transition().duration(750);

    // Transition the data on all arcs, even the ones that aren’t visible,
    // so that if this transition is interrupted, entering arcs will start
    // the next transition from the desired position.
    path.transition(t)
        .tween("data", d => {
          const i = d3.interpolate(d.current, d.target);
          return t => d.current = i(t);
        })
      .filter(function(d) {
        return +this.getAttribute("fill-opacity") || arcVisible(d.target);
      })
        .attr("fill-opacity", d => arcVisible(d.target) ? (d.children ? 0.6 : 0.4) : 0)
        .attrTween("d", d => () => arc(d.current));

    label.filter(function(d) {
        return +this.getAttribute("fill-opacity") || labelVisible(d.target);
      }).transition(t)
        .attr("fill-opacity", d => +labelVisible(d.target))
        .attrTween("transform", d => () => labelTransform(d.current));
  }
  
  function arcVisible(d) {
    return d.y1 <= 3 && d.y0 >= 1 && d.x1 > d.x0;
  }

  function labelVisible(d) {
    return d.y1 <= 3 && d.y0 >= 1 && (d.y1 - d.y0) * (d.x1 - d.x0) > 0.03;
  }

  function labelTransform(d) {
    const x = (d.x0 + d.x1) / 2 * 180 / Math.PI;
    const y = (d.y0 + d.y1) / 2 * radius;
    return `rotate(${x - 90}) translate(${y},0) rotate(${x < 180 ? 0 : 180})`;
  }
}

}

