export const formatDate = (timestamp:number) => {
    const value=new Date(timestamp);
    return value.toLocaleDateString(undefined, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};


export const formatDatetime = (timestamp:number) => {
    const value=new Date(timestamp);
    return value.toLocaleString();
};