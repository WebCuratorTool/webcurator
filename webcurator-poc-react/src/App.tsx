import React from 'react';
import logo from './assets/wct_logo.png';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import StackedExample from './views/app/AppSideBar'

function App() {
  return (
    <div className="">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.tsx</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>

        
      </header>
      <Sidebar>
        <div>
          <img src={logo} height="75" />
          <StackedExample/>
        </div> 
      </Sidebar>
    </div>
    
     
  );
}

export default App;
