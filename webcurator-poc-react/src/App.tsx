import React from 'react';
import logo from './assets/wct_logo.png';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import AppSideBar from './views/app-sidebar/AppSideBar'
import DashboardView from './views/dashboard/Dashboard'

import { Row ,Col } from 'react-bootstrap';
import { BrowserRouter,Routes,Route } from "react-router-dom";


function App() {
  return (
    <div className='App'>
      <Row>
        <Col md={3}>
            <div>
              <img src={logo} height="75" />
              <AppSideBar/>
            </div>
        </Col>
        <Col>
          {/* <BrowserRouter  basename="/app"> */}
            <Routes>
              {/* <Route path="/" element={<Home />} /> */}
              <Route path="/dashboard" element={<DashboardView />} />
            </Routes>
          {/* </BrowserRouter> */}
        </Col>
      </Row>
    </div>
  );
}

export default App;
