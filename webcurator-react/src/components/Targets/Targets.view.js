import React from 'react';

import '../../styles/targets.css';

import Header from '../Header';
import Loading from '../Loading';
import TargetsTable from './TargetsTable';

function TargetsView(props) {
    const {
        loading,
        targets,
        clearSearchTerms,
        onChangeSortBy,
        onSearchTargets,
        pageOffset,
        searchTerms,
        setPageOffset,
        sortOptions,
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
            {!loading && (
                <TargetsTable
                    onChangeSortBy={onChangeSortBy}
                    pageOffset={pageOffset}
                    setPageOffset={setPageOffset}
                    sortOptions={sortOptions}
                    targets={targets}
                />
            )}
        </div>
    );
}

export default TargetsView;
