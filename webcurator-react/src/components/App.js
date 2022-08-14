import React from "react";
import { Routes,Route } from "react-router-dom";

import '../styles/App.css'

import Targets from "./Targets/Targets.container";
import Home from "./Home";

function App() {
  return (
    <div className="page">
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="targets" element={<Targets />} />
      </Routes>
    </div>
  )
}

export default App;