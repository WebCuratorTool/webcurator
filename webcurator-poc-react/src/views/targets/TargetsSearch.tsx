import './TargetsSearch.scss';
import { Form, Table, Button, Stack } from 'react-bootstrap';

import Select from 'react-select'
import WctFormControlGroup from "../../components/WctFormControlGroup"
import WctDropdownSelect from '../../components/WctDropdownSelect';

const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
]

function TargetsSearchView() {
  return (
    <Form>
        <div className="d-flex align-items-center mb-3">
            <div className="p-2 bd-highlight">
                <WctFormControlGroup controlId="targetId" labelName="ID"  type="text" placeholder="" value="323"/>
            </div>
            <div className="p-2 bd-highlight">
                <WctFormControlGroup controlId="targetName" labelName="Name"  type="text" placeholder="" value="rnz"/>
            </div>
            <div className="p-2 bd-highlight">
                <WctFormControlGroup controlId="targetSeed" labelName="Seed"  type="text" placeholder="" value="https://"/>
            </div>
            <div className="p-2 bd-highlight">
                <WctDropdownSelect labelName="Agency" options={[]} />
            </div>
            <div className="p-2 bd-highlight">
                <div className="mb-3">
                    <label htmlFor="formGroupExampleInput" className="form-label"> &nbsp; </label>
                    <Button className="form-control" variant="outline-primary">Archive</Button>
                </div>
            </div>
            <div className="ms-auto p-2 bd-highlight">
                <div className="mb-3">
                    <label htmlFor="formGroupExampleInput" className="form-label"> &nbsp;  </label>
                    <Button className="form-control" variant="primary">Search</Button>
                </div>
            </div>
        </div>
    </Form>
  );
}

export default TargetsSearchView;