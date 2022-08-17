import React, { useEffect, useState } from 'react';
import axios from 'axios';

import buildQuery from '../../utils/buildQuery';

import TargetsView from './Targets.view';
import TargetsTable from './TargetsTable';

function TargetsContainer() {
  const [targets, setTargets] = useState(null);
  const [loading, setLoading] = useState(true);

  const [searchTerms, setSearchTerms ] = useState({
    targetId: '',
    name: '',
    seed: '',
    description: ''
  });

  const [shouldSort, setShouldSort] = useState(false);
  const [sortOptions, setSortOptions] = useState({
    accessor: '',
    direction: 'asc',
  });

  const [pageOffset, setPageOffset] = useState(0); 
  
  useEffect(() => {
    fetchTargets();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageOffset, sortOptions]) 

  const fetchTargets = async () => {
    setLoading(true)
    let url = '/wct/api/v1/targets';
    let query = await buildQuery(searchTerms, shouldSort, sortOptions, pageOffset);
    if (query.length > 0) {
      url += `?${query}`;
    }
    
    try {
      const response = await axios.get(url);
      setTargets(response.data.targets);
      setLoading(false);
    } catch (e) {
      console.log(e);
    }
  }

  const updateSearchTerms = (key, value) => {
    let updatedSearchTerms = searchTerms;
    updatedSearchTerms[key] = value;
    setSearchTerms({...updatedSearchTerms});
  }

  const clearSearchTerms = () => {
    setSearchTerms({
        targetId: '',
        name: '',
        seed: '',
        description: ''
    })
    setShouldSort(false);
    fetchTargets();
  }

  const onChangeSortBy = (accessor) => {
    setSortOptions(prevSortOptions => ({
      accessor,
      direction: prevSortOptions.direction === 'asc' ? 'desc' : 'asc'
    }))
    setShouldSort(true);
  }

  const renderTargetsTable = () => {
    return (
      <TargetsTable
          onChangeSortBy={onChangeSortBy}
          pageOffset={pageOffset}
          setPageOffset={setPageOffset}
          sortOptions={sortOptions}
          targets={targets}
      />
    )
  }
  
  return (
    <TargetsView
        loading={loading}
        clearSearchTerms={clearSearchTerms}
        onSearchTargets={fetchTargets}
        searchTerms={searchTerms}
        renderTargetsTable={renderTargetsTable}
        updateSearchTerms={updateSearchTerms}
    />
  );
}

export default TargetsContainer;
