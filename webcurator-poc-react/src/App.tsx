import React from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import AppSideBar from './views/app-layout/AppSideBar'
import DashboardView from './views/dashboard/Dashboard'
import TargetsView from './views/targets/Targets';

import { BrowserRouter,Routes,Route } from "react-router-dom";


function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<AppSideBar />}>
            <Route path="/dashboard" element={<DashboardView />} />
            <Route path="/targets" element={<TargetsView />} />
          </Route>
      </Routes>
    </BrowserRouter> 
  );
}

export default App;
