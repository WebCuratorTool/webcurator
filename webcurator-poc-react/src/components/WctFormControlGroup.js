function WctFormControlGroup(props) {
    return (
        <div className="p-1">
            <label htmlFor={props.controlId} className="form-label">{props.labelName}</label>
            <input type={props.type} className="form-control" id={props.controlId} placeholder={props.placeholder} onChange={(e) => props.onTextChange(e.target.value)}/>
        </div>
    );
}

export default WctFormControlGroup;