import Nav from 'react-bootstrap/Nav';
import { Outlet, NavLink } from 'react-router-dom';
import { Table, Image, Container } from 'react-bootstrap';
import { BsFillHouseDoorFill ,BsFillGrid3X3GapFill,BsFillStickiesFill, BsFillPersonLinesFill,BsBuildingsFill,BsBullseye,BsFillDiagram3Fill,BsPersonFillGear} from 'react-icons/bs'; 
import './AppSideBar.css';
import logo from './wct_logo.png';


function AppSideBar() {
  return (
    <div className='main-page'>
      <Table bsPrefix="main-page-table" borderless>
        <tbody>
          <tr>
            <td style={{width:"15vw"}}>
              <div className='app-sidebar'>
                <div className='header'>
                  <Container>
                    <Image width={171} src={logo}  fluid />
                  </Container>
                </div>
                <div>
                  <Nav defaultActiveKey="/home" className="flex-column">
                    <Nav.Link as={NavLink} to="/home"><BsFillHouseDoorFill/>&emsp;Home</Nav.Link>
                    <Nav.Link as={NavLink} to="/dashboard"><BsFillGrid3X3GapFill/>&emsp;Dashboard</Nav.Link>
                    <Nav.Link as={NavLink} to="/queue"><BsFillStickiesFill/>&emsp;Queue</Nav.Link>
                    <Nav.Link as={NavLink} to="/harvest-authorisations"><BsBuildingsFill/>&emsp;Harvest Authorisations</Nav.Link>
                    <Nav.Link as={NavLink} to="/targets"><BsBullseye/>&emsp;Targets</Nav.Link>
                    <Nav.Link as={NavLink} to="/groups"><BsFillDiagram3Fill/>&emsp;Groups</Nav.Link>
                    <Nav.Link as={NavLink} to="/management"><BsPersonFillGear/>&emsp;Management</Nav.Link>
                  </Nav>
                </div>
              </div>
            </td>

            <td style={{}}>
                <div className="main-content-container">
                  <Outlet />
                </div>
            </td>
          </tr>
        </tbody>
      </Table>
    </div>
  );
}

export default AppSideBar;