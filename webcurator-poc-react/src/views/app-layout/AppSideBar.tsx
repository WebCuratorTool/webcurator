import Nav from 'react-bootstrap/Nav';
import { BsFillHouseDoorFill ,BsFillGrid3X3GapFill,BsFillStickiesFill, BsFillPersonLinesFill,BsBuildingsFill,BsBullseye,BsFillDiagram3Fill,BsPersonFillGear} from 'react-icons/bs'; 
import './AppSideBar.css';

function AppSideBar() {
  return (
    <div className='app-sidebar'>
      <Nav defaultActiveKey="/home" className="flex-column">
        <Nav.Link href="/home"><BsFillHouseDoorFill/>&emsp;Home</Nav.Link>
        <Nav.Link eventKey="link-1"><BsFillGrid3X3GapFill/>&emsp;Dashboard</Nav.Link>
        <Nav.Link eventKey="link-1"><BsFillStickiesFill/>&emsp;Queue</Nav.Link>
        <Nav.Link eventKey="link-2"><BsBuildingsFill/>&emsp;Harvest Authorisations</Nav.Link>
        <Nav.Link eventKey="link-1"><BsBullseye/>&emsp;Targets</Nav.Link>
        <Nav.Link eventKey="link-1"><BsFillDiagram3Fill/>&emsp;Groups</Nav.Link>
        <Nav.Link eventKey="link-1"><BsPersonFillGear/>&emsp;Management</Nav.Link>
      </Nav>
    </div>
  );
}

export default AppSideBar;