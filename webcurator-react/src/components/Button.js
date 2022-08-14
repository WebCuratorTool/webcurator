import React from "react"

import '../styles/button.css'

function Button(props) {
    const { name, onClick } = props;

    return (
        <button
            className='button'
            onClick={onClick}
        >
            {name}
        </button>
    )
}

export default Button;