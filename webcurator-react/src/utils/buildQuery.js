const buildQuery = async (searchTerms, shouldSort, sortOptions, pageOffset) => {
    let params = [];
    Object.keys(searchTerms).forEach(key => {
      if (searchTerms[key].length > 0) {
        params.push(`${key}=${searchTerms[key]}`);
      }
    })
    if (shouldSort) {
      params.push(`sortBy=${sortOptions.accessor},${sortOptions.direction}`)
    }
    if (pageOffset > 9) {
      params.push(`offset=${pageOffset}&limit=10`)
    }
    let query = `${params.join('&')}`;
    return query;
}

export default buildQuery;