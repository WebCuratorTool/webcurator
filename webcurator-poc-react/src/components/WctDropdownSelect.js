// import { Form } from 'react-bootstrap';
function WctDropdownSelect(props) {
    let options=[];
    for(var i=0; i<props.options.length; i++){
        var option=props.options[i];
        options.push( <option key={ option.oid} value={ option.oid} >{ option.name }+ </option>); 
    }
    return (
        // <Form.Group className="p-1" controlId={props.controlId}>
        //     <Form.Label>{props.labelName}</Form.Label>
        //     <Form.Select>  {options} </Form.Select>
        // </Form.Group>

        <div className="p-1">
            <label htmlFor={props.controlId} className="form-label">{props.labelName}</label>
            <select className="form-select" id={props.controlId} aria-label="Default select example" onChange={(e) => props.onTextChange(e.target.value)}> {options} </select>
        </div>

    );
}

export default WctDropdownSelect;