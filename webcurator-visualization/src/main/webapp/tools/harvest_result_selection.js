class HarvestResult{
    constructor(id_ti_dropdown, id_hr_dropdown){
        this.id_ti_dropdown=id_ti_dropdown;
        this.id_hr_dropdown=id_hr_dropdown;
    }

    init(){
        var reqUrl="/vis/all_hr_results";
        var that=this;
        //g_TurnOnOverlayLoading();
        fetchHttp(reqUrl, null, function(data){
            that.data=data;
            that.fill_ti_dropdown();
        });
    }

    fill_ti_dropdown(){
        var h="";
        for(var i=0; i<this.data.length; i++){
            var ti=this.data[i];
            var tiId=ti.tiId;
            var option="<option>" + tiId + "</option>"
            h+=option;
        }
        $(this.id_ti_dropdown).html(h);
    }

    fill_hr_list(hrList){
//        var hrList=this.data[tiId].hrList;
        var h="";
        for(var i=0; i<hrList.length; i++){
            var tr="<tr>";
            tr=tr+"<td><span class='badge bg-primary'>"+tr.hrNumber+"</span></td>";
            if(tr.isIndexed){
                tr=tr+"<td>Indexed</td>";
            }else{
                tr=tr+"<td>UnIndexed</td>";
            }
            tr=tr+"<td><a href='javascript:'>Review</a></td>"
        }


        <tr>
                <td><span class="badge bg-primary">1</span></td>
                <td>Indexed</td>
                <td><a href=''>Review</a></td>
              </tr>
    }
}