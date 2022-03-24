class NetworkMapChart{
  constructor(containerContentType, containerStatusCode, statKey){
  	this.containerContentType=containerContentType;
  	this.containerStatusCode=containerStatusCode;
  	this.statKey=statKey;
    this.maxContentType;
  	// this.options = {'title':'', 'width':300, 'height':300};
    // this.options={};
    this.options = {
        title: 'Population of Largest U.S. Cities',
        chartArea: {width: '50%'},
        annotations: {
          alwaysOutside: true,
          textStyle: {
            fontSize: 12,
            auraColor: 'none',
            color: '#555'
          },
          boxStyle: {
            stroke: '#ccc',
            strokeWidth: 1,
            gradient: {
              color1: '#f3e5f5',
              x1: '0%', y1: '0%',
            }
          }
        },
        hAxis: {
          title: 'Total Population',
          minValue: 0,
        },
        vAxis: {
          title: 'key'
        }
    };


    this.node;
  }

  setData(node){
    this.node=node;
  }

  draw(){
    this.drawDatasetGroupByContentType();
    this.drawDatasetGroupByStatusCode();
  }

  drawDatasetGroupByContentType(){
    var node=this.node;
    var stat={};
    var maxKey=null, maxValue=0;
    for(var i=0; i<node.statData.length; i++){
      var statNode=node.statData[i];
      var contentType=statNode.contentType;
      var value=statNode[this.statKey];
      if(stat[contentType]){
        stat[contentType]=stat[contentType] + value;
      }else{
        stat[contentType]=value;
      }

      if(stat[contentType] > maxValue){
        maxValue=stat[contentType];
        maxKey=contentType;
      }
    }

    this.maxContentType=maxKey;


    var data=this.formatGoogleChartData(stat);
    // Set chart options
    this.options.title=this.node.title;
    
    // Instantiate and draw our chart, passing in some options.
    var chart = new google.visualization.BarChart(document.getElementById(this.containerContentType));
    chart.draw(data, this.options);

    return stat;
  }

  drawDatasetGroupByStatusCode(){
    var node=this.node;
    var stat={};
    for(var i=0; i<node.statData.length; i++){
      var statNode=node.statData[i];
      if (statNode.contentType !== this.maxContentType) {
        continue;
      }

      var statusCode=statNode.statusCode;
      var value=statNode[this.statKey];
      if(stat[statusCode]){
        stat[statusCode]=stat[statusCode] + value;
      }else{
        stat[statusCode]=value;
      }
    }

    var data=this.formatGoogleChartData(stat);
    // Set chart options
    this.options.title=this.maxContentType;
    
    // Instantiate and draw our chart, passing in some options.
    var chart = new google.visualization.PieChart(document.getElementById(this.containerStatusCode));
    chart.draw(data, this.options);

    return stat;
  }


  formatGoogleChartData(data){
    var list=[];
    list.push(['key','value']);
    for(var key in data){
      var value=data[key];
      list.push([key,value]);
    }
    return google.visualization.arrayToDataTable(list);
  }
}
