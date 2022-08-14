import React from "react";
import { Link} from "react-router-dom";
import Header from "./Header";

function Home() {
    return (
        <div>
            <Header title={'Web Curator Tool'} />
            <h2>React Proof of Concept</h2>
            <p>Use exisiting UI running on localhost:8080 to login</p>
            <p>Then navigate to <Link to="/targets">targets</Link> page to view the POC</p> 
        </div>
    )
}

export default Home;