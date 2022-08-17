import React from 'react';

import '../../styles/targets.css';

import Header from '../Header';
import Loading from '../Loading';


function TargetsView(props) {
    const {
        loading,
        clearSearchTerms,
        onSearchTargets,
        renderTargetsTable,
        searchTerms,
        updateSearchTerms
    } = props;
        
    return (
        <div>
            <Header title={'Targets'} />
            <form aria-label="form" className='search-form' onSubmit={(e) => { e.preventDefault(); onSearchTargets() }}>
                {Object.keys(searchTerms).map(key => {
                    return (
                        <label key={key} className='search-field'>
                            {key === 'targetId' ? 'ID' : `${key.charAt(0).toUpperCase() + key.slice(1)}`}
                            <input
                                data-testid={`${key}-input`}
                                type="text"
                                value={searchTerms[key]}
                                onChange={(e) => updateSearchTerms(key, e.target.value)}
                            />
                        </label>
                    )
                })}
                <div className='button-group'>
                    <input
                        className='submit-button'
                        disabled={loading}
                        type="submit"
                        value="Search"
                    />
                    <button
                        className='clear-button'
                        disabled={loading}
                        onClick={clearSearchTerms}
                    >
                        Reset
                    </button>
                </div>
            </form>

            {loading && <Loading />}
            {!loading && renderTargetsTable()}
        </div>
    );
}

export default TargetsView;
