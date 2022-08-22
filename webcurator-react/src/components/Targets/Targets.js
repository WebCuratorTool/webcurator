import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';

import '../../styles/targets.css';

import buildQuery from '../../utils/buildQuery';

import Header from '../Header';
import Loading from '../Loading';
import TargetsTable from './TargetsTable';

import {
  fetchTargets,
  resetSearchTerms,
  setSearchTerms,
} from '../../redux/targets';

function TargetsContainer() {
  const {
    loading,
    pageOffset,
    searchTerms,
    shouldSort,
    sortOptions,
  } = useSelector((state) => state.targets);

  const dispatch = useDispatch();

  const fetch = async () => {
    const query = await buildQuery(searchTerms, shouldSort, sortOptions, pageOffset);
    dispatch(fetchTargets(query));
  }
  
  useEffect(() => {
    fetch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageOffset, sortOptions]) 

  return (
    <div>
        <Header title='Targets' />
        <form aria-label="form" className='search-form' onSubmit={(e) => { e.preventDefault(); fetch() }}>
            {Object.keys(searchTerms).map(key => {
                return (
                    <label key={key} className='search-field'>
                        {key === 'targetId' ? 'ID' : `${key.charAt(0).toUpperCase() + key.slice(1)}`}
                        <input
                            data-testid={`${key}-input`}
                            type="text"
                            value={searchTerms[key]}
                            onChange={(e) => dispatch(setSearchTerms({key, value: e.target.value}))}
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
                    onClick={() => dispatch(resetSearchTerms())}
                >
                    Reset
                </button>
            </div>
        </form>

        {loading && <Loading />}
        {!loading && <TargetsTable />}
    </div>
  );
}

export default TargetsContainer;
