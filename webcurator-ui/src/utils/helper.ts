import { routes } from '@/router';

export const formatDate = (timestamp:number) => {
    const value=new Date(timestamp);
    return value.toLocaleDateString(undefined, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};

export const formatTime = (timestamp: number) => {
    const value = new Date(timestamp);    
    return value.toLocaleTimeString([], { hour12: false, timeStyle: 'short' });
}

export const formatDatetime = (timestamp:number) => {
    const value = new Date(timestamp);
    return value.toLocaleString();
};

export const filterRoutePathByName = (routeName:string)=>{
    let path=routes.routes[0].path;
    for(let i=0; i<routes.routes[0].children.length;i++){
        const r=routes.routes[0].children[i];
        if(r.name === routeName){
            return path + '/' + r.path;
        }
    }
    return undefined;
};

export const getRouteURLByName = (routeName:string, params:any=undefined)=>{
    let path=filterRoutePathByName(routeName);
    if(!path){
        return "/";
    }

    if(!params){
        return path;
    }
    
    for(const key in params){
        const value=params[key];
        path=path.replace(':'+key,value);
    }
    return path;
};

export const camelCaseToTitleCase = (s: string) => {
    const result = s.replace(/([A-Z])/g, ' $1');
    return result.charAt(0).toUpperCase() + result.slice(1);
}