import './Targets.css';
import MainContentTitle from '../MainContentTitle'; 
import TargetsSearchView from './TargetsSearch';

function TargetsView() {
  return (
    <div className='main-content'>
        <MainContentTitle mainContentTitle='Targets' />

        <div className="queue-search">
            <div className="search-title">Query</div>
            <TargetsSearchView />
            <br/>
            {/* <QueueFilterVue @btnFilter="filterResults" /> */}
            <br/>
            {/* <QueueResultVue ref="resultTable"/> */}
        </div>
    </div>
  );
}

export default TargetsView;