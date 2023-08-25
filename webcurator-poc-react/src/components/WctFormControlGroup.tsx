function WctFormControlGroup(props:any) {
    return (
        <div className="mb-3">
            <label htmlFor="formGroupExampleInput" className="form-label">{props.labelName}</label>
            <input type={props.type} className="form-control" id={props.controlId} placeholder={props.placeholder} />
        </div>
    );
}

export default WctFormControlGroup;